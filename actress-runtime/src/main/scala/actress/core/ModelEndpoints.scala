package actress.core

import akka.actor.ActorRef
import fr.inria.spirals.actress.metamodel.MRTClass

trait ModelEndpoint extends MRTClass {

  def name: String
  def endpoint: ActorRef
  
}

trait ModelsEndpoints extends MRTClass {

  def endpoints: Set[ModelEndpoint]

}