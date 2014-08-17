package fr.inria.spirals.actress.util

import org.scalatest.WordSpec
import org.scalatest.Matchers

class ReflectionSpec extends WordSpec with Matchers {

  import Reflection._

  "Reflection" should {

    "resolve all super classes" in {
      class B
      trait T0
      trait T1 extends T0
      trait T2 extends T0 with T1
      class E1 extends B
      class E2 extends E1 with T1 with T2

      classOf[B].allSuperClasses should be (empty)
      classOf[T0].allSuperClasses should be (empty)
      classOf[T1].allSuperClasses should contain only classOf[T0]
      classOf[T2].allSuperClasses should contain only(classOf[T0], classOf[T1])
      classOf[E1].allSuperClasses should contain only(classOf[B])
      classOf[E2].allSuperClasses should contain only(classOf[B], classOf[E1], classOf[T0], classOf[T1], classOf[T2])
    }

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