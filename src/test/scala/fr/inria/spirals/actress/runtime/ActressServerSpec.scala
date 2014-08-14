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
import fr.inria.spirals.actress.runtime.protocol.Get
import fr.inria.spirals.actress.runtime.protocol.AttributeValue

class ActressServerSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("ActressServerSpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

//  "NodeActor" should {
//    "get attributes" in {
//      val bf = { _: String ⇒ new OSInfoBinding }
//      val na = TestActorRef(new ModelActor(bf))
//      na ! GetAttributes
//      val r = expectMsgType[Attributes]
//      r.attributes should contain only("name")
//    }
//    
//    "get an attribute value" in {
//        val bf = { _: String ⇒ new OSInfoBinding }
//        val server = new ActressServer
//
//        server.registerModel[OSInfo]("os", bf)
//
//        server.modelLocator ! GetModels()
//        receiveOne(remaining) match {
//          case Models(Seq(ModelEndpoint("os", ref: ActorRef))) =>
//            ref ! GetAttribute("", "name")
//            println(receiveOne(remaining))
//        }
//      
//    }
//  }

  "ActressServer" should {
    "bootstrap model-endpoint server" in {
      val server = new ActressServer
      
      server.modelsEndpoints should not be(null)
      server.modelsEndpoints ! Get("endpoints")
      val r = expectMsgType[AttributeValue]
      println(r)
    }
  }
  
}