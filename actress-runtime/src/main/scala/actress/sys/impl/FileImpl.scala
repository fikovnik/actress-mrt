package actress.sys.impl

import java.io.{File => JFile}
import java.nio.file.attribute.{FileTime, PosixFileAttributeView}
import java.nio.file.{Files => JFiles}

import actress.sys.{FSPackage, Directory, File, RegularFile}
import fr.inria.spirals.actress.acore.AMutableSequence
import fr.inria.spirals.actress.acore.impl.AObjectImpl

abstract class FileImpl(jFile: JFile) extends AObjectImpl with File {

  override lazy val _class = FSPackage.FileClass

  private val posixFile = JFiles.getFileAttributeView(jFile.toPath, classOf[PosixFileAttributeView])

  override def name: String = jFile.getName

  override def name_=(v: String): Unit = jFile.renameTo(new JFile(jFile.getParentFile, v))

  override def creationTime_=(v: Long): Unit = posixFile.setTimes(null, null, FileTime.fromMillis(v))

  override def creationTime: Long = posixFile.readAttributes().creationTime().toMillis

  override def _elementName: String = name

}

class RegularFileImpl(jFile: JFile) extends FileImpl(jFile) with RegularFile {
  override lazy val _class = FSPackage.RegularFileClass
}

class DirectoryImpl(jFile: JFile) extends FileImpl(jFile) with Directory {
  override lazy val _class = FSPackage.DirectoryClass

  assert(jFile.isDirectory)

  override def files: AMutableSequence[File] = {
    val files = jFile.listFiles map { jf =>
      val aObj = if (jf.isDirectory) new DirectoryImpl(jf) else new RegularFileImpl(jf)

      // TODO: there should be some basic support for the following bookkeeping, extracted from the ACollectionContainment

      aObj.__container = Some((this, FSPackage.DirectoryClass_files_Feature))
      aObj._endpoint = this._endpoint
      aObj
    }
    files.toBuffer
  }
}