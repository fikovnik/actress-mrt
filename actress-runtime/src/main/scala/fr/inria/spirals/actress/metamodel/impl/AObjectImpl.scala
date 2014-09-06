package fr.inria.spirals.actress.metamodel.impl

import actress.core.AcorePackage
import akka.actor.ActorRef
import fr.inria.spirals.actress.metamodel._
import fr.inria.spirals.actress.runtime.protocol.{ElementPath, ElementPathSegment}
import fr.inria.spirals.actress.util.Reflection._

abstract class AObjectImpl extends AObject {

  override lazy val _class: AClass = AcorePackage.AObjectClass

  /** Uniquely identifies this instance within its actor. */
  override def _elementPath: ElementPath = {
    val elementSegment = ElementPathSegment(_containmentReference map (_._name) getOrElse "", _elementName)
    _container map (_._elementPath + elementSegment) getOrElse ElementPath(elementSegment)
  }

  override def _contents: AMutableSequence[AObject] = {
      val refs = _class._allFeatures collect { case x: AReference if x._containment => x}

      // it is a reference so we can safely assume that we can cast the result
      refs.collect {
        case r if r._many => _get(r).asInstanceOf[Iterable[AObject]]
        case r => Seq(_get(r).asInstanceOf[AObject])
      }.flatten
  }

  var __container: Option[(AObject, AReference)] = None
  override def _container: Option[AObject] = __container map (_._1)
  override def _containmentReference: Option[AReference] = __container map (_._2)

  private var __endpoint: Option[ActorRef] = _
  override def _endpoint: Option[ActorRef] = __endpoint
  def _endpoint_=(v: Option[ActorRef]) {
    __endpoint = v
    _contents map (_.asInstanceOf[AObjectImpl]._endpoint = v)
  }

  override def _get(feature: AFeature): Any = getClass.allDeclaredMethods.find(_.name == feature._name) match {
    case Some(method) => method.invoke(this)
    case None => sys.error(s"${feature._name}: no such a method in ${this.getClass}")
  }

  override def _set(feature: AFeature, value: AnyRef): Unit = getClass.allDeclaredMethods.find(_.name == feature._name+"_$eq") match {
    case Some(method) => method.invoke(this, value)
    case None => sys.error(s"${feature._name}: no such a method in ${this.getClass}")
  }

  override def toString = s"AObjectImpl (_elementName: ${_elementName}, _endpoint: ${_endpoint})"
}
