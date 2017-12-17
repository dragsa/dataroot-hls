package org.gnat.hls

import scala.util.{Failure, Success, Try}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.syntax._

//GET /<entity>/<id> для получения данных о сущности
//GET /users/<id>/visits для получения списка посещений пользователем
//GET /locations/<id>/avg для получения средней оценки достопримечательности
//POST /<entity>/<id> на обновление
//POST /<entity>/new на создание

trait ApiRouter extends HlsDatabase with FailFastCirceSupport {
  val route =
    pathPrefix("api") {
      pathPrefix(Segment) {
        case entityType @ ("users" | "locations" | "visits") =>
          pathPrefix("new") {
            pathEnd {
              post {
                complete(s"POST to create $entityType")
              }
            }
          } ~ pathPrefix(Segment) { existingIdOrNew =>
            Try(Integer.parseInt(existingIdOrNew)) match {
              case Success(parsedId) =>
                pathEnd {
                  entityType match {
                    case "users" =>
                      get {
                        onSuccess(usersRepository.getById(parsedId)) {
                          case Some(a) => complete(a.asJson)
                          case None    => complete(StatusCodes.NotFound)
                        }
                      } ~ post {
                        // TODO
                        complete(s"POST to update $entityType")
                      }
                    case "locations" =>
                      get {
                        onSuccess(locationsRepository.getById(parsedId)) {
                          case Some(a) => complete(a.asJson)
                          case None    => complete(StatusCodes.NotFound)
                        }
                      } ~ post {
                        // TODO
                        complete(s"POST to update $entityType")
                      }
                    case "visits" =>
                      get {
                        onSuccess(visitsRepository.getById(parsedId)) {
                          case Some(a) => complete(a.asJson)
                          case None    => complete(StatusCodes.NotFound)
                        }
                      } ~ post {
                        // TODO
                        complete(s"POST to update $entityType")
                      }
                  }
                } ~ pathPrefix(Segment) { aggregation =>
                  pathEnd {
                    get {
                      entityType match {
                        case "users" =>
                          aggregation match {
                            // TODO
                            case "visits" => {
                              complete(
                                s"$entityType $existingIdOrNew $aggregation requested")
                            }
                            case _ => complete(StatusCodes.NotFound)
                          }
                        case "locations" =>
                          aggregation match {
                            // TODO
                            case "avg" => {
                              complete(
                                s"$entityType $existingIdOrNew $aggregation requested")
                            }
                            case _ => complete(StatusCodes.NotFound)
                          }
                      }
                    }
                  }
                }
              case Failure(_) => complete(StatusCodes.NotFound)
            }
          }
        case _ => complete(StatusCodes.NotFound)
      }
    }

  // TODO
  private def jsonBodyValidation[T] = ???
}
