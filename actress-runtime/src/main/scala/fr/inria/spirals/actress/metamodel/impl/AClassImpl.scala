package fr.inria.spirals.actress.metamodel.impl

import actress.core.AcorePackage
import fr.inria.spirals.actress.metamodel._
import fr.inria.spirals.actress.metamodel.util.AMutableSequence

class AClassImpl extends AClassifierImpl with AClass {
  override lazy val _class: AClass = AcorePackage.AClassClass
  var _abstract = false

  override def _allFeatures: AMutableSequence[AFeature] = _superTypes.flatMap(_._allFeatures) ++ _features

  override def _references: AMutableSequence[AReference] = _features collect { case r: AReference => r}

  override lazy val _superTypes: AMutableSequence[AClass] = AMutableSequence()
  override lazy val _features: AMutableSequence[AFeature] = AMutableSequence(this, AcorePackage.AClassClass_features_Feature)
}
