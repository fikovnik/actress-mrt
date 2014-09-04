package fr.inria.spirals.actress.metamodel

import java.lang.reflect.Method

import akka.actor.ActorRef
import fr.inria.spirals.actress.runtime.protocol.ElementPath
import fr.inria.spirals.actress.util.Reflection._

import scala.collection.mutable
import scala.reflect._

object APackageImpl {

  // TODO: this should be actually somehow linked to an ActressContext
  // it should not be a singleton
  object Registry extends AObjectImpl with APackageRegistry {

    override lazy val _class: AClass = AcorePackage.APackageRegistryClass

    val packages = mutable.Buffer[APackage]()

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

    override val _elementName = "packages"
  }

}

// TODO: toString

abstract class AObjectImpl extends AObject {
  lazy val _class: AClass = AcorePackage.AObjectClass

  // TODO: this should be moved to some AObjectInternalImpl trait
  // TODO: it should be implemented similarly like _actor
  var _container: Option[AObject] = None

  private var __actor: Option[ActorRef] = _

  def _actor: Option[ActorRef] = __actor

  // TODO: this should be moved to some AObjectInternalImpl trait
  def _actor_=(v: Option[ActorRef]) {
    __actor = v
    _contents map (_.asInstanceOf[AObjectImpl]._actor = v)
  }

  override def _get(feature: AFeature): Any = getClass.allDeclaredMethods.find(_.name == feature._name) match {
      case Some(method) => method.invoke(this)
      case None => sys.error(s"${feature._name}: no such a method in ${this.getClass}")
    }
  }


abstract class AModelElementImpl extends AObjectImpl with AModelElement {
  override lazy val _class: AClass = AcorePackage.AModelElementClass
  var _name: String = _

  override def _elementName = _name
}

class APackageImpl extends AModelElementImpl with APackage {
  APackageImpl.Registry.packages += this

  override lazy val _class: AClass = AcorePackage.APackageClass
  val _classifiers: mutable.Buffer[AClassifier] = mutable.Buffer()

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
      aclass._superTypes ++= superClasses map (APackageImpl.Registry.aClass(_))

      // features
      aclass._features ++= featuresFrom(clazz)
    }

    classifier._package = this
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
          r._type = APackageImpl.Registry.aClass(rawType)
        }

      // 1..1 attribute
      case Seq(rawType) =>
        new AAttributeImpl() init { r =>
          r._type = APackageImpl.Registry.aDataType(rawType)
        }

      // 0..1 reference
      case Seq(IsOptionType(), rawType@IsReferenceType()) =>
        new AReferenceImpl() init { r =>
          r._type = APackageImpl.Registry.aClass(rawType)
          r._optional = true
        }

      // 0..1 attribute
      case Seq(IsOptionType(), rawType) =>
        new AAttributeImpl() init { r =>
          r._type = APackageImpl.Registry.aDataType(rawType)
          r._optional = true
        }

      // 0..* reference
      case Seq(CollectionType(mutable, ordered, unique), rawType@IsReferenceType()) =>
        new AReferenceImpl() init { r =>
          r._type = APackageImpl.Registry.aClass(rawType)
          r._many = true
          r._containment = m.hasAnnotation[Containment]
        }

      // 0..* attributes
      case Seq(CollectionType(mutable, ordered, unique), rawType) =>
        new AAttributeImpl() init { r =>
          r._type = APackageImpl.Registry.aDataType(rawType)
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

  protected object IsOptionType {
    def unapply(clazz: Class[_]): Boolean =
      if (classOf[Option[_]] <:< clazz) true
      else false
  }

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
      else if (classOf[Iterable[_]] <:< clazz) Some((false, true, false))
      else if (classOf[mutable.Buffer[_]] <:< clazz) Some((true, true, false))
      else None
  }

}

abstract class AClassifierImpl extends AModelElementImpl with AClassifier {
  override lazy val _class: AClass = AcorePackage.AClassifierClass
  var _instanceClass: Class[_] = _
  var _package: APackage = _
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
  var _optional: Boolean = false
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
  val AElementPathDataType = new ADataTypeImpl[ElementPath]()

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

  // TODO: this one should not be here
  val AModelRegistryClass = new AClassImpl[AModelRegistry]()

  _name = "acore"

  _classifiers ++= Seq(
    AStringDataType,
    ABooleanDataType,
    AClassDataType,
    AElementPathDataType,
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
    APackageRegistryClass,
    AModelRegistryClass
  )

  _classifiers foreach { c => initAClassifierFrom(c, c._instanceClass)}

  // TODO: fix the containment
  _classifiers foreach (_.asInstanceOf[AObjectImpl]._container = Some(this))
  _container = Some(APackageImpl.Registry)

  override def toString = s"APackageImpl(${_name},${_class},${_classifiers})"
}