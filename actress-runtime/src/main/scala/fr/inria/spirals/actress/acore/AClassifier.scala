package fr.inria.spirals.actress.acore

trait AClassifier extends AModelElement {

  def _package: APackage

  def _instanceClass: Class[_]

}
