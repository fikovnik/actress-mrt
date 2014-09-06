package fr.inria.spirals.actress.metamodel.impl

import actress.core.AcorePackage
import fr.inria.spirals.actress.metamodel.{AClass, AModelElement}

abstract class AModelElementImpl extends AObjectImpl with AModelElement {
  override lazy val _class: AClass = AcorePackage.AModelElementClass
  var _name: String = _

  override def _elementName = _name
}
