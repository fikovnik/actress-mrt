package actress.sys

import fr.inria.spirals.actress.metamodel.MRTClass

trait File extends MRTClass {

  def uid: Int
  def uid_=(v: Int): Unit

  def creationTime: Long
  def creationTime_=(v: Long): Long

  def files: Iterable[File]
}

trait Files extends MRTClass {

  def files: File

}