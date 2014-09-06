package fr.inria.spirals.actress.acore.util

import fr.inria.spirals.actress.acore.impl.AObjectImpl
import fr.inria.spirals.actress.acore.{AObject, AReference}

import scala.collection.generic.{Growable, Shrinkable}

trait ACollectionContainment[A <: AObject] extends Growable[A] with Shrinkable[A] {

  def containment: (AObject, AReference)

  def preAdd(value: A): Boolean = true

  def postAdd(value: A): Unit = {}

  def preRemove(elem: A): Boolean = true

  def postRemove(elem: A): Unit = {}

  abstract override def +=(elem: A): this.type = {
    if (preAdd(elem)) {
      val _elem = elem.asInstanceOf[AObjectImpl]

      // remove old containment
      _elem.__container.foreach { case (container, feature) =>
        val col = container._get(feature).asInstanceOf[Shrinkable[AObject]]
        col -= elem
      }

      super.+=(elem)

      // set a new containment
      _elem.__container = Some(containment)
      _elem._endpoint = containment._1._endpoint

      // handle bi-directional references
      containment._2._opposite foreach { opposite =>
        assert(!opposite._many)
        _elem._set(opposite, containment._1)
      }

      postAdd(elem)
    }

    this
  }

  abstract override def -=(elem: A): this.type = {
    if (preRemove(elem)) {
      val _elem = elem.asInstanceOf[AObjectImpl]

      // remove containment
      _elem.__container = None
      _elem._endpoint = None

      super.-=(elem)

      // handle bi-directional references
      containment._2._opposite foreach { opposite =>
        assert(!opposite._many)

        // only removes if it contains the container
        if (_elem._get(opposite) != containment._1) _elem._set(opposite, null)
      }

      postRemove(elem)
    }

    this
  }
}
