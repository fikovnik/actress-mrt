package fr.inria.spirals.actress.metamodel.impl

import actress.core.AcorePackage
import fr.inria.spirals.actress.metamodel.{AClass, AReference}

class AReferenceImpl extends AFeatureImpl with AReference {
  override lazy val _class: AClass = AcorePackage.AReferenceClass
  var _containment: Boolean = false
  var _opposite: Option[AReference] = None
}
