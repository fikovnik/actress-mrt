package actress.sys

import fr.inria.spirals.actress.metamodel.{MRTClass, Observable}

trait User extends MRTClass {

  def name: Observable[String]

  def name_=(v: String): Unit

}

trait Users extends MRTClass {

  def users: Observable[Set[User]]

}