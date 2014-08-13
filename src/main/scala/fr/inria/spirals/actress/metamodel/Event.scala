package fr.inria.spirals.actress.metamodel

trait Event

case class AttributeChangedEvent[T](val name: String, value: T) extends Event