package fr.inria.spirals.actress.runtime

import actress.core.{CorePackage, ModelRegistry}
import akka.actor._
import akka.agent.Agent
import fr.inria.spirals.actress.acore._
import fr.inria.spirals.actress.acore.impl.AObjectImpl
import fr.inria.spirals.actress.acore.util.AMutableSequence
import fr.inria.spirals.actress.runtime.protocol._

import scala.util.{Failure, Success}

/**
 *
 * A user can either create a new context or connect to an exition one
 *
 * {{{
 * val ctx = new ActressContext // new context
 * val ctx = ActressContext.connect() // connect
 * }}}
 */
class ActressContext {

  // TODO: extract name (as parameter)
  private val sys = ActorSystem("actress-server")

  val endpoint = sys.actorOf(Props[ActressEndpointActor], "endpoint")

}


class ActressEndpointActor extends Actor with ActorLogging {

  import scala.concurrent.ExecutionContext.Implicits.global

  // TODO: a question is if there should be a shared runtimeModel?
  // perhaps each RuntimeModelActor can spawn its own children
  val modelsMapAgent = Agent(Map[AClass, ActorRef]())

  // TODO: this single registry makes this actor to be bottle neck, it would be better if it is an agent that gets passed along
  val modelRegistry = new ModelRegistryImpl
  modelRegistry._endpoint = Some(self)

  deploy(AcorePackage)
  deploy(modelRegistry)

  // TODO: it should use the registry / deploy and undeploy
  class ModelRegistryImpl extends AObjectImpl with ModelRegistry {
    override lazy val _class = CorePackage.ModelRegistryClass

    override def _elementName: String = ""

    override lazy val models: AMutableSequence[AObject] = AMutableSequence(this, CorePackage.ModelRegistryClass_models_Feature)
    override lazy val metamodels: AMutableSequence[APackage] = AMutableSequence(this, CorePackage.ModelRegistryClass_metamodels_Feature)
  }

  def deploy(model: AObject): Unit = {
//    // sets the actor reference
//    model.asInstanceOf[AObjectImpl]._endpoint = Some(self)

    // spawn actors for all classes in the package
    spawnRuntimeModelActors(model._class._package)

    if (model != modelRegistry && model._class != AcorePackage.APackageClass) {
      modelRegistry.models += model
    }

    modelRegistry.metamodels += model._class._package
  }

  def spawnRuntimeModelActors(pkg: APackage) {
    modelsMapAgent send { orig =>
      pkg._classes
        .filter(!orig.contains(_))
        .foldLeft(orig) { (m, e) => m + (e -> spawnRuntimeModelActor(e))}
    }
  }

  private def spawnRuntimeModelActor(e: AClass): ActorRef = {
    val ref = context.child(e._name).getOrElse(context.actorOf(Props(classOf[RuntimeModelActor], e, modelsMapAgent), e._name))
    ref
  }

  override def receive = {

    case msg@Get(elementPath, feature) =>
      if (elementPath.head != ElementPathSegment.Root) {
        sender ! UnresolvableElementPath(elementPath)
      } else {
        val originalSender = context.sender()
        val instance = modelRegistry
        modelsMapAgent.future().onComplete {
          case Failure(e) => sys.error("Unable to get a registry: "+e)
          case Success(modelsMap) =>
            val ref = modelsMap get instance._class match {
              case Some(x) => x
              case None => sys.error(s"Unknown class: ${instance._class._name} in ${modelsMapAgent()}")
            }

            log info s"Forwarding message: $msg to $ref"

            ref ! FwdGet(instance, elementPath.tail, feature, originalSender)
        }
      }
  }
  
}

// in the generated version, we would have an actor per AClass
// in the reflective, the implementation is all the same
// TODO: move to router
class RuntimeModelActor(model: AClass, runtimeModels: Agent[Map[AClass, ActorRef]]) extends Actor with ActorLogging {

  override def receive = {
    case msg@FwdGet(instance, elementPath, feature, originalSender) =>
      log info s"Received message: $msg"

      elementPath match {
        case None =>
          // resolve feature
          model._allFeatures find (_._name == feature) match {
            case None =>
              originalSender ! UnknownFeature(feature)

            case Some(f) => instance._get(f) match {
              case rs: Iterable[_] =>
                originalSender ! References(rs collect { case r: AObject => Reference(r._elementPath, r._endpoint.get)})

              case r: AObject =>
                originalSender ! Reference(r._elementPath, r._endpoint.get)

              case r =>
                originalSender ! AttributeValue(f._name, r)
            }
          }

        case Some(path) =>
          // resolve instance
          val segment = path.head
          // TODO: only references
          model._allFeatures find (_._name == segment.feature) match {

            case None =>
              originalSender ! UnknownFeature(segment.feature)

            case Some(f) =>
              log info s"Accessing feature ${f._name} in $instance (_class: ${instance._class})"
              instance._get(f) match {

                case r: AObject =>
                  val ref = runtimeModels()(r._class)
                  ref ! FwdGet(r, path.tail, feature, originalSender)

                case None =>

                case rs: Iterable[_] => rs collectFirst {
                  case x: AObject if x._elementName == segment.elementName => x
                } match {

                  case Some(r) =>
                    val ref = runtimeModels()(r._class)
                    ref ! FwdGet(r, path.tail, feature, originalSender)

                  case None =>
                    originalSender ! UnknownElement(segment.elementName)
                }

                case _ => sys.error(s"Expected a reference while calling $f on $instance")
              }

          }

      }

  }
}

