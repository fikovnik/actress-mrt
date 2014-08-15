package actress.core.binding

import actress.core.{ModelEndpoint, ModelsEndpoints}
import akka.actor.ActorRef
import fr.inria.spirals.actress.runtime.Binding

case class ModelEndpointBinding(val name: String, val endpoint: ActorRef) extends Binding[ModelEndpoint]

// TODO: mark that this is an application scope binding and that it has to be thread safe
// TODO: actually, it could be that an application scope binding cannot be used with routers!!!
class ModelsEndpointsBinding(_endpoints: collection.Set[String]) extends Binding[ModelsEndpoints] {

  def endpoints: Set[String] = _endpoints.toSet

  // TODO: monitor

}