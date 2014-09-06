package fr.inria.spirals.actress.acore.impl

import fr.inria.spirals.actress.acore.{AcorePackage, AClass, AReference}

class AReferenceImpl extends AFeatureImpl with AReference {
  override lazy val _class: AClass = AcorePackage.AReferenceClass
  var _containment: Boolean = false
  var _opposite: Option[AReference] = None
}
