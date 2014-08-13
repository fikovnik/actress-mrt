package actress.sys

import java.nio.file.Path
import java.nio.file.{Files => JFiles}
import fr.inria.spirals.actress.metamodel.Monitor
import fr.inria.spirals.actress.metamodel.Channel
import java.nio.file.attribute.PosixFileAttributeView
import java.util.EnumSet
import java.nio.file.FileVisitOption
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.FileSystems

class FileBinding(id: String) {
  
  assert(id != null)
  
  val path = FileSystems.getDefault.getPath(id)
  assert(JFiles.exists(path), s"${path.normalize}: No such a file")
  
  val posixFile = JFiles.getFileAttributeView(path, classOf[PosixFileAttributeView])
  
  def uid = posixFile.readAttributes().owner.hashCode
  def uid_=(v: Int) = ??? 
  def monitorUid(ch: Channel) = ???
  
  def creationTime = posixFile.readAttributes().creationTime
  def creationTime_=(v: Long) = ???

  def files = {
    if (!JFiles.isDirectory(path)) Seq()
    else {
      val files = path.toFile.listFiles
      files map { f => new FileBinding(f.getCanonicalPath) }
    }
  }  
}

class FilesBinding {
  
  def files: String = "/" 
  
}