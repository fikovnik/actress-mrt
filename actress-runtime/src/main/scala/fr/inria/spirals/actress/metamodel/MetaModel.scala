package fr.inria.spirals.actress.metamodel

import java.lang.reflect.Method

import akka.actor.ActorRef
import fr.inria.spirals.actress.util.Reflection._

import scala.collection.mutable
import scala.reflect.{ClassTag, classTag}


trait AObject {

  def _class: AClass

  def _container: AObject

  def _container_=(v: AObject)

  def _actor: ActorRef

  def _actor_=(v: ActorRef)

}

sealed trait AModelElement extends AObject {

  def _name: String

  def _name_=(v: String)

}

trait APackage extends AModelElement {

  @Containment
  def _classifiers: mutable.Buffer[AClassifier]

  @Derived
  def _classes: mutable.Buffer[AClass] = _classifiers collect { case c: AClass => c}

}

sealed trait AClassifier extends AModelElement

trait ADataType extends AClassifier

trait AClass extends AClassifier {

  def _abstract: Boolean

  def _abstract_=(v: Boolean)

  def _superTypes: mutable.Buffer[AClass]

  @Containment
  def _features: mutable.Buffer[AFeature]

  @Derived
  def _references: mutable.Buffer[AReference] = _features collect { case r: AReference => r}

}

sealed trait AFeature extends AModelElement {

  type T <: AClassifier

  def _type: T

  def _type_=(v: T)

  def _mutable: Boolean

  def _mutable_=(v: Boolean)

  def _many: Boolean

  def _many_=(v: Boolean)

  def _derived: Boolean

  def _derived_=(v: Boolean)

}

trait AAttribute extends AFeature {

  type T = ADataType

}

trait AReference extends AFeature {

  type T = AClass

  def _containment: Boolean

  def _containment_=(v: Boolean)

}


object APackage {

  private implicit class AObjectInit[T <: AObject](that: T) {
    def init(fun: T => Any): T = {
      fun(that)
      that
    }
  }


  def initAClassifierFrom[T <: AClassifier](classifier: T, clazz: Class[_]): T = {
    def initADataTypeFrom(dataType: ADataType, clazz: Class[_]): Unit = {
      
    }

    def initAClassFrom(aclass: AClass, clazz: Class[_]) {
      // TODO: handle abstraction - need annotation for that

      // superclasses
      val superClasses = clazz.allSuperClasses
      assert(
        superClasses forall { c => IsReferenceType.unapply(c)},
        s"All super classes of the given class $classifier should be AObjects. At least one is not $superClasses"
      )
      aclass._superTypes ++= superClasses map (registry.aClass(_))

      // features
      aclass._features ++= featuresFrom(clazz)
    }

    classifier._name = clazz.simpleName

    classifier match {
      case dt: ADataType => initADataTypeFrom(dt, clazz)
      case ac: AClass => initAClassFrom(ac, clazz)
    }

    classifier
  }

  def featureFrom(m: Method): AFeature = {
    // TODO: 0..1 reference
    // TODO: 0..1 attribute
    // TODO: assert m is a feature

    val setter = m.declaringClass.declaredMethods.find { that =>
      that.name == m.name + "_$eq" &&
        that.parameterTypes.size == 1 &&
        that.parameterTypes(0) == m.returnType &&
        that.returnType == classOf[Unit]
    }

    // TODO: update extractors
    val feature = m.resolveGenericReturnType match {
      // 1..1 reference
      case Seq(rawType@IsReferenceType()) =>
        new AReferenceImpl() init { r =>
          r._type = registry.aClass(rawType)
        }

      // 1..1 attribute
      case Seq(rawType) =>
        new AAttributeImpl() init { r =>
          r._type = registry.aDataType(rawType)
        }

      // 0..* reference
      case Seq(CollectionType(mutable, ordered, unique), rawType@IsReferenceType()) =>
        new AReferenceImpl() init { r =>
          r._type = registry.aClass(rawType)
          r._many = true
          r._containment = m.hasAnnotation[Containment]
        }

      // 0..* attributes
      case Seq(CollectionType(mutable, ordered, unique), rawType) =>
        new AAttributeImpl() init { r =>
          r._type = registry.aDataType(rawType)
          r._many = true
        }

      case _ =>
        throw new IllegalArgumentException(s"$m.name: unsupported return type")
    }

    feature._name = m.name
    feature._mutable = setter.isDefined
    feature
  }

  def featuresFrom(clazz: Class[_]): Seq[AFeature] =
    clazz.declaredMethods
      .filter(_.parameterTypes.size == 0)
      .map(featureFrom)

  protected object IsReferenceType {
    def unapply(clazz: Class[_]): Boolean =
      if (classOf[AObject] <:< clazz) true
      else false
  }

  protected object CollectionType {
    /**
     * (mutable, ordered, unique)
     */
    def unapply(clazz: Class[_]): Option[(Boolean, Boolean, Boolean)] =
    // TODO: immutable ordered and unique - ListSet
    // TODO: mutable ordered and unique - ???
      if (classOf[Set[_]] <:< clazz) Some((false, false, true))
      else if (classOf[mutable.Set[_]] <:< clazz) Some((true, false, true))
      else if (classOf[Seq[_]] <:< clazz) Some((false, true, false))
      else if (classOf[mutable.Buffer[_]] <:< clazz) Some((true, true, false))
      else None
  }


  object registry {

    private val dataTypes = collection.mutable.Map[String, ADataType]()
    private val classes = collection.mutable.Map[String, AClass]()

    dataTypes ++= ActressPackage._classifiers collect { case a: ADataType => a._name -> a}
    classes ++= ActressPackage._classifiers collect { case a: AClass => a._name -> a}

    def aDataType(clazz: Class[_]): ADataType = dataTypes getOrElseUpdate(clazz.name, clazz match {
      case IsReferenceType() => throw new IllegalArgumentException(s"$clazz: is an attribute compatible datatype")
      case _ => initAClassifierFrom(new ADataTypeImpl(), clazz)
    })


    def aClass(clazz: Class[_]): AClass = classes getOrElse(clazz.name, {
      val aclass = new AClassImpl()
      classes += (clazz.name -> aclass)

      initAClassifierFrom(aclass, clazz)
    })

    def aClass[T <: AClass : ClassTag]: AClass = aClass(classTag[T].runtimeClass.asInstanceOf[Class[AClass]])

  }

}

// TODO: toString

class AObjectImpl extends AObject {
  lazy val _class: AClass = ActressPackage.AObjectClass
  var _container: AObject = _
  var _actor: ActorRef = _
}

abstract class AModelElementImpl extends AObjectImpl with AModelElement {
  override lazy val _class: AClass = ActressPackage.AModelElementClass
  var _name: String = _
}

class APackageImpl extends AModelElementImpl with APackage {
  override lazy val _class: AClass = ActressPackage.APackageClass
  val _classifiers: mutable.Buffer[AClassifier] = mutable.Buffer()
}

abstract class AClassifierImpl extends AModelElementImpl with AClassifier {
  override lazy val _class: AClass = ActressPackage.AClassifierClass
}

class ADataTypeImpl extends AClassifierImpl with ADataType {
  override lazy val _class: AClass = ActressPackage.ADataTypeClass
}

class AClassImpl extends AClassifierImpl with AClass {
  override lazy val _class: AClass = ActressPackage.AClassClass
  var _abstract: Boolean = false
  val _superTypes: mutable.Buffer[AClass] = mutable.Buffer()
  val _features: mutable.Buffer[AFeature] = mutable.Buffer()
}

abstract class AFeatureImpl extends AModelElementImpl with AFeature {
  override lazy val _class: AClass = ActressPackage.AFeatureClass
  var _mutable: Boolean = false
  var _many: Boolean = false
  var _derived: Boolean = false
}

class AAttributeImpl extends AFeatureImpl with AAttribute {
  override lazy val _class: AClass = ActressPackage.AAttributeClass
  var _type: ADataType = _
}

class AReferenceImpl extends AFeatureImpl with AReference {
  override lazy val _class: AClass = ActressPackage.AReferenceClass
  var _type: AClass = _
  var _containment: Boolean = false
}

object ActressPackage extends APackageImpl {

  val AString = new ADataTypeImpl()
  val ABoolean = new ADataTypeImpl()

  val AObjectClass = new AClassImpl()
  val AModelElementClass = new AClassImpl()
  val APackageClass = new AClassImpl()
  val AClassifierClass = new AClassImpl()
  val AClassClass = new AClassImpl()
  val ADataTypeClass = new AClassImpl()
  val AFeatureClass = new AClassImpl()
  val AAttributeClass = new AClassImpl()
  val AReferenceClass = new AClassImpl()

  private val content = Seq(
    AString -> classOf[String],
    ABoolean -> classOf[Boolean],

    AObjectClass -> classOf[AObject],
    AModelElementClass -> classOf[AModelElement],
    APackageClass -> classOf[APackage],
    AClassifierClass -> classOf[AClassifier],
    ADataTypeClass -> classOf[ADataType],
    AClassClass -> classOf[AClass],
    AFeatureClass -> classOf[AFeature],
    AAttributeClass -> classOf[AAttribute],
    AReferenceClass -> classOf[AReference]
  )

  _name = "acore"
  _classifiers ++= content map (_._1)

  content foreach { c => APackage.initAClassifierFrom(c._1, c._2)}

  override def toString = s"APackageImpl(${_name},${_class},${_classifiers})"
}

trait APackageRegistry extends AClass {

  def packages: collection.mutable.Seq[APackage]

}