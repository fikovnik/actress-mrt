package actress.sys.binding

import actress.sys.ModelEndpoint
import akka.actor.ActorRef

trait ModelEndpointBinding {

  def name: String
  def endpoint: ActorRef
  
}