package actress.sys

import akka.actor.ActorRef
import fr.inria.spirals.actress.metamodel.Observable
import scala.collection.mutable.Buffer
import fr.inria.spirals.actress.metamodel.Reference

trait ModelEndpoint {

  def name: String
  def endpoint: ActorRef

}

trait ModelsEndpoints {

  @Reference
  def endpoints: Observable[Buffer[ModelEndpoint]]

}