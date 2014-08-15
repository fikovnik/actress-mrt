package actress.sys

import fr.inria.spirals.actress.metamodel.{MRTClass, MRTFeature, Observable}

trait User extends MRTClass {

  @MRTFeature
  def name: Observable[String]

  def name_=(v: String): Unit

}

trait Users extends MRTClass {

  @MRTFeature
  def users: Observable[Set[User]]

}