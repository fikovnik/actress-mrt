package fr.inria.spirals.actress.runtime

import scala.reflect.ClassTag
import scala.reflect.classTag
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import fr.inria.spirals.actress.runtime.protocol._
import fr.inria.spirals.actress.util.Reflection._
import java.lang.reflect.Method
import scala.util.Success
import scala.util.Failure
import scala.util.Try

//trait Binding[T] {
//  // TODO: it should be used to guide the implementation of the binding
//  // will see how we can do that later, but I don't think it is possible
//  // better would be just generate the code and use inheritance
//  // this: T ⇒
//}
//
//trait BindingFactory[T, U <: Binding[T]] extends (Option[String] ⇒ U)

abstract class MRTClassActor extends Actor with ActorLogging {

  // in the future, there could be different binding scopes (similarly to JEE beans)
  // currently all scopes are request based, but there could be
  // - an application scope (a scope shared with all actors during the life type of the actor system)
  //   it is a bit dangerous as it can easily break the actor encapsulation and it has to be thread safe
  // - an actor scope (a scope shared by all requests within the same actor)
  // - a session scope (a scope shared by all requests from a given client and responded by the same actor)
  //   this will have to get coupled with a new routing policy having one actor per client
    
  override def receive = {
    case msg @ Get(name, elementId) ⇒
      log debug s"Get $msg for $elementId"
      doGet(name, elementId) match {
        case Success(reply) ⇒
          sender ! reply
        case Failure(_) ⇒
          log warning s"$name: unknown attribute"
          sender ! UnknownAttribute(name, elementId)
      }
  }

  // TODO: use Try
  def doGet(name: String, elementId: Option[String]): Try[GetReply]
}

class ActressServer {

  val sys = ActorSystem("actress-server")

//  val modelsEndpoints = registerModel("models-endpoints", new BindingFactory[ModelsEndpoints, ModelsEndpointsBinding] {
//    // application scope binding
//    // see the comments in ModelsEndpointsBinding
//    val binding = new ModelsEndpointsBinding
//    def apply(elementId: Option[String]) = binding
//  })
//
//  def registerModel[T: ClassTag, U <: Binding[T]: ClassTag](name: String, bindingFactory: BindingFactory[T, U]): ActorRef = {
//    val ref = sys.actorOf(Props(new ModelActor[T, U](bindingFactory)), name)
//    ref
//  }

}