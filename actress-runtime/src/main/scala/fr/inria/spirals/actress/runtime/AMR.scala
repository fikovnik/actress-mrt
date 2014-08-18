package fr.inria.spirals.actress.runtime

import akka.actor._
import akka.agent.Agent
import fr.inria.spirals.actress.metamodel.{AcorePackage, AClass, AObject}
import fr.inria.spirals.actress.runtime.TopLevelAMRActor.{FwdGet, RunModel}
import fr.inria.spirals.actress.runtime.protocol.{AttributeValue, Get}

import scala.collection.mutable

//trait Binding[T <: AObject] extends (String => T)

class AMRActor(model: AClass) extends Actor with ActorLogging {

  override def preStart(): Unit = {
    // spawn new actors for all contained references
    model._references filter (_._containment) foreach { r =>

      log info s"starting model actor for contained reference `${r._name}`"

      context.actorOf(Props(new AMRActor(r._type)), r._name)

    }

    // TODO: how to handle inheritance?
    // TODO: get actorrefs for non-contained references
//    model._references filterNot (_._containment) foreach { r => println(r._name) }
  }

  // def createAObject[T <: AObject]()= {
  //   obj._container = context.self
  // }


  override def receive = {
    case msg @ FwdGet(originalSender, instance, feature) =>
      log info msg.toString

      val f = model._features find (_._name == feature)
      val r = instance._get(f.get)

      originalSender ! AttributeValue("a", r)

      // if it is a contained reference then we have the actorRef
      // if it is not a contained reference then we need to figure it out from r._actor

  }

}


object TopLevelAMRActor {

  case class RunModel(name: String, instance: AObject)

  case class FwdGet(originalSender: ActorRef, instance: AObject, feature: String)

}

class TopLevelAMRActor extends Actor with ActorLogging {
  case class Model(instance: AObject, ref: ActorRef)

  val models = mutable.Map[String, Model]()

  override def receive = {

    case Get(elementId, feature) =>
      val Model(instance, ref) = models(elementId)

      log info s"Accessing ${instance._class}"

      val originalSender = context.sender()

      ref ! FwdGet(originalSender, instance, feature)


    case RunModel(name, instance) =>
      val ref = context.actorOf(Props(new AMRActor(instance._class)), name)
      val model = Model(instance, ref)

      log info s"Running new model $model"

      models += name -> model
  }

}

class AMR {

  private val sys = ActorSystem("actress-server")
  val models = sys.actorOf(Props(new TopLevelAMRActor()), "models")

  def run[T <: AObject](instance: AObject) = {
    models ! RunModel(instance._class._name, instance)
  }

}

