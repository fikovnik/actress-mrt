package fr.inria.spirals.actress.metamodel

trait AClassifier extends AModelElement {

  def _package: APackage

  def _instanceClass: Class[_]

}
