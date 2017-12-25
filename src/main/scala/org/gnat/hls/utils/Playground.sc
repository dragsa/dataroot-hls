import java.sql.Timestamp
import java.time._
import scala.util.{Failure, Success, Try}

new Timestamp(-631065600)
new Timestamp(1387072760000L)
val something = Timestamp.valueOf(LocalDateTime.ofEpochSecond(1387072760L, 0, ZoneOffset.UTC))

something.getTime
something.toLocalDateTime.toEpochSecond(ZoneOffset.UTC)

LocalDateTime.now()
LocalDateTime.now(Clock.systemUTC()).toEpochSecond(ZoneOffset.UTC)
(47.998 * 31536000).toLong


val sometime = Timestamp.valueOf(LocalDateTime.ofEpochSecond(1514155017L, 0, ZoneOffset.UTC))
val start = Timestamp.valueOf(LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC))
something


"%.2f".format(0.715)

val test = "123"

1307372631

Try(Integer.parseInt("f")).toEither

List(
  Option("1g2").flatMap(td =>
    Option(Try(Integer.parseInt(td).toLong))),
  Option("h").flatMap(g => g match {
    case "m" | "f" => Option(Success(g))
    case _         => Option(Failure(new Throwable("")))
  })).flatten.collect{case Failure(a) => ()}.length

val Pattern = "([f-zA-Z ]+)".r
java.net.URLDecoder.decode("%D0%91%D1%80%D1%83%D0%BD%D0%B5%D0%B9", "UTF-8")match {
  case c => c
  case _ =>
}
