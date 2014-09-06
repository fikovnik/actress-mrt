package actress.core

import fr.inria.spirals.actress.acore.{APackage, AMutableSequence, AObject, Containment}

trait ModelRegistry extends AObject {

  @Containment
  def models: AMutableSequence[AObject]

  @Containment
  def metamodels: AMutableSequence[APackage]

}