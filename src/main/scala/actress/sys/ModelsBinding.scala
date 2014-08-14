package actress.sys

import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConversions.asScalaSet
import scala.collection.JavaConversions.mapAsScalaConcurrentMap

import akka.actor.ActorRef
import fr.inria.spirals.actress.runtime.Binding

case class ModelEndpointBinding(val name: String, val endpoint: ActorRef) extends Binding[ModelEndpoint]

// TODO: mark that this is an application scope binding
// and that it has to be thread safe
// TODO: actually, it could be that an application scope binding cannot be used with routers!!!
class ModelsEndpointsBinding extends Binding[ModelsEndpoints] {
  
  private val _endpoints = new ConcurrentHashMap[String, ActorRef]

  def endpoints: Iterable[String] = _endpoints.keySet

  // FIXME: the following two methods are bad
  // there is nobody that will force us to implement it
  
  def endpoints_add(element: ModelEndpoint): String = {
    _endpoints += element.name -> element.endpoint
    
    element.name
  }
  
  def endpoints_del(elementId: String) {
    _endpoints remove elementId
  }
  
  // TODO: monitor
  
}