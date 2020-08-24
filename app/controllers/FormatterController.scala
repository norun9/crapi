package controllers

import models.{ nestComment, Comment, Post }
import play.api.libs.json._

// 全投稿一覧情報のJSONコンバーター
object FormatterController {
  implicit val nestCommentFormat = Json.format[nestComment]
  implicit val commentFormat = Json.format[Comment]
  implicit val postFormat = Json.format[Post]
}
