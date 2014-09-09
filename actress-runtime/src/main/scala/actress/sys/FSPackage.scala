package actress.sys

import fr.inria.spirals.actress.acore._

object FSPackage extends FSPackageImpl

trait FSPackage extends APackage {

  val FileClass: AClass
  val RegularFileClass: AClass
  val DirectoryClass: AClass

  val FileClass_name_Feature: AAttribute
  val FileClass_creationTime_Feature: AAttribute
  val DirectoryClass_files_Feature: AReference

}

trait File extends AObject {

  def name: String

  def name_=(v: String)

  def creationTime: Long

  def creationTime_=(v: Long): Unit

}

trait RegularFile extends File

trait Directory extends File {

  @Containment
  @Derived
  def files: AMutableSet[File]

}

