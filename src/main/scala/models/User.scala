package models

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import scala.concurrent.Future


//  case class User(firstName: String, lastNsame: String, birthDate: Long, gender: String, email: String, id: Int) extends Entity
//  users "first_name": "Василий", "last_name": "Стамыканый", "birth_date": 196819200, "gender": "m", "id": 10057,
// "email": "tissefedhusytfe@yahoo.com"}

final class UserTable(tag: Tag)
  extends Table[User](tag, "users") {

  def firstName = column[String]("first_name")

  def lastName = column[String]("last_name")

  def birthDate = column[Long]("birth_date")

  def gender = column[String]("gender")

  def email = column[String]("email", O.Unique)

  def id = column[Int]("id", O.PrimaryKey, O.Unique)

  def * =
    (firstName, lastName, birthDate, gender, email, id) <> (User.apply _ tupled, User.unapply)

}

object UserTable {
  val table = TableQuery[UserTable]
}

class UserRepository(implicit db: Database) {
  val userTableQuery = UserTable.table

  def createOne(user: User): Future[User] = {
    db.run(userTableQuery returning userTableQuery += user)
  }

  def createMany(users: List[User]): Future[Seq[User]] = {
    db.run(userTableQuery returning userTableQuery ++= users)
  }

  def updateOne(user: User): Future[Int] = {
    db.run(
      userTableQuery
        .filter(_.id === user.id)
        .update(user))
  }

  def getById(userId: Int): Future[Option[User]] = {
    db.run(
      userTableQuery.filter(_.id === userId).result.headOption)
  }

  def getAll: Future[Seq[User]] = {
    db.run(
      userTableQuery.result)
  }
}