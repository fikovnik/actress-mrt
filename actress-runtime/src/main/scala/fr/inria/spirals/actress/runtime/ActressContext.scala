package fr.inria.spirals.actress.runtime

import akka.actor._
import akka.agent.Agent
import fr.inria.spirals.actress.metamodel._
import fr.inria.spirals.actress.runtime.protocol._

import scala.collection.mutable

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

  deploy(APackageImpl.Registry)
  deploy(new AModelRegistryImpl)

  class AModelRegistryImpl extends AObjectImpl with AModelRegistry {
    override lazy val _class = AcorePackage.AModelRegistryClass

    _actor = Some(self)

    def models: Set[AObject] = registry.values.toSet

    override def _elementName: String = "models"
  }

  def deploy(model: AObject): Unit = {
    // sets the actor reference
    model.asInstanceOf[AObjectImpl]._actor = Some(self)

    // spawn actors for all classes in the package
    model._class._package._classes foreach { c =>
      runtimeModels send { r => r
        if (!r.contains(c)) {
          val ref = context.actorOf(Props(new RuntimeModelActor(c, runtimeModels)), c._name)
          r + (c -> ref)
        } else r
      }
    }

    registry += model._elementName -> model
  }

  override def receive = {

    case msg@Get(elementPath, feature) =>

      log info s"Received message: $msg"

      val originalSender = context.sender()

      elementPath.segments match {
        case Seq() =>
          val instance = registry(feature)

          originalSender ! Reference(instance._elementPath, instance._actor.get)

        case Seq(x, _*) =>
          val instance = registry(x)

          // TODO: refactor

          log info s"Accessing $instance (_class: ${instance._class})"

          val ref = runtimeModels()(instance._class)

          log info s"Target actor $ref"

          ref ! FwdGet(originalSender, instance, elementPath.tail, feature)
      }
  }

}


// in the generated version, we would have an actor per AClass
// in the reflective, the implementation is all the same
// TODO: move to router
class RuntimeModelActor(model: AClass, runtimeModels: Agent[Map[AClass, ActorRef]]) extends Actor with ActorLogging {

  override def receive = {
    case msg@FwdGet(originalSender, instance, elementId, feature) =>
      log info s"Received message: $msg"

      model._allFeatures find (_._name == feature) match {
        case None =>
          originalSender ! UnknownAttribute(feature)

        case Some(f) => instance._get(f) match {
          case rs: Iterable[AObject] =>
            originalSender ! References(rs map { r => Reference(r._elementPath, r._actor.get)})

          case r: AObject => elementId match {
            case None =>
              originalSender ! Reference(r._elementPath, r._actor.get)

            case Some(childId) =>
              log info s"Accessing $instance (_class: ${instance._class})"

              val ref = runtimeModels()(instance._class)

              log info s"Target actor $ref"

              ref ! FwdGet(originalSender, instance, childId.tail, feature)
          }

          case r =>
            originalSender ! AttributeValue(f._name, r)
        }
      }
  }
}

