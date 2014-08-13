package actress.sys

import fr.inria.spirals.actress.metamodel.Observable

trait User {
  
  def name: Observable[String]
  def name_=(v: String): Unit
  
}

trait Users {

  def users: Observable[Traversable[User]]
  
}