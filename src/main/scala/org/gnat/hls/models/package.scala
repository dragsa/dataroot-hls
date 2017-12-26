package org.gnat.hls

import io.circe.{Decoder, Encoder, Json}
import java.sql.Timestamp
import java.time.{LocalDateTime, ZoneOffset}
import io.circe.generic.semiauto.deriveEncoder

package object models {

  // TODO does it really work properly?
  implicit def longToTimestampConverter(l: Long) =
    Timestamp.valueOf(LocalDateTime.ofEpochSecond(l, 0, ZoneOffset.UTC))
  implicit def timestampToLongConverter(ts: Timestamp) =
    ts.toLocalDateTime.toEpochSecond(ZoneOffset.UTC)
  implicit def longOptToTimestampOptConverter(ol: Option[Long]) =
    ol.flatMap(
      l =>
        Option(
          Timestamp.valueOf(LocalDateTime.ofEpochSecond(l, 0, ZoneOffset.UTC))))

  //users {"first_name": "Василий", "last_name": "Стамыканый", "birth_date": 196819200, "gender": "m", "id": 10057,
  //       "email": "tissefedhusytfe@yahoo.com"}

  case class User(firstName: String,
                  lastName: String,
                  birthDate: Timestamp,
                  gender: String,
                  email: String,
                  id: Int)

  object User {
    implicit val decodeUser: Decoder[User] = Decoder.instance(u =>
      for {
        fn <- u.downField("first_name").as[String]
        ln <- u.downField("last_name").as[String]
        bd <- u.downField("birth_date").as[Long]
        g <- u.downField("gender").as[String]
        e <- u.downField("email").as[String]
        id <- u.downField("id").as[Int]
      } yield User(fn, ln, bd, g, e, id))

    implicit val userEncoder = new Encoder[User] {
      final def apply(u: User): Json = Json.obj(
        ("first_name", Json.fromString(u.firstName)),
        ("last_name", Json.fromString(u.lastName)),
        ("birth_date", Json.fromLong(u.birthDate)),
        ("gender", Json.fromString(u.gender)),
        ("rating", Json.fromString(u.email)),
        ("id", Json.fromInt(u.id))
      )
    }
  }

  case class UserUpdate(firstName: Option[String],
                        lastName: Option[String],
                        birthDate: Option[Timestamp],
                        gender: Option[String],
                        email: Option[String])

  object UserUpdate {
    implicit val decodeUserUpdate: Decoder[UserUpdate] = Decoder.instance(uu =>
      for {
        fn <- uu.downField("first_name").as[Option[String]]
        ln <- uu.downField("last_name").as[Option[String]]
        bd <- uu.downField("birth_date").as[Option[Long]]
        g <- uu.downField("gender").as[Option[String]]
        e <- uu.downField("email").as[Option[String]]
      } yield UserUpdate(fn, ln, bd, g, e))
  }

  //locations {"distance": 84, "city": "Варинск", "place": "Парк", "id": 7628, "country": "Армения"}

  case class Location(distance: Int,
                      city: String,
                      place: String,
                      country: String,
                      id: Int)

  object Location {
    implicit val decodeLocation: Decoder[Location] = Decoder.instance(l =>
      for {
        d <- l.downField("distance").as[Int]
        p <- l.downField("place").as[String]
        c <- l.downField("city").as[String]
        co <- l.downField("country").as[String]
        id <- l.downField("id").as[Int]
      } yield Location(d, p, c, co, id))
    implicit val locationEncoder: Encoder[Location] = deriveEncoder[Location]
  }

  case class LocationUpdate(distance: Option[Int],
                            city: Option[String],
                            place: Option[String],
                            country: Option[String])

  object LocationUpdate {
    implicit val decodeLocationUpdate: Decoder[LocationUpdate] =
      Decoder.instance(lu =>
        for {
          d <- lu.downField("distance").as[Option[Int]]
          p <- lu.downField("place").as[Option[String]]
          c <- lu.downField("city").as[Option[String]]
          co <- lu.downField("country").as[Option[String]]
        } yield LocationUpdate(d, p, c, co))
  }

  //visits {"user": 759, "location": 87, "visited_at": 1088011472, "id": 10000, "mark": 1}

  case class Visit(user: Int,
                   location: Int,
                   visitedAt: Timestamp,
                   mark: Int,
                   id: Int)

  object Visit {
    implicit val decodeVisit: Decoder[Visit] = Decoder.instance(v =>
      for {
        u <- v.downField("user").as[Int]
        l <- v.downField("location").as[Int]
        va <- v.downField("visited_at").as[Long]
        m <- v.downField("mark").as[Int]
        id <- v.downField("id").as[Int]
      } yield Visit(u, l, va, m, id))

    implicit val visitEncoder = new Encoder[Visit] {
      final def apply(v: Visit): Json = Json.obj(
        ("user", Json.fromInt(v.user)),
        ("location", Json.fromInt(v.location)),
        ("visited_at", Json.fromLong(v.visitedAt)),
        ("mark", Json.fromInt(v.mark)),
        ("id", Json.fromInt(v.id))
      )
    }
  }

  case class VisitUpdate(user: Option[Int],
                         location: Option[Int],
                         visitedAt: Option[Timestamp],
                         mark: Option[Int])

  object VisitUpdate {
    implicit val decodeVisitUpdate: Decoder[VisitUpdate] =
      Decoder.instance(vu =>
        for {
          u <- vu.downField("user").as[Option[Int]]
          l <- vu.downField("location").as[Option[Int]]
          va <- vu.downField("visited_at").as[Option[Long]]
          m <- vu.downField("mark").as[Option[Int]]
        } yield VisitUpdate(u, l, va, m))

    implicit val visitEncoder = new Encoder[Visit] {
      final def apply(v: Visit): Json = Json.obj(
        ("user", Json.fromInt(v.user)),
        ("location", Json.fromInt(v.location)),
        ("visited_at", Json.fromLong(v.visitedAt)),
        ("mark", Json.fromInt(v.mark)),
        ("id", Json.fromInt(v.id))
      )
    }
  }

  case class VisitByUser(mark: Int, visitedAt: Timestamp, place: String)

  object VisitByUser {
    implicit val visitByUserEncoder = new Encoder[VisitByUser] {
      final def apply(vbu: VisitByUser): Json = Json.obj(
        ("mark", Json.fromInt(vbu.mark)),
        ("visited_at",
         Json.fromLong(
           vbu.visitedAt.toLocalDateTime.toEpochSecond(ZoneOffset.UTC))),
        ("place", Json.fromString(vbu.place))
      )
    }
  }

  // no idea how and why
  // case class MaybeFilter[X, Y, C[_]](query: slick.lifted.Query[X, Y, C]) {
  //  def filter[T,R:CanBeQueryCondition](data: Option[T])(f: T => X => R) = {
  //    data.map(v => MaybeFilter(query.withFilter(f(v)))).getOrElse(this)
  //  }
  // }
  //  def find(id: Option[Int], createdMin: Option[Date], createdMax: Option[Date], modifiedMin: Option[Date], modifiedMax: Option[Date]) = {
  //    MaybeFilter(someTable)
  //      .filter(id)(v => d => d.id === v)
  //      .filter(createdMin)(v => d => d.created >= v)
  //      .filter(createdMax)(v => d => d.created <= v)
  //      .filter(modifiedMin)(v => d => d.modified >= v)
  //      .filter(modifiedMax)(v => d => d.modified <= v)
  //      .query
  //  }
}
