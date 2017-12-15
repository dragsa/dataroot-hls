package org.gnat.hls

import java.io.File
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scalaz.concurrent.Task
import com.typesafe.scalalogging.LazyLogging
import io.circe.streaming._
import io.iteratee.scalaz.task._
import org.gnat.hls.models.{LocationRepository, UserRepository, VisitRepository}
import org.gnat.hls.models._
import org.gnat.hls.utils.TimeUtils._
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.meta.MTable

object EntryPoint extends LazyLogging {

  implicit val db = Database.forConfig("gnat_hls")
  implicit val usersRepository = new UserRepository
  implicit val locationsRepository = new LocationRepository
  implicit val visitsRepository = new VisitRepository

  val tables = Map("users" -> usersRepository.userTableQuery,
                   "locations" -> locationsRepository.locationTableQuery,
                   "visits" -> visitsRepository.visitTableQuery)

  def initTables(): Unit = {
    tables.keys.foreach(tableCreator)
  }

  def tableCreator(tableName: String): Unit = {
    Await.result(
      db.run(MTable.getTables(tableName))
        .flatMap(matchedTables =>
          if (matchedTables.isEmpty) {
            logger.info(tableName + " table doesn't exist, creating...")
            db.run(tables(tableName).schema.create)
          } else Future.successful())
        .andThen { case _ => logger.info(tableName + " table check finished") },
      Duration.Inf
    )
  }

  val arrayProcessor: (Array[Byte] => Array[Byte]) = a => {
    if (a.contains(91.toByte)) a.dropWhile(b => !b.equals(91.toByte))
    else if (a.contains(93.toByte)) a.take(a.length - 1)
    else a
  }

  def userDecoder(path: String) =
    readBytes(new File(path))
      .map(arrayProcessor)
      .through(byteParser)
      .through(decoder[Task, User])
      .map(a => {
        println(a)
        Await.result(usersRepository.createOne(a), Duration.Inf)
      })
      .into(takeWhileI(_ => true))
      .unsafePerformSync

  def locationDecoder(path: String) =
    readBytes(new File(path))
      .map(arrayProcessor)
      .through(byteParser)
      .through(decoder[Task, Location])
      .map(a => println(a))
      .into(takeWhileI(_ => true))
      .unsafePerformSync

  def visitDecoder(path: String) =
    readBytes(new File(path))
      .map(arrayProcessor)
      .through(byteParser)
      .through(decoder[Task, Visit])
      .map(a => println(a))
      .into(takeWhileI(_ => true))
      .unsafePerformSync

  lazy val allFiles =
    listFiles(new File("/temp/data/"))
//    listFiles(
//      new File("/Users/dragsa/Documents/HLC/hlcupdocs-master/data/FULL/data/"))

  lazy val insertUsers = allFiles
    .filter(f => f.getName.startsWith("users"))
    .map(ff => {
      println(ff)
      userDecoder(ff.getAbsolutePath)
    })
    .into(takeWhileI(_ => true))
    .unsafePerformSync

  lazy val insertLocations = allFiles
    .filter(f => f.getName.startsWith("locations"))
    .map(f => locationDecoder(f.getAbsolutePath))
    .into(takeWhileI(_ => true))
    .unsafePerformSync

  lazy val insertVisits = allFiles
    .filter(f => f.getName.startsWith("visits"))
    .map(f => visitDecoder(f.getAbsolutePath))
    .into(takeWhileI(_ => true))
    .unsafePerformSync

  def main(args: Array[String]): Unit = {
    logger.info("started")
    initTables()
//    timeCounter(insertUsers, "insert all users")
  }
}
