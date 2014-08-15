package fr.inria.spirals.actress.util.io

import java.nio.file.Path

object FileWatchDog {
  
  def apply(path: Path)(func: Path => Any): FileWatchDog = new FileWatchDog(path, func)
  
}

class FileWatchDog(val path: Path, func: Path => Any) {
  
  def cancel(): Unit = {}
  
}