package fr.inria.spirals.actress.acore.impl

import fr.inria.spirals.actress.acore.{AcorePackage, AClass, AModelElement}

abstract class AModelElementImpl extends AObjectImpl with AModelElement {
  override lazy val _class: AClass = AcorePackage.AModelElementClass
  var _name: String = _

  override def _elementName = _name
}
