package actress.sys.binding

import akka.actor.ActorRef

case class ModelEndpointBindingDelegate(val name: String, val endpoint: ActorRef) extends ModelEndpointBinding