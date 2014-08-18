package fr.inria.spirals.actress.metamodel

import java.lang.reflect.Method

import akka.actor.ActorRef
import fr.inria.spirals.actress.util.Reflection._

import scala.collection.mutable
import scala.reflect.{ClassTag, classTag}


trait AObject {
  def _get(feature: AFeature): Any

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
  def _classes: Seq[AClass] = _classifiers collect { case c: AClass => c}

  @Derived
  def _dataTypes: Seq[ADataType] = _classifiers collect { case c: ADataType => c}
}

sealed trait AClassifier extends AModelElement {

  def _instanceClass: Class[_]

  def _instanceClass_=(clazz: Class[_])

}

trait ADataType extends AClassifier

trait AClass extends AClassifier {

  def _abstract: Boolean

  def _abstract_=(v: Boolean)

  def _superTypes: mutable.Buffer[AClass]

  @Containment
  def _features: mutable.Buffer[AFeature]

  @Derived
  def _references: Seq[AReference] = _features collect { case r: AReference => r}

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

trait APackageRegistry extends AObject {

  @Containment
  def packages: collection.mutable.Seq[APackage]

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
    classifier._instanceClass = clazz

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


  object registry extends AObjectImpl with APackageRegistry {

    override lazy val _class: AClass = AcorePackage.APackageRegistryClass

    var packages = mutable.Buffer[APackage]()

    def allClassifiers = packages flatMap (_._classifiers)

    def allDataTypes = packages flatMap (_._dataTypes)

    def allClasses = packages flatMap (_._classes)

    def classifierForInstanceClass(clazz: Class[_]): Option[AClassifier] = allClassifiers find (_._instanceClass == clazz)

    def aDataType(clazz: Class[_]): ADataType = classifierForInstanceClass(clazz) match {
      case Some(dt: ADataType) => dt
      case Some(_) => throw new IllegalArgumentException(s"$clazz: is not an ADataType, but an AClass")
      case None => throw new IllegalArgumentException(s"$clazz: could not be found in any package ($packages)")
    }

    def aClass(clazz: Class[_]): AClass = classifierForInstanceClass(clazz) match {
      case Some(c: AClass) => c
      case Some(_) => throw new IllegalArgumentException(s"$clazz: is not an AClass, but an ADataType")
      case None => throw new IllegalArgumentException(s"$clazz: could not be found in any package ($packages)")
    }

    def aClass[T <: AClass : ClassTag]: AClass = aClass(classTag[T].runtimeClass.asInstanceOf[Class[AClass]])

  }

}

// TODO: toString

class AObjectImpl extends AObject {
  lazy val _class: AClass = AcorePackage.AObjectClass
  var _container: AObject = _
  var _actor: ActorRef = _

  override def _get(feature: AFeature): Any = {
    getClass.declaredMethods.find(_.name == feature._name).get.invoke(this)
  }
}

abstract class AModelElementImpl extends AObjectImpl with AModelElement {
  override lazy val _class: AClass = AcorePackage.AModelElementClass
  var _name: String = _
}

class APackageImpl extends AModelElementImpl with APackage {
  APackage.registry.packages += this

  override lazy val _class: AClass = AcorePackage.APackageClass
  val _classifiers: mutable.Buffer[AClassifier] = mutable.Buffer()
}

abstract class AClassifierImpl extends AModelElementImpl with AClassifier {
  override lazy val _class: AClass = AcorePackage.AClassifierClass
  var _instanceClass: Class[_] = _
}

class ADataTypeImpl[T: ClassTag] extends AClassifierImpl with ADataType {
  _instanceClass = classTag[T].runtimeClass

  override lazy val _class: AClass = AcorePackage.ADataTypeClass
}

class AClassImpl[T <: AObject : ClassTag] extends AClassifierImpl with AClass {
  _instanceClass = classTag[T].runtimeClass

  override lazy val _class: AClass = AcorePackage.AClassClass
  var _abstract: Boolean = false
  val _superTypes: mutable.Buffer[AClass] = mutable.Buffer()
  val _features: mutable.Buffer[AFeature] = mutable.Buffer()
}

abstract class AFeatureImpl extends AModelElementImpl with AFeature {
  override lazy val _class: AClass = AcorePackage.AFeatureClass
  var _mutable: Boolean = false
  var _many: Boolean = false
  var _derived: Boolean = false
}

class AAttributeImpl extends AFeatureImpl with AAttribute {
  override lazy val _class: AClass = AcorePackage.AAttributeClass
  var _type: ADataType = _
}

class AReferenceImpl extends AFeatureImpl with AReference {
  override lazy val _class: AClass = AcorePackage.AReferenceClass
  var _type: AClass = _
  var _containment: Boolean = false
}

object AcorePackage extends APackageImpl {

  val AStringDataType = new ADataTypeImpl[String]()
  val ABooleanDataType = new ADataTypeImpl[Boolean]()
  val AClassDataType = new ADataTypeImpl[Class[_]]()

  // TODO: temporary
  val AActorRefDataType = new ADataTypeImpl[ActorRef]()

  val AObjectClass = new AClassImpl[AObject]()
  val AModelElementClass = new AClassImpl[AModelElement]()
  val APackageClass = new AClassImpl[APackage]()
  val AClassifierClass = new AClassImpl[AClassifier]()
  val AClassClass = new AClassImpl[AClass]()
  val ADataTypeClass = new AClassImpl[ADataType]()
  val AFeatureClass = new AClassImpl[AFeature]()
  val AAttributeClass = new AClassImpl[AAttribute]()
  val AReferenceClass = new AClassImpl[AReference]()
  val APackageRegistryClass = new AClassImpl[APackageRegistry]()

  _name = "acore"
  _classifiers ++= Seq(
    AStringDataType,
    ABooleanDataType,
    AClassDataType,
    AActorRefDataType,

    AObjectClass,
    AModelElementClass,
    APackageClass,
    AClassifierClass,
    ADataTypeClass,
    AClassClass,
    AFeatureClass,
    AAttributeClass,
    AReferenceClass,
    APackageRegistryClass
  )

  _classifiers foreach { c => APackage.initAClassifierFrom(c, c._instanceClass)}

  override def toString = s"APackageImpl(${_name},${_class},${_classifiers})"
}