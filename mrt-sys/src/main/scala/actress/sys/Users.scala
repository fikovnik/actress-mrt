package actress.sys

import fr.inria.spirals.actress.metamodel.AClass

trait User extends AClass {

  def name: String

  def name_=(v: String): Unit

}

trait Users extends AClass {

  def users: Set[User]

}