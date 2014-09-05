package fr.inria.spirals.actress.metamodel

import akka.actor.ActorRef
import fr.inria.spirals.actress.runtime.protocol.{ElementPathSegment, ElementPath}

import scala.collection.mutable


// TODO: trait AObject[T <: AObject]
// TODO: at some point we could have a distinction between immutable and mutable variants (static vs dynamic)

trait AObject {

  // TODO: can this return a type union: AObject, Iterable[AObject], Attribute (whatever will that be)
  def _get(feature: AFeature): Any

  def _class: AClass

  /** Uniquely identifies this instance within its container. */
  def _elementName: String

  /** Uniquely identifies this instance within its actor. */
  @Derived
  def _elementPath: ElementPath = {
    val elementSegment = ElementPathSegment(_containmentFeature map (_._name) getOrElse "", _elementName)
    _container map (_._elementPath + elementSegment) getOrElse ElementPath(elementSegment)
  }

  def _container: Option[AObject]

  def _containmentFeature: Option[AReference]

  def _actor: Option[ActorRef]

  @Derived
  def _contents: Iterable[AObject] = {
    val refs = _class._allFeatures collect { case x: AReference if x._containment => x}

    // it is a reference so we can safely assume that we can cast the result
    refs.collect {
      case r if r._many => _get(r).asInstanceOf[Iterable[AObject]]
      case r => Seq(_get(r).asInstanceOf[AObject])
    }.flatten
  }
}

trait AModelElement extends AObject {

  def _name: String

  def _name_=(v: String)

}

trait APackage extends AModelElement {

  // TODO: this should be a bi-directional reference, currently not supported
  // @Containment(opposite="_package")
  @Containment
  def _classifiers: mutable.Buffer[AClassifier]

  @Derived
  def _classes: Seq[AClass] = _classifiers collect { case c: AClass => c}

  @Derived
  def _dataTypes: Seq[ADataType] = _classifiers collect { case c: ADataType => c}
}

trait AClassifier extends AModelElement {

  // TODO: this should be a bi-directional reference, currently not supported
  // @Containment(opposite="_classifiers")
  def _package: APackage

  def _package_=(v: APackage)

  def _instanceClass: Class[_]

  // TODO: this shouldn't be allowed as it has implications

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
  def _allFeatures: Seq[AFeature] = _superTypes.flatMap(_._allFeatures) ++ _features

  @Derived
  def _references: Seq[AReference] = _features collect { case r: AReference => r}

}

trait AFeature extends AModelElement {

  type T <: AClassifier

  def _type: T

  def _type_=(v: T)

  def _mutable: Boolean

  def _mutable_=(v: Boolean)

  def _many: Boolean

  def _many_=(v: Boolean)

  def _derived: Boolean

  def _derived_=(v: Boolean)

  def _optional: Boolean

  def _optional_=(v: Boolean)

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
  def packages: mutable.Seq[APackage]

}

trait AModelRegistry extends AObject {
  @Containment
  def models: Set[AObject]
}
