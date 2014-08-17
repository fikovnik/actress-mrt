package actress.core.binding

import actress.core.{ModelEndpoint, ModelsEndpoints}
import akka.actor.ActorRef
import akka.agent.Agent
import fr.inria.spirals.actress.runtime.Binding

case class ModelEndpointBinding(name: String, endpoint: ActorRef) extends Binding

class ModelsEndpointsBinding(_endpoints: Agent[Map[String, ActorRef]]) extends Binding {

  def endpoints: Set[String] = _endpoints().keySet

}