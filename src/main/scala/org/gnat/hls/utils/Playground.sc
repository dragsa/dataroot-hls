import java.sql.Timestamp
import java.time._

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