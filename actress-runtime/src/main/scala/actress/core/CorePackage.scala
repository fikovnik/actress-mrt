package actress.core

import fr.inria.spirals.actress.acore._

object CorePackage extends CorePackageImpl

trait CorePackage extends APackage {

  val ModelRegistryClass: AClass
  
  val ModelRegistryClass_models_Feature: AReference
  val ModelRegistryClass_metamodels_Feature: AReference

}

trait ModelRegistry extends AObject {

  @Containment
  def models: AMutableSequence[AObject]

  @Containment
  def metamodels: AMutableSequence[APackage]

}