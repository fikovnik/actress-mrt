package actress.sys.binding

import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.{FileSystems, Files => JFiles}

class FileBinding(id: String) {

  assert(id != null)

  val path = FileSystems.getDefault.getPath(id)
  assert(JFiles.exists(path), s"${path.normalize}: No such a file")

  val posixFile = JFiles.getFileAttributeView(path, classOf[PosixFileAttributeView])

  def uid = posixFile.readAttributes().owner.hashCode

  def uid_=(v: Int) = ???

  //  def monitorUid(ch: Channel) = ???

  def creationTime = posixFile.readAttributes().creationTime

  def creationTime_=(v: Long) = ???

  def files = {
    if (!JFiles.isDirectory(path)) Seq()
    else {
      val files = path.toFile.listFiles
      files map { f => new FileBinding(f.getCanonicalPath)}
    }
  }
}

class FilesBinding {

  def files: String = "/"

}