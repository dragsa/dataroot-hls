import io.circe.Decoder

package object models {

  //visits {"user": 759, "location": 87, "visited_at": 1088011472, "id": 10000, "mark": 1}
  //users "first_name": "Василий", "last_name": "Стамыканый", "birth_date": 196819200, "gender": "m", "id": 10057,
  // "email": "tissefedhusytfe@yahoo.com"}]
  //locations {"distance": 84, "city": "Варинск", "place": "Парк", "id": 7628, "country": "Армения"}

  case class User(firstName: String, lastNsame: String, birthDate: Long, gender: String, email: String, id: Int)
  case class Location(distance: Int, city: String, place: String, country: String, id: Int)
  case class Visit(user: Int, location: Int, visitedAt: Long, mark: Int, id: Int)

  object User {
    implicit val decodeUser: Decoder[User] = Decoder.instance(c =>
      for {
        fn <- c.downField("first_name").as[String]
        ln <- c.downField("last_name").as[String]
        bd <- c.downField("birth_date").as[Long]
        g <- c.downField("gender").as[String]
        e <- c.downField("email").as[String]
        id <- c.downField("id").as[Int]
      } yield User(fn, ln, bd, g, e, id)
    )
  }
}
