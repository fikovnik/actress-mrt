package fr.inria.spirals.actress.runtime

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import fr.inria.spirals.actress.runtime.protocol._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ActressContextSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("ActressServerSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "ActressContext" should {
    "bootstrap server" in {

      val ctx = new ActressContext

      ctx.endpoint ! Get(ElementPath.Root, "_class")
      expectMsgType[Reference].elementPath.path should be ("//models/registry/packages/acore/_classifiers/AModelRegistry")

      ctx.endpoint ! Get(ElementPath.Root, "models")
      val msg = expectMsgType[References]
      println(msg)

      val model = msg.elementPaths.toSeq(0)

      model.endpoint ! Get(model.elementPath, "_class")
      val msg1 = expectMsgType[Reference]
      println(msg1)

      msg1.endpoint ! Get(msg1.elementPath, "_class")
      val msg2 = expectMsgType[Reference]
      println(msg2)

    }
  }

}