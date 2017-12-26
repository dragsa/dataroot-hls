package org.gnat.hls

import scala.util.{Failure, Success, Try}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.{JsonNumber, JsonObject}
import io.circe.syntax._
import org.gnat.hls.models._
import org.gnat.hls.utils.Utils._

//GET /<entity>/<id> для получения данных о сущности
//GET /users/<id>/visits для получения списка посещений пользователем
//GET /locations/<id>/avg для получения средней оценки достопримечательности
//POST /<entity>/<id> на обновление
//POST /<entity>/new на создание

trait ApiRouter extends HlsDatabase with FailFastCirceSupport {

  implicit def myRejectionHandler =
    RejectionHandler
      .newBuilder()
      .handle {
        case MalformedRequestContentRejection(msg, err) =>
          complete(StatusCodes.BadRequest, "wrong input data")
      }
      .result

  val route =
    pathSingleSlash {
      complete("root")
    } ~ {
      pathPrefix(Segment) {
        case entityType @ ("users" | "locations" | "visits") =>
          pathPrefix("new") {
            pathEnd {
              post {
                handleRejections(myRejectionHandler) {
                  entityType match {
                    case "users" =>
                      entity(as[User]) { user =>
                        onComplete(usersRepository.createOne(user)) {
                          case Success(_) => complete(JsonObject.empty)
                          case Failure(_) => complete(StatusCodes.BadRequest)
                        }
                      }
                    case "locations" =>
                      entity(as[Location]) { location =>
                        onComplete(locationsRepository.createOne(location)) {
                          case Success(_) => complete(JsonObject.empty)
                          case Failure(_) => complete(StatusCodes.BadRequest)
                        }
                      }
                    case "visits" =>
                      entity(as[Visit]) { visit =>
                        onComplete(visitsRepository.createOne(visit)) {
                          case Success(_) => complete(JsonObject.empty)
                          case Failure(_) => complete(StatusCodes.BadRequest)
                        }
                      }
                  }
                }
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
                        entity(as[UserUpdate]) { uDiff =>
                          onSuccess(usersRepository.getById(parsedId)) {
//                            case class User(firstName: String,
//                             lastName: String,
//                             birthDate: Timestamp,
//                             gender: String,
//                             email: String,
//                             id: Int)
                            case Some(uCurr) =>
                              val newUser = uCurr.copy(
                                uDiff.firstName.getOrElse(uCurr.firstName),
                                uDiff.lastName.getOrElse(uCurr.lastName),
                                uDiff.birthDate.getOrElse(uCurr.birthDate),
                                uDiff.gender.getOrElse(uCurr.gender),
                                uDiff.email.getOrElse(uCurr.email)
                              )
                              onSuccess(usersRepository.updateOne(newUser)) {
                                case 1 =>
                                  println(newUser + " updated")
                                  complete(JsonObject.empty)
                                case 0 => complete(StatusCodes.BadRequest)
                              }
                            case None => complete(StatusCodes.NotFound)
                          }
                        }
                      }
                    case "locations" =>
                      get {
                        onSuccess(locationsRepository.getById(parsedId)) {
                          case Some(a) => complete(a.asJson)
                          case None    => complete(StatusCodes.NotFound)
                        }
                      } ~ post {
                        entity(as[LocationUpdate]) { lDiff =>
                          onSuccess(locationsRepository.getById(parsedId)) {
//                            case class Location(distance: Int,
//                             city: String,
//                             place: String,
//                             country: String,
//                             id: Int)
                            case Some(lCurr) =>
                              val newLocation = lCurr.copy(
                                lDiff.distance.getOrElse(lCurr.distance),
                                lDiff.place.getOrElse(lCurr.place),
                                lDiff.city.getOrElse(lCurr.city),
                                lDiff.country.getOrElse(lCurr.country)
                              )
                              onSuccess(
                                locationsRepository.updateOne(newLocation)) {
                                case 1 =>
                                  println(newLocation + " updated")
                                  complete(JsonObject.empty)
                                case 0 => complete(StatusCodes.BadRequest)
                              }
                            case None => complete(StatusCodes.NotFound)
                          }
                        }
                      }
                    case "visits" =>
                      get {
                        onSuccess(visitsRepository.getById(parsedId)) {
                          case Some(a) => complete(a.asJson)
                          case None    => complete(StatusCodes.NotFound)
                        }
                      } ~ post {
                        entity(as[VisitUpdate]) { vDiff =>
                          onSuccess(visitsRepository.getById(parsedId)) {
//                           case class Visit(user: Int,
//                            location: Int,
//                            visitedAt: Timestamp,
//                            mark: Int,
//                            id: Int)
                            case Some(vCurr) =>
                              val newVisit = vCurr.copy(
                                vDiff.user.getOrElse(vCurr.user),
                                vDiff.location.getOrElse(vCurr.location),
                                vDiff.visitedAt.getOrElse(vCurr.visitedAt),
                                vDiff.mark.getOrElse(vCurr.mark)
                              )
                              onSuccess(visitsRepository.updateOne(newVisit)) {
                                case 1 =>
                                  println(newVisit + " updated")
                                  complete(JsonObject.empty)
                                case 0 => complete(StatusCodes.BadRequest)
                              }
                            case None => complete(StatusCodes.NotFound)
                          }
                        }
                      }
                  }
                } ~ pathPrefix(Segment) { aggregation =>
                  pathEnd {
                    get {
                      entityType match {
                        case "users" =>
                          parameters('fromDate.?,
                                     'toDate.?,
                                     'country.?,
                                     'toDistance.?) {
                            (fromDate, toDate, country, toDistance) =>
                              validate(
                                visitsParametersListValidation(fromDate,
                                                               toDate,
                                                               country,
                                                               toDistance),
                                "wrong input data") {
                                aggregation match {
                                  case "visits" =>
                                    onSuccess(usersRepository.getById(parsedId)) {
                                      case Some(_) =>
                                        onSuccess(visitsRepository
                                          .getByUserIdWithFilter(
                                            parsedId,
                                            optStringToOptLong(fromDate),
                                            optStringToOptLong(toDate),
                                            country.flatMap(c =>
                                              Option(java.net.URLDecoder
                                                .decode(c, "UTF-8"))),
                                            optStringToOptInt(toDistance)
                                          )) { result =>
                                          complete(JsonObject.fromMap(
                                            Map("visits" -> result
                                              .map(tup =>
                                                VisitByUser.apply _ tupled tup)
                                              .asJson)))
                                        }
                                      case None =>
                                        complete(StatusCodes.NotFound)
                                    }
                                  case _ => complete(StatusCodes.NotFound)
                                }
                              }
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
                                "wrong input data") {
                                aggregation match {
                                  case "avg" =>
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
//    println(validator)
    validator.isEmpty
  }

  val matchPattern = "([a-zA-Z ]+)".r

  private def visitsParametersListValidation(filters: Option[String]*) = {
    val mapOfParams =
      (List("fromDate", "toDate", "country", "toDistance") zip filters.toList).toMap
    val validator = List(
      mapOfParams("fromDate").flatMap(fd =>
        Option(Try(java.lang.Long.parseLong(fd)))),
      mapOfParams("toDate").flatMap(td =>
        Option(Try(java.lang.Long.parseLong(td)))),
//      mapOfParams("country").flatMap(g =>
//        g match {
//          case matchPattern(d) => Option(Success(g))
//          case _               => Option(Failure(new Throwable("country error")))
//      }),
      mapOfParams("toDistance").flatMap(ta =>
        Option(Try(Integer.parseInt(ta)))),
    ).flatten.collect { case a @ Failure(_) => a }
//    println(validator)
    validator.isEmpty
  }
}
