package org.gnat.hls.models

import java.sql.Timestamp
import java.time._
import java.time.temporal.ChronoField
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import scala.concurrent.Future

//case class Visit(user: Int,
//                 location: Int,
//                 visitedAt: Long,
//                 mark: Int,
//                 id: Int)
//
//visits {"user": 759, "location": 87, "visited_at": 1088011472, "id": 10000, "mark": 1}
//
//id - уникальный внешний id посещения. Устанавливается тестирующей системой. 32-разрядное целое беззнакое число.
//location - id достопримечательности. 32-разрядное целое беззнаковое число.
//user - id путешественника. 32-разрядное целое беззнаковое число.
//visited_at - дата посещения, timestamp.
//mark - оценка посещения от 0 до 5 включительно. Целое число.

final class VisitTable(tag: Tag) extends Table[Visit](tag, "visits") {

  def user = column[Int]("user")

  def location = column[Int]("location")

  def visitedAt = column[Timestamp]("visited_at")

  def mark = column[Int]("mark")

  def id = column[Int]("id", O.PrimaryKey, O.Unique)

  def * =
    (user, location, visitedAt, mark, id) <> (Visit.apply _ tupled, Visit.unapply)

}

object VisitTable {
  val table = TableQuery[VisitTable]
}

class VisitRepository(implicit db: Database) {
  val visitTableQuery = VisitTable.table
  val userTableQuery = UserTable.table

  def createOne(visit: Visit): Future[Visit] = {
    db.run(visitTableQuery returning visitTableQuery += visit)
  }

  def createMany(visits: List[Visit]): Future[Seq[Visit]] = {
    db.run(visitTableQuery returning visitTableQuery ++= visits)
  }

  def updateOne(visit: Visit): Future[Int] = {
    db.run(
      visitTableQuery
        .filter(_.id === visit.id)
        .update(visit))
  }

  def getById(_id: Int): Future[Option[Visit]] = {
    db.run(visitTableQuery.filter(_.id === _id).result.headOption)
  }

  def getByUserId(_userId: Int): Future[Seq[Visit]] = {
    db.run(visitTableQuery.filter(visit => visit.user === _userId).result)
  }

  def getLocationMarksById(_locationId: Int) = {
    db.run(
      visitTableQuery
        .filter(_.location === _locationId)
        .map(_.mark)
        .avg
        .asColumnOf[Option[Double]]
        .result)
  }

  def getLocationMarksByIdWithFilter(_locationId: Int,
                                     _fromDate: Option[Long],
                                     _toDate: Option[Long],
                                     _fromAge: Option[Int],
                                     _toAge: Option[Int],
                                     _gender: Option[String]) = {
    db.run {
      (visitTableQuery join userTableQuery on (_.user === _.id))
        .map {
          case (v, u) =>
            (v.id, v.location, v.visitedAt, v.mark, u.birthDate, u.gender)
        }
        // TODO apply filter here more concise, MaybeFilter concept?
        //  GET-параметры:
        //  fromDate - учитывать оценки только с visited_at > fromDate
        //  toDate - учитывать оценки только до visited_at < toDate
        //  fromAge - учитывать только путешественников, у которых возраст (считается от текущего timestamp) строго больше этого параметра
        //  toAge - учитывать только путешественников, у которых возраст (считается от текущего timestamp) строго меньше этого параметра
        //  gender - учитывать оценки только мужчин или женщин
        .filter(_._2 === _locationId)
        .filter(
          f =>
            _fromDate
              .map(fd => f._3 > longToTimestampConverter(fd))
              .getOrElse(slick.lifted.LiteralColumn(true)) &&
              _toDate
                .map(td => f._3 < longToTimestampConverter(td))
                .getOrElse(slick.lifted.LiteralColumn(true))
              &&
//              _fromAge
//                .map(fa => f._5 >  (Instant.now().get(ChronoField.YEAR) - fa.toLong)
//                .getOrElse(slick.lifted.LiteralColumn(true))
//              _toAge
//                .map(ta => f._5 < ta)
//                .getOrElse(slick.lifted.LiteralColumn(true)) &&
                _gender
                  .map(a => f._6 === a)
                  .getOrElse(slick.lifted.LiteralColumn(true))
        )
        .map { case (_, _, _, mark, _, _) => mark }
        .avg
        .asColumnOf[Option[Double]]
        .result
    }
  }

  def getAll: Future[Seq[Visit]] = {
    db.run(visitTableQuery.result)
  }

}
