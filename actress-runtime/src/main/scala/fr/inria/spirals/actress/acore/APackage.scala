package fr.inria.spirals.actress.acore

trait APackage extends AModelElement {

  @Containment
  def _classifiers: AMutableSequence[AClassifier]

  @Derived
  def _classes: AMutableSequence[AClass] = _classifiers collect { case c: AClass => c}

  @Derived
  def _dataTypes: AMutableSequence[ADataType] = _classifiers collect { case c: ADataType => c}
}
