package fr.inria.spirals.actress.metamodel

import fr.inria.spirals.actress.util.Reflection._

import scala.reflect.{ClassTag, classTag}

trait AObject {

  def _class: AClass

}

trait AModelElement extends AObject {

  def _name: String

}

trait AClassifier extends AModelElement

trait ADataType extends AClassifier

trait AClass extends AClassifier {

  def _abstract: Boolean

  @Containment
  def _features: Seq[AFeature]

  @Derived
  def _references: Seq[AReference] = _features collect { case r: AReference => r}

  def _superTypes: Seq[AClass]

}

trait AFeature extends AModelElement {

  def _type: AClassifier

  def _mutable: Boolean

  def _many: Boolean

  def _derived: Boolean

}

trait AReference extends AFeature {

  def _containment: Boolean

  override def _type: AClass

}

trait AAttribute extends AFeature

object APackage {

  object registry {

    private val dataTypes = collection.mutable.Map[String, ADataType]()
    private val classes = collection.mutable.Map[String, AClass]()

    dataTypes ++= ActressPackage._classifiers collect { case a: ADataType => a._name -> a}
    classes ++= ActressPackage._classifiers collect { case a: AClass => a._name -> a}

    protected object IsReferenceType {
      def unapply(clazz: Class[_]): Boolean =
        if (classOf[AClass] <:< clazz) true
        else false
    }

    protected object CollectionType {
      def unapply(clazz: Class[_]): Option[(Boolean, Boolean, Boolean)] =
        if (classOf[Set[_]] <:< clazz) Some((false, false, true))
        else if (classOf[collection.mutable.Set[_]] <:< clazz) Some((true, false, true))
        else None
    }

    def aDataType(clazz: Class[_]): ADataType = {
      val name = clazz.name
      dataTypes getOrElseUpdate(name, clazz match {
        case IsReferenceType() => throw new IllegalArgumentException(s"$clazz: is an AClass")
        case _ => ADataTypeImpl(name)
      })
    }

    // FIXME: this is broken as it will lookup the same class multiple times
    // class A extends AClass {
    //   def ref: A
    // }
    
    def aClass(clazz: Class[_ <: AClass]): AClass = classes getOrElseUpdate(clazz.name, loadAClass(clazz))

    def aClass[T <: AClass : ClassTag]: AClass = aClass(classTag[T].runtimeClass.asInstanceOf[Class[AClass]])

    protected def loadAClass(clazz: Class[_ <: AClass]): AClassImpl = {
      // TODO: handle abstraction
      // TODO: handle super classes
      val candidates = clazz.declaredMethods


      // TODO: 0..1 reference
      // TODO: 0..1 attribute
      // TODO: mutable
      val features =
        for (m <- candidates) yield m.resolveGenericReturnType match {
          // 1..1 reference
          case Seq(rawType@IsReferenceType()) =>
            val _type = aClass(rawType.asInstanceOf[Class[AClass]])
            APackage.AReferenceImpl(m.name, _type)
          //        f copy (mutable = hasSetter(f))

          // 1..1 attribute
          case Seq(rawType) =>
            val _type = aDataType(rawType)
            APackage.AAttributeImpl(m.name, _type)
          //        f copy (mutable = hasSetter(f))

          // 0..* reference
          case Seq(CollectionType(mutable, ordered, unique), rawType@IsReferenceType()) =>
            val _type = aClass(rawType.asInstanceOf[Class[AClass]])
            APackage.AReferenceImpl(m.name, _type, _many = true, _containment = m.hasAnnotation[Containment])

          // 0..* attributes
          case Seq(CollectionType(mutable, ordered, unique), rawType) =>
            val _type = aDataType(rawType)
            APackage.AAttributeImpl(m.name, _type, _many = true)

          case _ =>
            throw new IllegalArgumentException(s"$m.name: unsupported return type")
        }

      // FIXME: it should be .simpleName once we start using packages
      APackage.AClassImpl(clazz.name, _features = features)
    }

  }

  case class ADataTypeImpl(_name: String) extends ADataType {
    lazy val _class = ActressPackage.ADataTypeClass
  }

  case class AAttributeImpl(_name: String, _type: ADataType, _mutable: Boolean = false, _many: Boolean = false, _derived: Boolean = false) extends AAttribute {
    lazy val _class = ActressPackage.AAttributeClass
  }

  case class AReferenceImpl(_name: String, _type: AClass, _mutable: Boolean = false, _many: Boolean = false, _derived: Boolean = false, _containment: Boolean = false) extends AReference {
    require(!_derived || !_containment, s"${_name}: derived feature cannot be contained")

    lazy val _class = ActressPackage.AReferenceClass
  }

  case class AClassImpl(_name: String, _abstract: Boolean = false, _superTypes: Seq[AClass] = Seq(), _features: Seq[AFeature] = Seq()) extends AClass {
    lazy val _class = ActressPackage.AClassClass
  }

  abstract class AbstractPackageImpl(val _name: String) extends APackage {
    lazy val _class = ActressPackage.APackageClass
  }

}

trait APackage extends AModelElement {

  @Containment
  def _classifiers: Seq[AClassifier]

  @Derived
  def _classes: Seq[AClass] = _classifiers collect { case c: AClass => c}

}

object ActressPackage extends APackage {

  import fr.inria.spirals.actress.metamodel.APackage._

  lazy val AString = ADataTypeImpl("string")
  lazy val ABoolean = ADataTypeImpl("boolean")

  lazy val AModelElementClass: AClass = AClassImpl("AModelElement",
    _abstract = true,
    _features = Seq(
      AAttributeImpl("_name", AString)
    ))

  lazy val APackageClass: AClass = AClassImpl("APackage",
    _superTypes = Seq(AModelElementClass),
    _features = Seq(
      AReferenceImpl("_classifiers", AReferenceClass, _many = true, _containment = true),
      AReferenceImpl("_classifiers", AReferenceClass, _many = true, _derived = true)
    ))

  lazy val AClassifierClass: AClass = AClassImpl("AClassifier",
    _abstract = true,
    _superTypes = Seq(AModelElementClass))

  lazy val AClassClass: AClass = new AClassImpl("AClass", _superTypes = Seq(AClassifierClass)) {
    override val _features = Seq(
      AAttributeImpl("_abstract", ABoolean),
      AReferenceImpl("_superTypes", this, _many = true),
      AReferenceImpl("_features", AFeatureClass, _many = true, _containment = true),
      AReferenceImpl("_references", AFeatureClass, _many = true, _derived = true)
    )

    // We cannot print the full _features as this will be recursive!
    override val toString = s"AClass(${_name},${_abstract},${_superTypes},${_features map (_._name)}})"
  }

  lazy val ADataTypeClass: AClass = AClassImpl("ADataType",
    _superTypes = Seq(AClassifierClass))

  lazy val AFeatureClass: AClass = AClassImpl("AFeature",
    _superTypes = Seq(AModelElementClass),
    _features = Seq(
      AReferenceImpl("_type", AClassifierClass),
      AAttributeImpl("_mutable", ABoolean),
      AAttributeImpl("_many", ABoolean),
      AAttributeImpl("_derived", ABoolean)
    ))

  lazy val AAttributeClass: AClass = AClassImpl("AAttribute",
    _superTypes = Seq(AFeatureClass))

  lazy val AReferenceClass: AClass = AClassImpl("AAttribute",
    _superTypes = Seq(AFeatureClass),
    _features = Seq(
      AAttributeImpl("_containment", ABoolean)))

  val _classifiers = Seq(
    AString,
    ABoolean,
    AModelElementClass,
    APackageClass,
    AClassifierClass,
    AClassClass,
    ADataTypeClass,
    AFeatureClass,
    AAttributeClass,
    AReferenceClass
  )

  val _name = "actress"

  lazy val _class = APackageClass

  override def toString = s"APackage(${_name},${_class},${_classifiers})"
}

trait APackageRegistry extends AClass {

  def packages: collection.mutable.Seq[APackage]

}