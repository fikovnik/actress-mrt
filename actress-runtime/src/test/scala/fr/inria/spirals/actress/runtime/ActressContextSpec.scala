package fr.inria.spirals.actress.runtime

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import fr.inria.spirals.actress.metamodel.{APackage, AcorePackage}
import fr.inria.spirals.actress.runtime.protocol.{Reference, ElementPath, AttributeValue, Get}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ActressContextSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("ActressServerSpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "ActressContext" should {
    "bootstrap server" in {

      val ctx = new ActressContext
      ctx.endpoint ! Get(ElementPath.Root, "models")

      val msg = expectMsgType[Reference]
      println(msg)

      msg.endpoint ! Get(msg.elementPath, "_class")

      println(expectMsgType[Reference])

    }
  }

}