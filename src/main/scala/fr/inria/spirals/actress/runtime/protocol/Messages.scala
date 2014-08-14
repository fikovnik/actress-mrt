package fr.inria.spirals.actress.runtime.protocol

import akka.actor.ActorRef

sealed trait Message

// ModelActor
case class Get(name: String, elementId: Option[String] = None) extends Message


case class AttributeValue(name: String, value: Any, elementId: Option[String] = None) extends Message
case class UnknownAttribute(name: String, elementId: Option[String]) extends Message
case class Reference(elementId: String, endpoint: ActorRef) extends Message
case class References(elements: Iterable[Reference]) extends Message