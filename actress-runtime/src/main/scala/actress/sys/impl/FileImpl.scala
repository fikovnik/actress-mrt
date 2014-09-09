package actress.sys.impl

import java.nio.file.attribute.{FileTime, PosixFileAttributeView}
import java.nio.file.{StandardCopyOption, Files => JFiles, Path => JPath}

import actress.sys.{Directory, FSPackage, File, RegularFile}
import fr.inria.spirals.actress.acore.AMutableSet
import fr.inria.spirals.actress.acore.impl.AObjectImpl
import fr.inria.spirals.actress.acore.util.{ContainmentPair, ContainmentSettingIterable, ARuntimeMutableSet}

import scala.collection.JavaConversions._

abstract class FileImpl(val path: JPath) extends AObjectImpl with File {

  override lazy val _class = FSPackage.FileClass

  override def _elementName: String = name

  private val posixFile = JFiles.getFileAttributeView(path, classOf[PosixFileAttributeView])

  override def name: String = path.getFileName.toString

  override def name_=(v: String): Unit = {
    val dest = path.resolveSibling(v)
    assert(JFiles.notExists(dest))
    JFiles.move(path, dest)
  }

  override def creationTime_=(v: Long): Unit = posixFile.setTimes(null, null, FileTime.fromMillis(v))

  override def creationTime: Long = posixFile.readAttributes().creationTime().toMillis

}

class RegularFileImpl(path: JPath) extends FileImpl(path) with RegularFile {
  override lazy val _class = FSPackage.RegularFileClass
}

class DirectoryImpl(path: JPath) extends FileImpl(path) with Directory {
  override lazy val _class = FSPackage.DirectoryClass

  assert(JFiles.isDirectory(path))

  override lazy val files: AMutableSet[File] = new ARuntimeMutableSet[File] with ContainmentSettingIterable[File] {
    //        aObj.__containmentPair = Some((this, FSPackage.DirectoryClass_files_Feature))
    //        aObj._endpoint = this._endpoint

    override protected def _iterable: Iterable[File] = {
      JFiles.newDirectoryStream(path) map { x =>
        if (JFiles.isDirectory(x)) new DirectoryImpl(x) else new RegularFileImpl(x)
      }
    }

    override protected def _add(elem: File): Unit = {

      val dest = path.resolve(elem.name)
      if (JFiles.exists(dest)) throw new Exception(s"Destination $dest already exists")

      // FIXME: just a quick hack
      val orig = elem.asInstanceOf[FileImpl]

      if (JFiles.exists(orig.path))
        JFiles.move(orig.path, dest, StandardCopyOption.ATOMIC_MOVE)
      else
        elem match {
          // TODO: copy attributes
          case d: Directory =>
            JFiles.createDirectory(dest)
          case f: RegularFile =>
            JFiles.createFile(dest)
          // TODO: copy stuff
        }
    }

    override protected def _del(elem: File): Unit = {

    }

    override val containmentPair: ContainmentPair = ContainmentPair(DirectoryImpl.this, FSPackage.DirectoryClass_files_Feature)
  }
}