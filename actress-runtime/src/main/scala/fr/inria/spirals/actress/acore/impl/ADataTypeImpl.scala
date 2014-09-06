package fr.inria.spirals.actress.acore.impl

import fr.inria.spirals.actress.acore.{AcorePackage, AClass, ADataType}

class ADataTypeImpl extends AClassifierImpl with ADataType {
  override lazy val _class: AClass = AcorePackage.ADataTypeClass
}
