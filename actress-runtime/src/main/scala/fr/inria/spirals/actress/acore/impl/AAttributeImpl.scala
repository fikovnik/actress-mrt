package fr.inria.spirals.actress.acore.impl

import fr.inria.spirals.actress.acore.{AcorePackage, AAttribute, AClass}

class AAttributeImpl extends AFeatureImpl with AAttribute {
  override lazy val _class: AClass = AcorePackage.AAttributeClass
}
