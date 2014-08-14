package fr.inria.spirals.actress.runtime

import scala.reflect.ClassTag
import scala.reflect.classTag

import actress.sys.ModelsEndpoints
import actress.sys.ModelsEndpointsBinding
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import fr.inria.spirals.actress.metamodel.Attribute
import fr.inria.spirals.actress.metamodel.Reference
import fr.inria.spirals.actress.runtime.protocol._
import fr.inria.spirals.actress.util.Reflection._

trait Binding[T] {
  // TODO: it should be used to guide the implementation of the binding
  // will see how we can do that later
  // this: T ⇒
}

trait BindingFactory[T] extends (Option[String] => Binding[T])


class ModelActor[T: ClassTag](bindingFactory: BindingFactory[T]) extends Actor with ActorLogging {

  // in the future, there could be different binding scopes (similarly to JEE beans)
  // currently all scopes are request based, but there could be
  // - an application scope (a scope shared with all actors during the life type of the actor system)
  //   it is a bit dangerous as it can easily break the actor encapsulation and it has to be thread safe
  // - an actor scope (a scope shared by all requests within the same actor)
  // - a session scope (a scope shared by all requests from a given client and responded by the same actor)
  //   this will have to get coupled with a new routing policy having one actor per client

  // TODO: handle error
  def binding(elementId: Option[String]) = bindingFactory(elementId)
    
  val features = classTag[T]
    .runtimeClass
    .declaredMethods
    .filter { m ⇒ m.hasAnnotation[Attribute] || m.hasAnnotation[Reference] }
    .map { m ⇒ m.name -> m }
    .toMap

  override def receive = {
    case msg @ Get(name, elementId) ⇒
      log debug s"Get $msg for $elementId"
      doGet(name, elementId)
  }

  def doGet(name: String, elementId: Option[String]) {
    val bnd = binding(elementId)

    features get name match {
      case Some(method) ⇒
        val value = method.invoke(bnd)
        sender ! AttributeValue(name, value, elementId)
      case None ⇒
        log warning s"$name: unknown attribute"
        sender ! UnknownAttribute(name, elementId)
    }
  }

}

class ActressServer {

  val sys = ActorSystem("actress-server")
  
  val modelsEndpoints = registerModel[ModelsEndpoints]("models-endpoints", new BindingFactory[ModelsEndpoints] {
    // application scope binding
    // see the comments in ModelsEndpointsBinding
    val binding = new ModelsEndpointsBinding
    def apply(elementId: Option[String]) = binding
  }) 

  def registerModel[T: ClassTag](name: String, bindingFactory: BindingFactory[T]): ActorRef = {
    val ref = sys.actorOf(Props(new ModelActor[T](bindingFactory)), name)
    ref
  }

}