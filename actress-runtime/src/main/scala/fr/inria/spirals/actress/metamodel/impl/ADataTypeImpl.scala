package fr.inria.spirals.actress.metamodel.impl

import actress.core.AcorePackage
import fr.inria.spirals.actress.metamodel.{AClass, ADataType}

class ADataTypeImpl extends AClassifierImpl with ADataType {
  override lazy val _class: AClass = AcorePackage.ADataTypeClass
}
