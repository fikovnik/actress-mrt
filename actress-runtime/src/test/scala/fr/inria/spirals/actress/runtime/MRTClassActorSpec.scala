package fr.inria.spirals.actress.runtime

import fr.inria.spirals.actress.metamodel.{MRTFeature, MRTClass, Observable}
import fr.inria.spirals.actress.util.Reflection._
import org.scalatest.{Matchers, WordSpec}

class MRTClassActorSpec extends WordSpec with Matchers {

  "MRTClassActor" should {
    "inspect all MRT class features" in {

      trait R extends MRTClass

      trait C extends MRTClass {
        def attribute1: String

        def attribute2: Observable[String]

        def attribute2_=(v: String)

        def attribute3: Observable[Set[String]]

        def attribute4: Observable[collection.mutable.Set[String]]

        def reference1: R

        def reference2: Observable[R]

        def reference2_=(v: R)

        def reference3: Observable[Set[R]]
      }

      val features = MRTClassActor.inspectFeatures[C]

      features should contain(MRTFeature("attribute1", classOf[String]))
      features should contain(MRTFeature("attribute2", classOf[String], observable = true, mutable = true))
      features should contain(MRTFeature("attribute3", classOf[String], observable = true, container = true, unique = true))
      features should contain(MRTFeature("attribute4", classOf[String], observable = true, container = true, unique = true, mutable = true))

      features should contain(MRTFeature("reference1", classOf[R], reference = true))
      features should contain(MRTFeature("reference2", classOf[R], reference = true, observable = true, mutable = true))
      features should contain(MRTFeature("reference3", classOf[R], reference = true, observable = true, container = true, unique = true))

      features should have size 7

    }

    "find feature getters in bindings" in {
      trait B {
        def attribute1: String

        def attribute2: String

        def attribute3: Set[String]

      }

      MRTClassActor.findGet(MRTFeature("attribute1", classOf[String]), classOf[B]) should be('defined)
      MRTClassActor.findGet(MRTFeature("attribute2", classOf[String], observable = true), classOf[B]) should be('defined)
      MRTClassActor.findGet(MRTFeature("attribute3", classOf[String], observable = true, container = true, ordered = false, unique = true), classOf[B]) should be('defined)

      // TODO: the rest of the cases
    }

    "find feature setters in bindings" in {
      trait B {
        def attribute1: String

        def attribute1_=(v: String)
      }

      MRTClassActor.findSet(MRTFeature("attribute1", classOf[String], mutable = true), classOf[B]) should be('defined)
      MRTClassActor.findSet(MRTFeature("attribute1", classOf[String], mutable = true), classOf[B]).get.name should be("attribute1_$eq")

      // TODO: the rest of the cases
    }

    // TODO: add / del methods

    "load all MRT class features" in {
      trait R extends MRTClass

      trait C extends MRTClass {
        def attribute1: String

        def attribute2: Observable[String]

        def attribute2_=(v: String)

        def attribute3: Observable[Set[String]]

        def attribute4: Observable[collection.mutable.Set[String]]
      }

      trait B extends Binding[C] {

        def attribute1: String

        def attribute2: String

        def attribute2_=(v: String)

        def attribute3: Set[String]

        def attribute4: collection.mutable.Set[String]

        def attribute4_add(v: Int)

        def attribute4_del(v: Int)
      }

      val features = MRTClassActor.loadFeatures[C, B]

      features should have size 4

      // TODO: rest of the cases
    }
  }

}
