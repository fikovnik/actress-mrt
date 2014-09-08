package actress.sys

import fr.inria.spirals.actress.acore.AcorePackage
import fr.inria.spirals.actress.acore.impl.{AAttributeImpl, AClassImpl, APackageImpl, AReferenceImpl}

trait FSPackageImpl extends APackageImpl with FSPackage {

  override val FileClass = new AClassImpl
  override val RegularFileClass = new AClassImpl
  override val DirectoryClass = new AClassImpl

  override val FileClass_name_Feature = new AAttributeImpl
  override val FileClass_creationTime_Feature = new AAttributeImpl
  override val DirectoryClass_files_Feature = new AReferenceImpl

  initialize()

  private def initialize(): Unit = {

    _name = "fs"

    initializeFileClass()
    initializeRegularFileClass()
    initializeDirectoryClass()

    _classifiers ++= Seq(
      FileClass,
      RegularFileClass,
      DirectoryClass
    )
  }

  def initializeFileClass(): Unit = {
    FileClass._name = "File"
    FileClass._abstract = true

    FileClass._superTypes ++= Seq(
      AcorePackage.AObjectClass
    )

    FileClass._features ++= Seq(
      FileClass_name_Feature init { x =>
        x._name = "name"
        x._type = AcorePackage.AStringDataType
      },
      FileClass_creationTime_Feature init { x =>
        x._name = "creationTime"
        x._type = AcorePackage.ALongDataType
      }
    )
  }

  def initializeRegularFileClass(): Unit = {
    RegularFileClass._name = "RegularFile"

    RegularFileClass._superTypes ++= Seq(
      FileClass
    )
  }

  def initializeDirectoryClass(): Unit = {
    DirectoryClass._name = "Directory"

    DirectoryClass._superTypes ++= Seq(
      FileClass
    )

    DirectoryClass._features ++= Seq(
      DirectoryClass_files_Feature init { x =>
        x._name = "files"
        x._type = FileClass
        x._many = true
        x._containment = true
        x._derived = true
      }
    )
  }


}
