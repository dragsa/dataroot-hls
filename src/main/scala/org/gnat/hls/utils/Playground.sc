import java.sql.Timestamp
import java.time.{Instant, LocalDateTime, ZoneId, ZoneOffset}

new Timestamp(-631065600)
new Timestamp(1387072760000L)
val something = Timestamp.valueOf(LocalDateTime.ofEpochSecond(1387072760L, 0, ZoneOffset.UTC))
something.getTime
something.toLocalDateTime.toEpochSecond(ZoneOffset.UTC)

"%.2f".format(0.715)

val test = "123"

1307372631