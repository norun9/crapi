package controllers

import java.util._

import javax.inject.Inject
import models._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Json
import scalikejdbc._

object PostJsonController {

  //Post情報を受け取る為のケースクラス
  case class PostForm(user_id: String, text: String)

  // PostをJSONに変換するためのWritesを定義
  implicit val postFormWrites: Writes[PostForm] = (
    (__ \ "user_id").write[String] and
      (__ \ "text").write[String]
  )(unlift(PostForm.unapply))

  // JSONをPostFormに変換するためのReadsを定義
  implicit val postFormReads: Reads[PostForm] = (
    (__ \ "user_id").read[String] and
      (__ \ "text").read[String]
  )(PostForm)

}

class PostJsonController @Inject()(components: ControllerComponents)
    extends AbstractController(components) {

  import PostJsonController._
  import FormatterController._

  //全投稿一覧を取得
  def index = Action { implicit request =>
    val posts = Post.findAllPosts
    Ok(Json.obj("posts" -> Json.toJson(posts)))
  }

  //新規のPostを作成
  def create = Action(parse.json) { implicit request =>
    request.body
      .validate[PostForm]
      .map { form =>
        DB.localTx { implicit session =>
          //Formに送信されたuser_idがuserテーブルに存在するかどうか確認
          User.findUser(form.user_id) match {
            case Some(user) =>
              if (form.text.length == 0) {
                //文字列長が0の状態
                BadRequest(
                  Json.obj("meta" -> Json.toJson(Meta(400, "Can't be registered with null text"))))
              } else if (form.text.length > 100) {
                //文字列長が100より長い状態
                BadRequest(Json.obj("meta" -> (Json.toJson(
                  Meta(400, "Can't be registered with more than 100 characters")))))
              } else {
                val uuid = UUID.randomUUID
                Post.create(uuid.toString, form.user_id, form.text)
                Ok(Json.obj("result" -> "OK"))
              }

            case None =>
              //Formに送信されたuser_idがuserテーブルに存在しなかった場合
              BadRequest(Json.obj(
                "meta" -> (Json.toJson(Meta(400, s"user_id : ${form.user_id} not found")))))
          }
        }
      }
      .recoverTotal { error =>
        // Formが妥当で無い場合バリデーションエラーを返す
        BadRequest(Json.obj("result" -> "Failure", "Error" -> JsError.toJson(error)))
      }
  }
}
