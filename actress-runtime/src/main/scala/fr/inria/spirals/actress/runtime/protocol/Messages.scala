package fr.inria.spirals.actress.runtime.protocol

import akka.actor.ActorRef
import fr.inria.spirals.actress.metamodel.AObject

object ElementPath {
  val Root = ElementPath(Seq())
  val Separator = "/"

  def apply(segments: Seq[String]) = new ElementPath(segments)
  def apply(segment1: String, segments: String*) = new ElementPath(segment1 +: segments.toSeq)
}

class ElementPath(val segments: Seq[String]) {

  def path: String = segments mkString (ElementPath.Separator, ElementPath.Separator, "")

  def head: Option[String] = segments.headOption

  def tail: Option[ElementPath] = segments match {
    case Seq() => None
    case Seq(x) => None
    case Seq(x, xs@_*) => Some(ElementPath(xs))
  }

  def child(elementName: String): ElementPath = ElementPath(segments :+ elementName)

  override def toString = path
}

sealed trait Message

sealed trait GetReply

case class Get(elementPath: ElementPath, feature: String) extends Message

case class AttributeValue(name: String, value: Any) extends GetReply

case class UnknownAttribute(name: String) extends GetReply

case class Reference(elementPath: ElementPath, endpoint: ActorRef) extends GetReply

case class References(elementPaths: Iterable[Reference]) extends GetReply

case class FwdGet(originalSender: ActorRef, instance: AObject, elementPath: Option[ElementPath], feature: String)