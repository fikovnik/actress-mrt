package fr.inria.spirals.actress.acore

import actress.acore.fr.inria.spirals.acress.acore.AcorePackageImpl

object AcorePackage extends AcorePackageImpl

trait AcorePackage extends APackage {

  val AStringDataType: ADataType
  val ALongDataType: ADataType
  val ABooleanDataType: ADataType
  val AClassDataType: ADataType
  val AElementPathDataType: ADataType
  val AActorRefDataType: ADataType

  val AObjectClass: AClass
  val AModelElementClass: AClass
  val APackageClass: AClass
  val AClassifierClass: AClass
  val AClassClass: AClass
  val ADataTypeClass: AClass
  val AFeatureClass: AClass
  val AAttributeClass: AClass
  val AReferenceClass: AClass

  val AObjectClass_class_Feature: AReference
  val AObjectClass_elementName_Feature: AAttribute
  val AObjectClass_elementPath_Feature: AAttribute
  val AObjectClass_container_Feature: AReference
  val AObjectClass_containmentReference_Feature: AReference
  val AObjectClass_endpoint_Feature: AAttribute
  val AObjectClass_contents_Feature: AReference

  val AModelElementClass_name_Feature: AAttribute

  val APackageClass_classifiers_Feature: AReference
  val APackageClass_classes_Feature: AReference
  val APackageClass_datatypes_Feature: AReference

  val AClassifierClass_package_Feature: AReference
  val AClassifierClass_instanceClass_Feature: AAttribute

  val AClassClass_abstract_Feature: AAttribute
  val AClassClass_superTypes_Feature: AReference
  val AClassClass_features_Feature: AReference
  val AClassClass_allFeatures_Feature: AReference
  val AClassClass_references_Feature: AReference

  val AFeatureClass_type_Feature: AReference
  val AFeatureClass_many_Feature: AAttribute
  val AFeatureClass_derived_Feature: AAttribute
  val AFeatureClass_optional_Feature: AAttribute
  val AFeatureClass_ordered_Feature: AAttribute
  val AFeatureClass_unique_Feature: AAttribute

  val AReferenceClass_containment_Feature: AAttribute
  val AReferenceClass_opposite_Feature: AReference

}
