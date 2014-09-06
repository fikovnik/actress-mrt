package fr.inria.spirals.actress.acore

import org.scalatest.{Matchers, WordSpec}

class MetaModelSpec extends WordSpec with Matchers {

  "AcorePackage" should {
    "initialize" in {

      // initialize
      AcorePackage.toString

      AcorePackage._classes foreach { x =>
        x._package should be(AcorePackage)
        x._container should be(Some(AcorePackage))
        x._containmentReference should be(Some(AcorePackage.APackageClass_classifiers_Feature))
      }
    }
  }


}
