package org.gnat.hls.utils

import com.typesafe.scalalogging.LazyLogging

object TimeUtils extends LazyLogging {
  def timeCounter[R](block: => R, operation: String): R = {
    val t0 = System.nanoTime()
    val result = block
    val t1 = System.nanoTime()
    logger.info(String.format("Elapsed time for %s: " + (t1 - t0) + "ns", operation))
    result
  }
}
