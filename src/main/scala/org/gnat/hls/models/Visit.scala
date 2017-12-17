package org.gnat.hls.models

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

  //TODO do we need Long to Timestamp here?
  def visitedAt = column[Long]("visited_at")

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
    // TODO lift to double!
//    db.run(visitTableQuery.filter(_.location === _locationId).map(_.mark.asColumnOf[Double]).avg.result)
    db.run(visitTableQuery.filter(_.location === _locationId).map(_.mark).avg.result)
  }

  def getAll: Future[Seq[Visit]] = {
    db.run(visitTableQuery.result)
  }
}
