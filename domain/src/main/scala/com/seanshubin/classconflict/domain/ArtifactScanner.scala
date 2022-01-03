package com.seanshubin.classconflict.domain

import java.nio.file.Path

class ArtifactScanner(path:Path) extends Iterator[ClassInfo] {
  override def hasNext: Boolean = ???

  override def next(): ClassInfo = ???
}
