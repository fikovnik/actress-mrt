package actress.sys

import fr.inria.spirals.actress.metamodel.{MRTClass, MRTFeature, Observable}

trait File extends MRTClass {

  @MRTFeature
  def uid: Observable[Int]
  def uid_=(v: Int): Unit

  @MRTFeature
  def creationTime: Observable[Long]
  def creationTime_=(v: Long): Long

  @MRTFeature
  def files: Observable[Iterable[File]]
}

trait Files extends MRTClass {

  @MRTFeature
  def files: Observable[File]
}