package fr.inria.spirals.actress.runtime.protocol

import akka.actor.ActorRef
import fr.inria.spirals.actress.metamodel.AObject

case class ElementPathSegment(feature: String, elementName: String) {
  override def toString = feature + ElementPath.Separator + elementName
}

object ElementPathSegment {
  val Root: ElementPathSegment = ElementPathSegment("", "")
}

object ElementPath {

  val Root = ElementPath(Seq(ElementPathSegment.Root))
  val Separator = "/"

  def apply(segment: ElementPathSegment): ElementPath = ElementPath(Seq(segment))

}

case class ElementPath(segments: Seq[ElementPathSegment]) {

  assert(segments.size >= 1)

  def +(that: ElementPathSegment): ElementPath = ElementPath(segments :+ that)

  def path: String = segments mkString ElementPath.Separator

  def head: ElementPathSegment = segments.head

  def tail: Option[ElementPath] = segments match {
    case Seq(x) => None
    case Seq(x, xs@_*) => Some(ElementPath(xs))
  }

  override def toString = path
}

// TODO: Get with selector what to get back

sealed trait Message

sealed trait GetReply

case class Get(elementPath: ElementPath, feature: String) extends Message

case class AttributeValue(name: String, value: Any) extends GetReply

case class UnknownFeature(name: String) extends GetReply

case class UnknownElement(name: String) extends GetReply

case class UnresolvableElementPath(elementPath: ElementPath) extends GetReply

case class Reference(elementPath: ElementPath, endpoint: ActorRef) extends GetReply

case class References(elementPaths: Iterable[Reference]) extends GetReply

case class FwdGet(instance: AObject, elementPath: Option[ElementPath], feature: String, originalSender: ActorRef)