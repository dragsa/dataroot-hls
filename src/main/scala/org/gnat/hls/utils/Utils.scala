package org.gnat.hls.utils

import java.io.{FileInputStream, FileOutputStream}
import java.util.zip.ZipInputStream
import scala.util.Try
import com.typesafe.scalalogging.LazyLogging

object Utils extends LazyLogging {
  def timeCounter[R](block: => R, operation: String): R = {
    val t0 = System.nanoTime()
    val result = block
    val t1 = System.nanoTime()
    logger.info(
      String.format("Elapsed time for %s: " + (t1 - t0) + "ns", operation))
    result
  }

  def zippedFolderExtractor(path: String) = {
    val zis = new ZipInputStream(new FileInputStream(path + "data.zip"))
    Stream.continually(zis.getNextEntry).takeWhile(_ != null).foreach { file =>
      logger.info("extracting " + file)
      val fout = new FileOutputStream(path + "/" + file.getName)
      val buffer = new Array[Byte](1024)
      Stream
        .continually(zis.read(buffer))
        .takeWhile(_ != -1)
        .foreach(fout.write(buffer, 0, _))
    }
  }

  def optionalStringToOptionalLong(optStr: Option[String]) =
    optStr.flatMap(
      s =>
        Try(
          Integer
            .parseInt(s)
            .toLong).toOption)
}
