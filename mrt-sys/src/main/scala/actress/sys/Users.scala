package actress.sys

import fr.inria.spirals.actress.metamodel.MRTClass

trait User extends MRTClass {

  def name: String

  def name_=(v: String): Unit

}

trait Users extends MRTClass {

  def users: Set[User]

}