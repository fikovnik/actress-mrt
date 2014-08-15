package actress.sys.model

import akka.actor.ActorRef
import fr.inria.spirals.actress.metamodel.MRTClass

class ModelEndpoint extends MRTClass {

  attribute[String]("name", modifiable = false)
  attribute[ActorRef]("endpoint", modifiable = false)
  
}

class ModelsEndpoints extends MRTClass {

  reference[ModelEndpoint]("endpoints", container = true, observable = true, unique = true)

}