package actress.acore.fr.inria.spirals.acress.acore

import akka.actor.ActorRef
import fr.inria.spirals.actress.acore._
import fr.inria.spirals.actress.acore.impl._
import fr.inria.spirals.actress.runtime.protocol.ElementPath

trait AcorePackageImpl extends APackageImpl with AcorePackage {

  override val AStringDataType = new ADataTypeImpl
  override val ALongDataType = new ADataTypeImpl
  override val ABooleanDataType = new ADataTypeImpl
  override val AClassDataType = new ADataTypeImpl
  override val AElementPathDataType = new ADataTypeImpl
  override val AActorRefDataType = new ADataTypeImpl

  override val AObjectClass = new AClassImpl
  override val AModelElementClass = new AClassImpl
  override val APackageClass = new AClassImpl
  override val AClassifierClass = new AClassImpl
  override val AClassClass = new AClassImpl
  override val ADataTypeClass = new AClassImpl
  override val AFeatureClass = new AClassImpl
  override val AAttributeClass = new AClassImpl
  override val AReferenceClass = new AClassImpl

  override val AObjectClass_class_Feature = new AReferenceImpl
  override val AObjectClass_elementName_Feature = new AAttributeImpl
  override val AObjectClass_elementPath_Feature = new AAttributeImpl
  override val AObjectClass_container_Feature = new AReferenceImpl
  override val AObjectClass_containmentReference_Feature = new AReferenceImpl
  override val AObjectClass_endpoint_Feature = new AAttributeImpl
  override val AObjectClass_contents_Feature = new AReferenceImpl

  override val AModelElementClass_name_Feature = new AAttributeImpl

  override val APackageClass_classifiers_Feature = new AReferenceImpl
  override val APackageClass_classes_Feature = new AReferenceImpl
  override val APackageClass_datatypes_Feature = new AReferenceImpl

  override val AClassifierClass_package_Feature = new AReferenceImpl
  override val AClassifierClass_instanceClass_Feature = new AAttributeImpl

  override val AClassClass_abstract_Feature = new AAttributeImpl
  override val AClassClass_superTypes_Feature = new AReferenceImpl
  override val AClassClass_features_Feature = new AReferenceImpl
  override val AClassClass_allFeatures_Feature = new AReferenceImpl
  override val AClassClass_references_Feature = new AReferenceImpl

  override val AFeatureClass_type_Feature = new AReferenceImpl
  override val AFeatureClass_many_Feature = new AAttributeImpl
  override val AFeatureClass_derived_Feature = new AAttributeImpl
  override val AFeatureClass_optional_Feature = new AAttributeImpl
  override val AFeatureClass_ordered_Feature = new AAttributeImpl
  override val AFeatureClass_unique_Feature = new AAttributeImpl

  override val AReferenceClass_containment_Feature = new AAttributeImpl
  override val AReferenceClass_opposite_Feature = new AReferenceImpl

  initialize()

  private def initialize(): Unit = {

    _name = "acore"

    initializeAStringDataType()
    initializeALongDataType()
    initializeABooleanDataType()
    initializeAClassDataType()
    initializeAElementPathDataType()
    initializeAActorRefDataType()

    initializeAObjectClass()
    initializeAModelElementClass()
    initializeAPackageClass()
    initializeAClassifierClass()
    initializeAClassClass()
    initializeADataTypeClass()
    initializeAFeatureClass()
    initializeAAttributeClass()
    initializeAReferenceClass()

    _classifiers += AStringDataType
    _classifiers += ABooleanDataType
    _classifiers += AClassDataType
    _classifiers += AElementPathDataType
    _classifiers += AActorRefDataType

    _classifiers += AObjectClass
    _classifiers += AModelElementClass
    _classifiers += APackageClass
    _classifiers += AClassifierClass
    _classifiers += ADataTypeClass
    _classifiers += AClassClass
    _classifiers += AFeatureClass
    _classifiers += AAttributeClass
    _classifiers += AReferenceClass
  }

  private def initializeAStringDataType(): Unit = {
    AStringDataType._name = "String"
    AStringDataType._instanceClass = classOf[String]
  }

  private def initializeALongDataType(): Unit = {
    AStringDataType._name = "Long"
    AStringDataType._instanceClass = classOf[Long]
  }

  private def initializeABooleanDataType(): Unit = {
    ABooleanDataType._name = "Boolean"
    ABooleanDataType._instanceClass = classOf[Boolean]
  }

  private def initializeAClassDataType(): Unit = {
    AClassDataType._name = "Class"
    AClassDataType._instanceClass = classOf[Class[_]]
  }

  private def initializeAElementPathDataType(): Unit = {
    AElementPathDataType._name = "ElementPath"
    AElementPathDataType._instanceClass = classOf[ElementPath]
  }

  private def initializeAActorRefDataType(): Unit = {
    AActorRefDataType._name = "ActorRef"
    AActorRefDataType._instanceClass = classOf[ActorRef]
  }

  private def initializeAPackageClass(): Unit = {
    APackageClass._name = "APackage"

    APackageClass._superTypes ++= Seq(
      AModelElementClass
    )

    APackageClass._features ++= Seq(
      APackageClass_classifiers_Feature init { x =>
        x._name = "_classifiers"
        x._type = AClassifierClass
        x._containment = true
        x._many = true
        x._opposite = Some(AClassifierClass_package_Feature)
        x._ordered = true
      },
      APackageClass_classes_Feature init { x =>
        x._name = "_classes"
        x._type = AClassClass
        x._many = true
        x._derived = true
        x._ordered = true
      },
      APackageClass_datatypes_Feature init { x =>
        x._name = "_dataTypes"
        x._type = ADataTypeClass
        x._many = true
        x._derived = true
        x._ordered = true
      })
  }

  private def initializeAModelElementClass(): Unit = {
    AModelElementClass._name = "AModelElement"
    AModelElementClass._abstract = true

    AModelElementClass._superTypes ++= Seq(
      AcorePackage.AObjectClass
    )

    AModelElementClass._features ++= Seq(
      AModelElementClass_name_Feature init { x =>
        x._name = "_name"
        x._type = AStringDataType
      })
  }

  private def initializeAObjectClass(): Unit = {
    AObjectClass._name = "AObject"

    AObjectClass._features ++= Seq(
      AObjectClass_class_Feature init { x =>
        x._name = "_class"
        x._type = AClassClass
      },
      AObjectClass_elementName_Feature init { x =>
        x._name = "_elementName"
        x._type = AStringDataType
      },
      AObjectClass_elementPath_Feature init { x =>
        x._name = "_elementPath"
        x._type = AElementPathDataType
        x._derived = true
      },
      AObjectClass_container_Feature init { x =>
        x._name = "_container"
        x._type = AObjectClass
        x._optional = true
      },
      AObjectClass_containmentReference_Feature init { x =>
        x._name = "_containmentReference"
        x._type = AReferenceClass
        x._optional = true
      },
      AObjectClass_endpoint_Feature init { x =>
        x._name = "_endpoint"
        x._type = AActorRefDataType
        x._derived = true
      },
      AObjectClass_contents_Feature init { x =>
        x._name = "_contents"
        x._type = AObjectClass
        x._many = true
        x._derived = true
      })
  }

  private def initializeAClassifierClass(): Unit = {

    AClassifierClass._name = "AClassifier"
    AClassifierClass._abstract = true

    AClassifierClass._superTypes ++= Seq(
      AModelElementClass
    )

    AClassifierClass._features ++= Seq(
      AClassifierClass_package_Feature init { x =>
        x._name = "_package"
        x._type = APackageClass
        x._opposite = Some(APackageClass_classifiers_Feature)
      },
      AClassifierClass_instanceClass_Feature init { x =>
        x._name = "_instanceClass"
        x._type = AClassDataType
      })

  }

  private def initializeAClassClass(): Unit = {
    AClassClass._name = "AClass"

    AClassClass._superTypes ++= Seq(
      AClassifierClass
    )

    AClassClass._features ++= Seq(
      AClassClass_abstract_Feature init { x =>
        x._name = "_abstract"
        x._type = ABooleanDataType
      },
      AClassClass_superTypes_Feature init { x =>
        x._name = "_superTypes"
        x._type = AClassClass
        x._many = true
        x._ordered = true
      },
      AClassClass_features_Feature init { x =>
        x._name = "_features"
        x._type = AFeatureClass
        x._many = true
        x._containment = true
        x._ordered = true
      },
      AClassClass_allFeatures_Feature init { x =>
        x._name = "_allFeatures"
        x._type = AFeatureClass
        x._many = true
        x._derived = true
        x._ordered = true
      },
      AClassClass_references_Feature init { x =>
        x._name = "_references"
        x._type = AReferenceClass
        x._many = true
        x._derived = true
        x._ordered = true
      })

  }

  private def initializeADataTypeClass(): Unit = {
    ADataTypeClass._name = "ADataType"

    ADataTypeClass._superTypes ++= Seq(
      AClassifierClass
    )
  }

  private def initializeAFeatureClass(): Unit = {
    AFeatureClass._name = "AFeature"
    AFeatureClass._abstract = true

    AFeatureClass._superTypes ++= Seq(
      AModelElementClass
    )

    AFeatureClass._features ++= Seq(
      AFeatureClass_type_Feature init { x =>
        x._name = "_type"
        x._type = AReferenceClass
      },
      AFeatureClass_many_Feature init { x =>
        x._name = "_many"
        x._type = ABooleanDataType
      },
      AFeatureClass_derived_Feature init { x =>
        x._name = "_derived"
        x._type = ABooleanDataType
      },
      AFeatureClass_optional_Feature init { x =>
        x._name = "_optional"
        x._type = ABooleanDataType
      },
      AFeatureClass_ordered_Feature init { x =>
        x._name = "_oredered"
        x._type = ABooleanDataType
      },
      AFeatureClass_unique_Feature init { x =>
        x._name = "_unique"
        x._type = ABooleanDataType
      }
    )

  }

  private def initializeAAttributeClass(): Unit = {
    AAttributeClass._name = "AAttribute"

    AAttributeClass._superTypes ++= Seq(
      AFeatureClass
    )
  }

  private def initializeAReferenceClass(): Unit = {
    AReferenceClass._name = "AReference"

    AReferenceClass._superTypes ++= Seq(
      AFeatureClass
    )

    AReferenceClass._features ++= Seq(
      AReferenceClass_containment_Feature init { x =>
        x._name = "_containment"
        x._type = ABooleanDataType
      },
      AReferenceClass_opposite_Feature init { x =>
        x._name = "_opposite"
        x._type = AReferenceClass
        x._optional = true
      }
    )

  }

}
