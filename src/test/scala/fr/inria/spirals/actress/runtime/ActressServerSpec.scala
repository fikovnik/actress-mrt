package fr.inria.spirals.actress.runtime

import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.WordSpecLike
import actress.sys.OSInfo
import actress.sys.OSInfoBinding
import akka.actor.ActorSystem
import akka.actor.actorRef2Scala
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import fr.inria.spirals.actress.runtime.protocol.Capabilities
import fr.inria.spirals.actress.runtime.protocol.GetCapabilities
import akka.actor.ActorRef
import fr.inria.spirals.actress.runtime.protocol.GetAttribute
import akka.testkit.TestActorRef
import fr.inria.spirals.actress.runtime.protocol.GetAttributes
import fr.inria.spirals.actress.runtime.protocol.Attributes
import fr.inria.spirals.actress.runtime.protocol.Attributes

class ActressServerSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("ActressServerSpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "ServiceLocator" when {
    "no services have been registered" should {
      "report no services" in {
        val server = new ActressServer
        server.serviceLocator ! GetCapabilities()
        expectMsg(Capabilities(Seq()))
      }
    }

    "a service is registered" should {
      "report its endpoint" in {
        val bf = { _: String ⇒ new OSInfoBinding }
        val server = new ActressServer

        server.registerModel[OSInfo]("os", bf)

        server.serviceLocator ! GetCapabilities()
        val msg = receiveOne(remaining)
        println(msg)
        //      expectMsg(Capabilities(_))
      }
    }
  }
  
  "NodeActor" should {
    "get attributes" in {
      val bf = { _: String ⇒ new OSInfoBinding }
      val na = TestActorRef(new ModelActor(bf))
      na ! GetAttributes
      val r = expectMsgType[Attributes]
      r.attributes should contain only("name")
    }
    
    "get an attribute value" in {
        val bf = { _: String ⇒ new OSInfoBinding }
        val server = new ActressServer

        server.registerModel[OSInfo]("os", bf)

        server.serviceLocator ! GetCapabilities()
        receiveOne(remaining) match {
          case Capabilities(Seq(("os", ref: ActorRef))) =>
            ref ! GetAttribute("", "name")
            println(receiveOne(remaining))
        }
      
    }
  }

}