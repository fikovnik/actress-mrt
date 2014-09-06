package fr.inria.spirals.actress.acore.impl

import fr.inria.spirals.actress.acore.{AcorePackage, AClass, AFeature}

abstract class AFeatureImpl extends AModelElementImpl with AFeature {
  override lazy val _class: AClass = AcorePackage.AFeatureClass

  var _type: Type = _
  var _many: Boolean = false
  var _derived: Boolean = false
  var _optional: Boolean = false
}
