package fr.inria.spirals.actress

import scala.collection.mutable

package object metamodel {

  type ASequence[A] = Seq[A]
  type AMutableSequence[A] = mutable.Buffer[A]

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
