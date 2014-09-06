package fr.inria.spirals.actress.metamodel

trait APackage extends AModelElement {

  // TODO: this should be a bi-directional reference, currently not supported
  // @Containment(opposite="_package")
  @Containment
  def _classifiers: AMutableSequence[AClassifier]

  @Derived
  def _classes: AMutableSequence[AClass] = _classifiers collect { case c: AClass => c}

  @Derived
  def _dataTypes: AMutableSequence[ADataType] = _classifiers collect { case c: ADataType => c}
}
