package fr.inria.spirals.actress.acore.util

import fr.inria.spirals.actress.acore.AObject
import fr.inria.spirals.actress.acore.impl.AObjectImpl

import scala.collection.mutable




abstract class ARuntimeMutableSet[A] extends mutable.AbstractSet[A] with mutable.Set[A] {

  override def size: Int = _iterable.size

  override def -=(x: A): this.type = {
    _del(x)
    this
  }

  override def +=(elem: A): this.type = {
    _add(elem)
    this
  }

  override def iterator: Iterator[A] = {
    // this is the public interface as needs to handle reference containment
    _iterable.iterator
  }

  override def contains(elem: A): Boolean = _iterable.exists(_ == elem)

  protected def _iterable: Iterable[A]

  protected def _add(elem: A): Unit

  protected def _del(elem: A): Unit

}
