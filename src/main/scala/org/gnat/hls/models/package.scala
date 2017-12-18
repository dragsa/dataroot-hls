package org.gnat.hls

import io.circe.{Decoder, Encoder, Json}
import java.sql.Timestamp
import java.time.{LocalDateTime, ZoneOffset}
import io.circe.generic.semiauto.deriveEncoder

package object models {

  //users "first_name": "Василий", "last_name": "Стамыканый", "birth_date": 196819200, "gender": "m", "id": 10057,
  //      "email": "tissefedhusytfe@yahoo.com"}

  //locations {"distance": 84, "city": "Варинск", "place": "Парк", "id": 7628, "country": "Армения"}

  //visits {"user": 759, "location": 87, "visited_at": 1088011472, "id": 10000, "mark": 1}

  case class User(firstName: String,
                  lastName: String,
                  birthDate: Timestamp,
                  gender: String,
                  email: String,
                  id: Int)

  case class Location(distance: Int,
                      city: String,
                      place: String,
                      country: String,
                      id: Int)

  case class Visit(user: Int,
                   location: Int,
                   visitedAt: Timestamp,
                   mark: Int,
                   id: Int)

  // TODO fix this
  implicit def longToTimestampConverter(l: Long) = Timestamp.valueOf(LocalDateTime.ofEpochSecond(l, 0, ZoneOffset.UTC))
  implicit def timestampToLongConverter(ts: Timestamp) = ts.getTime

  object User {

    implicit val decodeUser: Decoder[User] = Decoder.instance(c =>
      for {
        fn <- c.downField("first_name").as[String]
        ln <- c.downField("last_name").as[String]
        bd <- c.downField("birth_date").as[Long]
        g <- c.downField("gender").as[String]
        e <- c.downField("email").as[String]
        id <- c.downField("id").as[Int]
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
}
