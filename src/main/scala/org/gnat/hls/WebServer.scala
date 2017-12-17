package org.gnat.hls

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import scala.io.StdIn
import com.typesafe.scalalogging.LazyLogging

object WebServer extends App with ApiRouter with LazyLogging {

  implicit val system = ActorSystem("slick-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  //  checkDatabase()

  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)

//  initDatabase
  logger.info("Started server")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}