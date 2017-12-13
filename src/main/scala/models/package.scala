import io.circe.Decoder
import java.sql.Timestamp

package object models {

  //users "first_name": "Василий", "last_name": "Стамыканый", "birth_date": 196819200, "gender": "m", "id": 10057,
  // "email": "tissefedhusytfe@yahoo.com"}

  //locations {"distance": 84, "city": "Варинск", "place": "Парк", "id": 7628, "country": "Армения"}

  //visits {"user": 759, "location": 87, "visited_at": 1088011472, "id": 10000, "mark": 1}

  trait Entity
  case class User(firstName: String,
                  lastName: String,
                  birthDate: Long,
                  gender: String,
                  email: String,
                  id: Int)
      extends Entity
  case class Location(distance: Int,
                      city: String,
                      place: String,
                      country: String,
                      id: Int)
      extends Entity
  case class Visit(user: Int,
                   location: Int,
                   visitedAt: Long,
                   mark: Int,
                   id: Int)
      extends Entity

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
  }

  object Location {
    implicit val decodeLocation: Decoder[Location] = Decoder.instance(l =>
      for {
        d <- l.downField("distance").as[Int]
        c <- l.downField("city").as[String]
        p <- l.downField("place").as[String]
        c <- l.downField("country").as[String]
        id <- l.downField("id").as[Int]
      } yield Location(d, c, p, c, id))
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
  }

  implicit def longToTimestampConverter(l: Long) = {
    new Timestamp(l)
  }

  implicit def timestampToLongConverter(stamp: Timestamp) = {
    stamp.getTime
  }
}
