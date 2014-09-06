package fr.inria.spirals.actress.acore.util

import fr.inria.spirals.actress.acore._

import scala.collection.mutable

object AMutableSequence {
  def apply[A <: AObject](): AMutableSequence[A] = {
    new mutable.ArrayBuffer[A]
  }

  def apply[A <: AObject](container: AObject, containmentFeature: AReference): AMutableSequence[A] = {
    // TODO: check that containmentFeature is actually a sequence
    new mutable.ArrayBuffer[A] with ACollectionContainment[A] {
      override val containment = (container, containmentFeature)
    }
  }
}



