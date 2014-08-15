package actress.sys.binding

import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConversions._

import actress.sys.ModelEndpoint
import actress.sys.ModelsEndpoints
import akka.actor.ActorRef

trait ModelsEndpointsBinding {
  
  def endpoints: Iterable[String]
  def endpoints_add(element: ModelEndpoint): String
  def endpoints_del(elementId: String): Unit
  
  // TODO: monitor
  
}