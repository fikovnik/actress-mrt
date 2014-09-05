package fr.inria.spirals.actress.runtime

import akka.actor._
import akka.agent.Agent
import akka.util.Timeout
import fr.inria.spirals.actress.metamodel._
import fr.inria.spirals.actress.runtime.protocol._

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._

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

  val endpoint = sys.actorOf(Props[RegistryActor], "endpoint")

}


class RegistryActor extends Actor with ActorLogging {

  import scala.concurrent.ExecutionContext.Implicits.global

  // should be an agent
  val registry = mutable.Map[String, AObject]()

  val runtimeModels = Agent(Map[AClass, ActorRef]())

  val modelRegistry = new AModelRegistryImpl

  deploy(APackageImpl.Registry)
  deploy(modelRegistry)

  // TODO: should send a ready message to itself and then change the behavior
  Await.result(runtimeModels.future(), 5.seconds)

  class AModelRegistryImpl extends AObjectImpl with AModelRegistry {
    override lazy val _class = AcorePackage.AModelRegistryClass

    _actor = Some(self)

    def models: Set[AObject] = registry.values.toSet

    override def _elementName: String = ""
  }

  def deploy(model: AObject): Unit = {
    // sets the actor reference
    model.asInstanceOf[AObjectImpl]._actor = Some(self)

    // spawn actors for all classes in the package
    model._class._package._classes foreach { c =>
      runtimeModels send { r => r
        if (!r.contains(c)) {
          val ref = context.child(c._name).getOrElse(context.actorOf(Props(classOf[RuntimeModelActor], c, runtimeModels), c._name))
          r + (c -> ref)
        } else r
      }
    }

    // TODO: this must be automatic
    if (model != modelRegistry) {
      registry += model._elementName -> model
      model.asInstanceOf[AObjectImpl]._container = Some(modelRegistry)
      model.asInstanceOf[AObjectImpl]._containmentFeature = Some(modelRegistry._class._references.find(_._name == "models").get)
    }
  }

  override def receive = {

    case msg@Get(elementPath, feature) =>
      if (elementPath.head != ElementPathSegment.Root) {
        sender ! UnresolvableElementPath(elementPath)
      } else {
        val originalSender = context.sender()
        val instance = modelRegistry
        val ref = runtimeModels() get instance._class match {
          case Some(e) => e
          case None => sys.error(s"Unknown class: ${instance._class._name} in ${runtimeModels()}")
        }

        log info s"Forwarding message: $msg to $ref"

        ref ! FwdGet(instance, elementPath.tail, feature, originalSender)
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
                originalSender ! References(rs collect { case r: AObject => Reference(r._elementPath, r._actor.get)})

              case r: AObject =>
                val z = r
                originalSender ! Reference(r._elementPath, r._actor.get)

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

