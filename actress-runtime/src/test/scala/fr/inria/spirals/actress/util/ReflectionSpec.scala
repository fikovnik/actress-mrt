package fr.inria.spirals.actress.util

import org.scalatest.WordSpec
import org.scalatest.Matchers

class ReflectionSpec extends WordSpec with Matchers {

  import Reflection._

  "Reflection" should {

    "resolve all type parameters of a method" which {

      "has a simple return type" in {
        trait A { def m: String }

        val m = classOf[A].declaredMethods(0)
        m.resolveGenericReturnType should contain only (classOf[String])
      }

      "has a one level nested return type" in {
        trait A { def m: Option[String] }

        val m = classOf[A].declaredMethods(0)
        m.resolveGenericReturnType should contain inOrderOnly (
          classOf[Option[_]],
          classOf[String]
        )
      }

      "has a two levels nested return type" in {
        trait A { def m: Seq[Option[String]] }

        val m = classOf[A].declaredMethods(0)
        m.resolveGenericReturnType should contain inOrderOnly (
          classOf[Seq[_]],
          classOf[Option[_]],
          classOf[String]
        )
      }
    }
  }

}