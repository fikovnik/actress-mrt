package actress.sys.runtime

import actress.sys.binding.ModelsEndpointsBinding
import scala.util.Try
import fr.inria.spirals.actress.runtime.protocol.UnknownAttribute
import fr.inria.spirals.actress.runtime.protocol.References
import fr.inria.spirals.actress.runtime.protocol.GetReply
import fr.inria.spirals.actress.runtime.protocol.Reference
import actress.sys.ModelEndpoint

trait ModelsEndpointsActor {

  def binding(elementId: Option[String]): ModelsEndpointsBinding
  
  def doGet(name: String, elementId: Option[String]): Try[GetReply] = Try {
    name match {
      case "endpoints" => References(binding(elementId).endpoints, endpointFor[ModelEndpoint]) 
      case _ => UnknownAttribute(name, elementId)
    }
  }
  
}