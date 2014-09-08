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

      println(testActor)
      ctx.endpoint tell(Get(ElementPath.Root, "_class"), testActor)
      expectMsgType[Reference].elementPath.path should be("//metamodels/core/_classifiers/ModelRegistry")

      ctx.endpoint ! Get(ElementPath.Root, "metamodels")
      val refs = expectMsgType[References]
      refs.references.map(_.elementPath.path) should contain allOf(
        "//metamodels/core",
        "//metamodels/acore",
        "//metamodels/fs")

      val metamodel = refs.references.find(_.elementPath.path.endsWith("acore")).get

      metamodel.endpoint ! Get(metamodel.elementPath, "_class")
      val pkg = expectMsgType[Reference]
      pkg.elementPath.path should be("//metamodels/acore/_classifiers/APackage")

      pkg.endpoint ! Get(pkg.elementPath, "_class")
      val cls = expectMsgType[Reference]
      cls.elementPath.path should be("//metamodels/acore/_classifiers/AClass")

    }
    "work with files" in {

      val ctx = new ActressContext

//      ctx.endpoint ! Get(ElementPath.Root, "models")
//      val msg = expectMsgType[References]
//      println(msg)

      ctx.endpoint ! Get(ElementPath.Root + ElementPathSegment("models","fs"), "name")
      println(expectMsgType[Any])
      ctx.endpoint ! Get(ElementPath.Root + ElementPathSegment("models","fs"), "creationTime")
      println(expectMsgType[Any])
      ctx.endpoint ! Get(ElementPath.Root + ElementPathSegment("models","fs"), "files")
      val files = expectMsgType[References]
      println(files)
      val file = files.references.toSeq(0)
      file.endpoint ! Get(file.elementPath, "name")
      println(expectMsgType[AttributeValue])
      //      resp.elementPath.path should be ("//models")
      //
      //      resp.endpoint ! Get(resp.elementPath, "")


    }
  }

}