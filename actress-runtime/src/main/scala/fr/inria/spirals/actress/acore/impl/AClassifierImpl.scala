package fr.inria.spirals.actress.acore.impl

import fr.inria.spirals.actress.acore.{AcorePackage, AClass, AClassifier, APackage}

abstract class AClassifierImpl extends AModelElementImpl with AClassifier {
  override lazy val _class: AClass = AcorePackage.AClassifierClass
  var _instanceClass: Class[_] = _

  private var __package: APackage = _
  override def _package: APackage = __package
  def _package_=(v: APackage) = {
    if (v != __package) {
      val old = __package
      __package = v
      // handle bi-directional reference
      if (old != null) old._classifiers -= this
    }
  }
}