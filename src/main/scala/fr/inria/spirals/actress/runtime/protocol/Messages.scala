package fr.inria.spirals.actress.runtime.protocol

import akka.actor.ActorRef

sealed trait Message

case class GetCapabilities() extends Message
case class Capabilities(services: Seq[(String, ActorRef)]) extends Message

case class Register(name: String, ref: ActorRef) extends Message


case class GetAttribute(id: String, name: String) extends Message
case class GetAttributes() extends Message
case class Attributes(attributes: Iterable[String]) extends Message
case class AttributeValue(id: String, name: String, value: Any) extends Message
case class UnknownAttribute(id: String, name: String) extends Message
