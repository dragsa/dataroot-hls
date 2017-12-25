package org.gnat.hls

import scala.util.{Failure, Success, Try}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.{JsonNumber, JsonObject}
import io.circe.syntax._

import org.gnat.hls.utils.Utils._

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
                // TODO call validation here
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
//  GET-параметры:
//  fromDate - посещения с visited_at > fromDate
//  toDate - посещения до visited_at < toDate
//  country - название страны, в которой находятся интересующие достопримечательности
//  toDistance - возвращать только те места, у которых расстояние от города меньше этого параметра
                          aggregation match {
                            // TODO add filters!
                            case "visits" => {
                              onSuccess(visitsRepository.getByUserId(parsedId)) {
                                result =>
                                  complete(JsonObject.fromMap(
                                    Map("visits" -> result.asJson)))
                              }
                            }
                            case _ => complete(StatusCodes.NotFound)
                          }
                        case "locations" =>
                          parameters('fromDate.?,
                                     'toDate.?,
                                     'fromAge.?,
                                     'toAge.?,
                                     'gender.?) {
                            (fromDate, toDate, fromAge, toAge, gender) =>
                              validate(
                                locationAvgParametersListValidation(fromDate,
                                                                    toDate,
                                                                    fromAge,
                                                                    toAge,
                                                                    gender),
                                "wrong imput data") {
                                aggregation match {
                                  case "avg" => {
                                    onSuccess(
                                      locationsRepository.getById(parsedId)) {
                                      case Some(_) =>
                                        onSuccess(
                                          visitsRepository
                                            .getLocationMarksByIdWithFilter(
                                              parsedId,
                                              optStringToOptLong(fromDate),
                                              optStringToOptLong(toDate),
                                              optStringToOptInt(fromAge),
                                              optStringToOptInt(toAge),
                                              gender
                                            )) {
                                          case Some(avg) =>
                                            complete(
                                              JsonObject.fromMap(
                                                Map("avg" -> JsonNumber
                                                  .fromDecimalStringUnsafe(
                                                    "%.5f".format(avg))
                                                  .asJson)))
                                          case None =>
                                            complete(JsonObject.fromMap(
                                              Map("avg" -> JsonNumber
                                                .fromDecimalStringUnsafe("0.0")
                                                .asJson)))
                                        }
                                      case None =>
                                        complete(StatusCodes.NotFound)
                                    }
                                  }
                                  case _ => complete(StatusCodes.NotFound)
                                }
                              }
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

  private def locationAvgParametersListValidation(filters: Option[String]*) = {
    val mapOfParams =
      (List("fromDate", "toDate", "fromAge", "toAge", "gender") zip filters.toList).toMap
    val validator = List(
      mapOfParams("fromDate").flatMap(fd =>
        Option(Try(java.lang.Long.parseLong(fd)))),
      mapOfParams("toDate").flatMap(td =>
        Option(Try(java.lang.Long.parseLong(td)))),
      mapOfParams("fromAge").flatMap(fa => Option(Try(Integer.parseInt(fa)))),
      mapOfParams("toAge").flatMap(ta => Option(Try(Integer.parseInt(ta)))),
      mapOfParams("gender").flatMap(g =>
        g match {
          case "m" | "f" => Option(Success(g))
          case _         => Option(Failure(new Throwable("gender error")))
      })
    ).flatten.collect { case a @ Failure(_) => a }
    println(validator)
    validator.isEmpty
  }
}
