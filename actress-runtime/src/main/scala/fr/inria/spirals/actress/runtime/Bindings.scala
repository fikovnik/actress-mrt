package fr.inria.spirals.actress.runtime

import fr.inria.spirals.actress.metamodel.MRTClass

trait Binding[T <: MRTClass]

trait BindingFactory[T <: MRTClass, U <: Binding[T]] extends (Option[String] => U)
