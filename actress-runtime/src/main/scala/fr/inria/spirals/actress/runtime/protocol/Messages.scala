package fr.inria.spirals.actress.runtime.protocol

import akka.actor.ActorRef

sealed trait Message

sealed trait GetReply extends Message

case class Get(name: String, elementId: Option[String] = None) extends Message

case class AttributeValue(name: String, value: Any, elementId: Option[String] = None) extends GetReply
case class UnknownAttribute(name: String, elementId: Option[String]) extends GetReply
case class Reference(elementId: String, endpoint: ActorRef) extends GetReply
case class References(elementsIds: Iterable[String], endpoint: ActorRef) extends GetReply