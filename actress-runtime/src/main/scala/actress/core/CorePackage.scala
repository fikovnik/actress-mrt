package actress.core

import fr.inria.spirals.actress.metamodel.{AReference, AClass, APackage}

object CorePackage extends CorePackageImpl

trait CorePackage extends APackage {

  val ModelRegistryClass: AClass
  
  val ModelRegistryClass_models_Feature: AReference
  val ModelRegistryClass_metamodels_Feature: AReference

}
