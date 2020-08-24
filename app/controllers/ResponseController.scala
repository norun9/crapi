package controllers

import play.api.libs.functional.syntax.unlift
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Meta(status: Int, errorMessage: String)

object Meta {
  implicit val metaWrites = (
    (__ \ "status").write[Int] and
      (__ \ "errorMessage").write[String]
  )(unlift(Meta.unapply))
}
