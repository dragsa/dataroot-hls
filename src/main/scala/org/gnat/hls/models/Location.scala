package org.gnat.hls.models

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import scala.concurrent.Future

//case class Location(distance: Int,
//                    city: String,
//                    place: String,
//                    country: String,
//                    id: Int)
//
//locations {"distance": 84, "city": "Варинск", "place": "Парк", "id": 7628, "country": "Армения"}
//
//id - уникальный внешний id достопримечательности. Устанавливается тестирующей системой. 32-разрядное целое беззнаковоее число.
//place - описание достопримечательности. Текстовое поле неограниченной длины.
//country - название страны расположения. unicode-строка длиной до 50 символов.
//city - название города расположения. unicode-строка длиной до 50 символов.
//distance - расстояние от города по прямой в километрах. 32-разрядное целое беззнаковое число.

final class LocationTable(tag: Tag)
  extends Table[Location](tag, "locations") {

  def distance = column[Int]("distance")

  def place = column[String]("place")

  def city = column[String]("city", O.Length(50))

  def country = column[String]("country", O.Length(50))

  def id = column[Int]("id", O.PrimaryKey, O.Unique)

  def * =
    (distance, place, city, country, id) <> (Location.apply _ tupled, Location.unapply)

}

object LocationTable {
  val table = TableQuery[LocationTable]
}

class LocationRepository(implicit db: Database) {
  val locationTableQuery = LocationTable.table

  def createOne(location: Location): Future[Location] = {
    db.run(locationTableQuery returning locationTableQuery += location)
  }

  def createMany(locations: List[Location]): Future[Seq[Location]] = {
    db.run(locationTableQuery returning locationTableQuery ++= locations)
  }

  def updateOne(location: Location): Future[Int] = {
    db.run(
      locationTableQuery
        .filter(_.id === location.id)
        .update(location))
  }

  def getById(locationId: Int): Future[Option[Location]] = {
    db.run(
      locationTableQuery.filter(_.id === locationId).result.headOption)
  }

  def getAll: Future[Seq[Location]] = {
    db.run(
      locationTableQuery.result)
  }
}
