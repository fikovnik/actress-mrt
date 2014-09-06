package fr.inria.spirals.actress.acore

trait AFeature extends AModelElement {

  type Type <: AClassifier

  def _type: Type

  def _many: Boolean

  def _derived: Boolean

  def _optional: Boolean

}
