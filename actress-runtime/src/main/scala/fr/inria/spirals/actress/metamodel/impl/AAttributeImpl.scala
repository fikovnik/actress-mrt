package fr.inria.spirals.actress.metamodel.impl

import actress.core.AcorePackage
import fr.inria.spirals.actress.metamodel.{AAttribute, AClass}

class AAttributeImpl extends AFeatureImpl with AAttribute {
  override lazy val _class: AClass = AcorePackage.AAttributeClass
}
