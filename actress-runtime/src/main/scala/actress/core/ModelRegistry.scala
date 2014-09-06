package actress.core

import fr.inria.spirals.actress.metamodel.{APackage, AMutableSequence, AObject, Containment}

trait ModelRegistry extends AObject {

  @Containment
  def models: AMutableSequence[AObject]

  @Containment
  def metamodels: AMutableSequence[APackage]

}