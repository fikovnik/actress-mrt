package actress.sys

import fr.inria.spirals.actress.metamodel.Observable

trait File {  
  def uid: Observable[Int]
  def uid_=(v: Int): Unit
  
  // TODO: use a timestamp
  def creationTime: Observable[Long]
  def creationTime_=(v: Long): Long
  
  def files: Observable[Iterable[File]] 
}

trait Files {
  def files: Observable[File]
}