package actress.core

import fr.inria.spirals.actress.acore.AcorePackage
import fr.inria.spirals.actress.acore.impl.{AClassImpl, APackageImpl, AReferenceImpl}

trait CorePackageImpl extends APackageImpl with CorePackage {

  _name = "core"

  override val ModelRegistryClass = new AClassImpl
  override val ModelRegistryClass_models_Feature = new AReferenceImpl
  override val ModelRegistryClass_metamodels_Feature = new AReferenceImpl

  initialize()

  def initialize(): Unit = {

    initializeModelRegistryClass()

    _classifiers += ModelRegistryClass
  }

  def initializeModelRegistryClass(): Unit = {
    ModelRegistryClass._name = "ModelRegistry"

    ModelRegistryClass._superTypes ++= Seq(
      AcorePackage.AObjectClass
    )

    ModelRegistryClass._features ++= Seq(
      ModelRegistryClass_models_Feature init { x =>
        x._name = "models"
        x._type = AcorePackage.AObjectClass
        x._many = true
        x._containment = true
      },
      ModelRegistryClass_metamodels_Feature init { x =>
        x._name = "metamodels"
        x._type = AcorePackage.APackageClass
        x._many = true
        x._containment = true
      }
    )
  }

}
