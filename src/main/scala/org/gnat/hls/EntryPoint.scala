package org.gnat.hls

import java.io.File
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Success
import scalaz.concurrent.Task
import com.typesafe.scalalogging.LazyLogging
import io.circe.streaming._
import io.iteratee.scalaz.task._
import org.gnat.hls.models.{LocationRepository, UserRepository, VisitRepository}
import org.gnat.hls.models._
import org.gnat.hls.utils.Utils._
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

  val workingPath = "/tmp/data/"
//    "/Users/dragsa/Documents/HLC/hlcupdocs-master/data/FULL/data_gnat/"

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

  val objectArrayExtractor: (Array[Byte] => Array[Byte]) = a => {
    if (a.contains(91.toByte)) a.dropWhile(b => !b.equals(91.toByte))
    else if (a.contains(93.toByte)) a.take(a.length - 1)
    else a
  }

  // TODO parametrization is tricky here due to to the way how cicre consumes implicits
  def userDecoder(path: String) =
    readBytes(new File(path))
      .map(objectArrayExtractor)
      .through(byteParser)
      .through(decoder[Task, User])
      .into(takeWhileI(_ => true))
      .map(a =>
        usersRepository
          .createMany(a.toList)
          .andThen({
            case Success(_) => logger.info("finished processing file " + path)
          }))
      .unsafePerformSync

  def locationDecoder(path: String) =
    readBytes(new File(path))
      .map(objectArrayExtractor)
      .through(byteParser)
      .through(decoder[Task, Location])
      .into(takeWhileI(_ => true))
      .map(a =>
        locationsRepository
          .createMany(a.toList)
          .andThen({
            case Success(_) => logger.info("finished processing file " + path)
          }))
      .unsafePerformSync

  def visitDecoder[A](path: String) =
    readBytes(new File(path))
      .map(objectArrayExtractor)
      .through(byteParser)
      .through(decoder[Task, Visit])
      .into(takeWhileI(_ => true))
      .map(a =>
        visitsRepository
          .createMany(a.toList)
          .andThen({
            case Success(_) => logger.info("finished processing file " + path)
          }))
      .unsafePerformSync

  val allFiles = {
    zippedFolderExtractor(workingPath)
    listFiles(new File(workingPath))
    //    listFiles(new File("/temp/data/"))
  }

  def insertUsers =
    Future {
      allFiles
        .filter(f =>
          f.getName.startsWith("users") && f.getName.endsWith(".json"))
        .map(ff => {
          logger.info("processing input file " + ff)
          userDecoder(ff.getAbsolutePath)
        })
        .into(takeWhileI(_ => true))
        .unsafePerformSync
    }

  def insertLocations =
    Future {
      allFiles
        .filter(f =>
          f.getName.startsWith("locations") && f.getName.endsWith(".json"))
        .map(ff => {
          logger.info("processing input file " + ff)
          locationDecoder(ff.getAbsolutePath)
        })
        .into(takeWhileI(_ => true))
        .unsafePerformSync
    }

  def insertVisits =
    Future {
      allFiles
        .filter(f =>
          f.getName.startsWith("visits") && f.getName.endsWith(".json"))
        .map(ff => {
          logger.info("processing input file " + ff)
          visitDecoder(ff.getAbsolutePath)
        })
        .into(takeWhileI(_ => true))
        .unsafePerformSync
    }

  def main(args: Array[String]): Unit = {
    logger.info("started")
    initTables()
//    timeCounter(insertUsers, "insert all users")
    insertUsers
    insertLocations
    insertVisits
    Thread.sleep(6000000)
  }
}
