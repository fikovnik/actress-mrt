package actress.core

import akka.actor.ActorRef
import fr.inria.spirals.actress.metamodel.{MRTClass, Observable, MRTFeature}

trait ModelEndpoint extends MRTClass {

  @MRTFeature def name: String
  @MRTFeature def endpoint: ActorRef
  
}

trait ModelsEndpoints extends MRTClass {

  @MRTFeature def endpoints: Observable[Set[ModelEndpoint]]

}