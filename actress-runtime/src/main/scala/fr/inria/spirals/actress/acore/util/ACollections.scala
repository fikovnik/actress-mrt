package fr.inria.spirals.actress.acore.util

import fr.inria.spirals.actress.acore._

import scala.collection.mutable


object AMutableSet {
  def apply[A <: AObject](): AMutableSet[A] = {
    new mutable.HashSet[A]
  }

  def apply[A <: AObject](container: AObject, containmentFeature: AReference): AMutableSet[A] = {
    // TODO: check that containmentFeature is actually a sequence
    new mutable.HashSet[A] with ReferenceContainment[A] {
      override val containmentPair = ContainmentPair(container, containmentFeature)
    }
  }
}

object AMutableSequence {
  def apply[A <: AObject](): AMutableSequence[A] = {
    new mutable.ArrayBuffer[A]
  }

  def apply[A <: AObject](container: AObject, containmentFeature: AReference): AMutableSequence[A] = {
    // TODO: check that containmentFeature is actually a sequence
    new mutable.ArrayBuffer[A] with ReferenceContainment[A] {
      override val containmentPair = ContainmentPair(container, containmentFeature)
    }
  }
}


