package actress.core

import akka.actor.ActorRef
import fr.inria.spirals.actress.metamodel.{AClass, Containment}

trait ModelEndpoint extends AClass {

  def name: String

  def endpoint: ActorRef

}

trait ModelsEndpoints extends AClass {

  @Containment
  def endpoints: Set[ModelEndpoint]

}