package fr.inria.spirals.actress.acore.impl

import fr.inria.spirals.actress.acore._
import fr.inria.spirals.actress.acore.util.AMutableSequence

class APackageImpl extends AModelElementImpl with APackage {

  override lazy val _class: AClass = AcorePackage.APackageClass
  override lazy val _classifiers: AMutableSequence[AClassifier] = AMutableSequence(this, AcorePackage.APackageClass_classifiers_Feature)

  override def toString = s"APackageImpl (_name: ${_name})"
}