package actress.sys

import fr.inria.spirals.actress.metamodel.{MRTClass, Observable}

trait File extends MRTClass {

  def uid: Observable[Int]
  def uid_=(v: Int): Unit

  def creationTime: Observable[Long]
  def creationTime_=(v: Long): Long

  def files: Observable[Iterable[File]]
}

trait Files extends MRTClass {

  def files: Observable[File]

}