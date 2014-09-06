package fr.inria.spirals.actress.metamodel

trait AClass extends AClassifier {

  def _abstract: Boolean

  def _superTypes: AMutableSequence[AClass]

  @Containment
  def _features: AMutableSequence[AFeature]

  @Derived
  def _allFeatures: AMutableSequence[AFeature]

  @Derived
  def _references: AMutableSequence[AReference]

}
