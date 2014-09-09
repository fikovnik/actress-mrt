package fr.inria.spirals.actress.acore.util

import fr.inria.spirals.actress.acore.AObject
import fr.inria.spirals.actress.acore.impl.AObjectImpl

import scala.collection.generic.{Growable, Shrinkable}

trait ReferenceContainment[A <: AObject] extends Growable[A] with Shrinkable[A] {

  def containmentPair: ContainmentPair

  abstract override def +=(elem: A): this.type = {
    val ret = super.+=(elem)

    val _elem = elem.asInstanceOf[AObjectImpl]

    // remove old containment
    for (c <- _elem.__containmentPair) {
      // get the containing collection
      val xs = c.container._get(c.reference).asInstanceOf[Shrinkable[AObject]]
      xs -= elem
    }

    // set a new containment
    _elem.__containmentPair = Some(containmentPair)

    // handle bi-directional references
    for (opposite <- containmentPair.reference._opposite) {
      assert(!opposite._many)
      _elem._set(opposite, containmentPair.container)
    }

    ret
  }

  abstract override def -=(elem: A): this.type = {
    val ret = super.-=(elem)

    val _elem = elem.asInstanceOf[AObjectImpl]

    // remove containment
    _elem.__containmentPair = None

    // handle bi-directional references
    for (opposite <- containmentPair.reference._opposite) {
      assert(!opposite._many)
      // only removes if it contains the container
      if (_elem._get(opposite) != containmentPair.container) _elem._set(opposite, null)
    }

    ret
  }
}
