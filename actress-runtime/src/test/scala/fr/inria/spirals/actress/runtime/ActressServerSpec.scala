package fr.inria.spirals.actress.runtime

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import fr.inria.spirals.actress.metamodel.{APackage, AcorePackage}
import fr.inria.spirals.actress.runtime.protocol.{AttributeValue, Get}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ActressServerSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("ActressServerSpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "ActressServer" should {
    "bootstrap model-endpoint server" in {

      val sys = new AMR
      sys.run(AcorePackage)
      sys.run(APackage.registry)

      sys.models ! Get("APackageRegistry","packages")

      println(expectMsgType[AttributeValue])

    }
  }

}