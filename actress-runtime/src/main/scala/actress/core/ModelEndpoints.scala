package actress.core

import akka.actor.ActorRef
import fr.inria.spirals.actress.metamodel.{AObject, Containment}

trait ModelEndpoint extends AObject {

  def name: String

  def endpoint: ActorRef

}

trait ModelsEndpoints extends AObject {

  @Containment
  def endpoints: Set[ModelEndpoint]

}