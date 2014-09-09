package fr.inria.spirals.actress

import scala.collection.mutable

package object acore {

  /** ordered = false, unique = true */
  type AMutableSet[A] = mutable.Set[A]
  /** ordered = true, unique = false */
  type AMutableSequence[A] = mutable.Buffer[A]

  /** ordered = false, unique = false */
//  type AMutableBag[A] =
  /** ordered = true, unique = true */
//  type AOrderedSet[A] =

  implicit class ACollectionLookup[A <: AObject](that: Iterable[A]) {
    def apply(elementName: String): Option[A] = that find (_._elementName == elementName)
  }

  implicit class AObjectInit[T <: AObject](that: T) {
    def init(fun: T => Any): T = {
      fun(that)
      that
    }
  }

}
