package fr.inria.spirals.actress.metamodel

import akka.actor.ActorRef
import fr.inria.spirals.actress.runtime.protocol.{ElementPathSegment, ElementPath}

trait AObject {

  def _class: AClass

  /** Uniquely identifies this instance within its container. */
  def _elementName: String

  /** Uniquely identifies this instance within its actor. */
  @Derived
  def _elementPath: ElementPath

  def _container: Option[AObject]

  def _containmentReference: Option[AReference]

  def _endpoint: Option[ActorRef]

  @Derived
  def _contents: AMutableSequence[AObject]

  /**
   * @return AObject, Iterable[AObject] or Any depending on the feature type
   */
  def _get(feature: AFeature): Any

  def _set(feature: AFeature, value: AnyRef): Unit
}
