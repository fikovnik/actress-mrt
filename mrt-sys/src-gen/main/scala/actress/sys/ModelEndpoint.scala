package actress.sys

import akka.actor.ActorRef

trait ModelEndpoint {

  def name: String
  def endpoint: ActorRef
  
}