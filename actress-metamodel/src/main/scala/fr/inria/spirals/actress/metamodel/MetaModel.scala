package fr.inria.spirals.actress.metamodel

import scala.reflect.ClassTag
import scala.reflect.classTag
import scala.collection.mutable.SortedSet

trait MRTStructuralFeature extends Ordered[MRTStructuralFeature] {
  def dataType: ClassTag[_]
  def name: String
  def required: Boolean
  def container: Boolean
  def observable: Boolean
  def modifiable: Boolean
  def unique: Boolean
  def ordered: Boolean

  def compare(that: MRTStructuralFeature) = this.name compare that.name
}

case class MRTAttribute[T: ClassTag](
    name: String,
    required: Boolean,
    container: Boolean,
    observable: Boolean,
    modifiable: Boolean,
    unique: Boolean,
    ordered: Boolean) extends MRTStructuralFeature {

  def dataType = classTag[T]

}

case class MRTReference[T <: MRTClass: ClassTag](
    name: String,
    required: Boolean,
    container: Boolean,
    observable: Boolean,
    modifiable: Boolean,
    unique: Boolean,
    ordered: Boolean) extends MRTStructuralFeature {

  def dataType = classTag[T]

}

trait MRTClass {

  val features = SortedSet[MRTStructuralFeature]()

  def attribute[T: ClassTag](
    name: String,
    required: Boolean = true,
    container: Boolean = false,
    observable: Boolean = false,
    modifiable: Boolean = false,
    unique: Boolean = false,
    ordered: Boolean = true) {

    features += MRTAttribute[T](
      name,
      required = required,
      container = container,
      observable = observable,
      modifiable = modifiable,
      unique = unique,
      ordered = ordered
    )

  }

  def reference[T <: MRTClass: ClassTag](
    name: String,
    required: Boolean = true,
    container: Boolean = false,
    observable: Boolean = false,
    modifiable: Boolean = false,
    unique: Boolean = false,
    ordered: Boolean = true) {

    features += MRTReference[T](
      name,
      required = required,
      container = container,
      observable = observable,
      modifiable = modifiable,
      unique = unique,
      ordered = ordered
    )

  }

}