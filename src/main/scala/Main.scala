import io.circe.streaming._
import io.iteratee.scalaz.task._
import java.io.File
import scalaz.concurrent.Task
import models.User

object Main extends App {
  val user =
    readBytes(new File("/temp/data/users_1.json"))
      .map(a => {
        if (a.contains(91.toByte)) a.dropWhile(b => !b.equals(91.toByte))
        else if (a.contains(93.toByte)) a.take(a.length - 1)
        else a
      })
      .through(byteParser)
      .through(decoder[Task, User])
      .map(a => println(a))
  val test = user.into(takeWhileI(_ => true)).unsafePerformSync
}
