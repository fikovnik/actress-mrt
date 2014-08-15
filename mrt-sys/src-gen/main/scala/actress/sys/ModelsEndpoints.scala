package actress.sys

import akka.actor.ActorRef
import fr.inria.spirals.actress.client.Observable

trait ModelsEndpoints {

  def endpoints: Observable[Set[ModelEndpoint]]
  
}