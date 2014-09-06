package fr.inria.spirals.actress.metamodel

trait AReference extends AFeature {

  type Type = AClass

  def _containment: Boolean

  def _opposite: Option[AReference]

}