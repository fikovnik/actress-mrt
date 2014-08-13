package fr.inria.spirals.actress.runtime

import akka.actor.ActorSystem
import akka.actor.Actor
import fr.inria.spirals.actress.util.Reflection._
import fr.inria.spirals.actress.metamodel.Attribute
import fr.inria.spirals.actress.metamodel.Reference
import fr.inria.spirals.actress.runtime.protocol._
import akka.event.Logging
import akka.actor.Props
import actress.sys.OSInfoBinding
import actress.sys.OSBinding
import scala.reflect.ClassTag
import scala.reflect.classTag
import scala.collection.mutable.Buffer
import akka.actor.ActorRef

trait Binding[T] {
  this: T ⇒
}

abstract class ActressServerActor[T](bindingFactory: (String) ⇒ Binding[T]) extends Actor {
  lazy val log = Logging(context.system, this)

  // in the future, there could be different binding scopes (similarly to JEE beans)
  // currently all scopes are request based, but there could be
  // - an application scope (a scope shared with all actors during the life type of the actor system)
  //   it is a bit dangerous as it can easily break the actor encapsulation and it has to be thread safe
  // - an actor scope (a scope shared by all requests within the same actor)
  // - a session scope (a scope shared by all requests from a given client and responded by the same actor)
  //   this will have to get coupled with a new routing policy having one actor per client

  def binding(id: String) = bindingFactory(id)

}

class ModelActor[T: ClassTag](bindingFactory: (String) ⇒ Binding[T]) extends ActressServerActor(bindingFactory) {

  val features = classTag[T]
    .runtimeClass
    .declaredMethods
    .filter { m ⇒ m.hasAnnotation[Attribute] || m.hasAnnotation[Reference] }
    .map { m ⇒ m.name -> m }
    .toMap

  override def receive = {
    case msg @ GetAttribute(id, name) ⇒
      log debug s"Received $msg"
      doGet(id, name)
    case GetAttributes => sender ! Attributes(features.keys)
  }

  def doGet(id: String, name: String) {
    val bnd = binding(id)

    features get name match {
      case Some(method) ⇒
        val value = method.invoke(bnd)
        sender ! AttributeValue(id, name, value)
      case None ⇒
        log warning s"$name: unknown attribute"
        sender ! UnknownAttribute(id, name)
    }
  }

}

class ServiceLocator extends Actor {

  lazy val log = Logging(context.system, this)
  val services = Buffer[(String, ActorRef)]()

  def receive = {
    case Register(name, ref) ⇒
      log info s"Registering a new model: $name with ref: $ref"
      context.watch(ref)
      services += name -> ref
    case GetCapabilities() ⇒
      sender ! Capabilities(services.toSeq)
  }
}

class ActressServer {

  val sys = ActorSystem("actress-server")
  val serviceLocator = sys.actorOf(Props[ServiceLocator], "service-locator")

  def registerModel[T: ClassTag](name: String, bindingFactory: (String) ⇒ Binding[T]) {
    //	  val ref = sys.actorOf(Props(classOf[NodeActor[T]], bindingFactory), name)
    val ref = sys.actorOf(Props(new ModelActor[T](bindingFactory)), name)

    serviceLocator ! Register(name, ref)
  }

}