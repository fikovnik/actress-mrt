package fr.inria.spirals.actress.metamodel

trait Channel {

  def emit(e: Event): Unit
  
}