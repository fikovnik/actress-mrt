package actress.sys

import fr.inria.spirals.actress.acore.AClass

trait File extends AClass {

  def uid: Int
  def uid_=(v: Int): Unit

  def creationTime: Long
  def creationTime_=(v: Long): Long

  def files: Iterable[File]
}

trait Files extends AClass {

  def files: File

}