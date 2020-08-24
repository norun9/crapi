package controllers

import java.util._
import java.time._
import javax.inject.Inject
import models._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._
import scalikejdbc._

object CommentJsonController {
  // Comment一覧情報をJSONに変換するためのWritesを定義
  implicit val commentsWrites: Writes[Comment] = (
    (__ \ "id").write[String] and
      (__ \ "user_id").write[String] and
      (__ \ "text").write[String] and
      (__ \ "parent_post_id").write[String] and
      (__ \ "comment_count").write[Int] and
      (__ \ "posted_at").write[LocalDateTime]
  )(unlift(Comment.unapply))

  // Comment情報を受け取る為のケースクラス
  case class CommentForm(
      user_id: String,
      text: String
  )
  // CommentをJSONに変換するためのWritesを定義
  implicit val commentFormWrites: Writes[CommentForm] = (
    (__ \ "user_id").write[String] and
      (__ \ "text").write[String]
  )(unlift(CommentForm.unapply))

  // JSONをCommentFormに変換するためのReadsを定義
  implicit val commentFormReads: Reads[CommentForm] = (
    (__ \ "user_id").read[String] and
      (__ \ "text").read[String]
  )(CommentForm)

}

class CommentJsonController @Inject()(components: ControllerComponents)
    extends AbstractController(components) {

  import CommentJsonController._

  //任意のpost_idに紐づくComment一覧を取得
  def index(post_id: String) = Action { implicit request =>
    val comments = Comment.findAllComments(post_id)
    Ok(Json.obj("comments" -> Json.toJson(comments)))
  }

  //任意のpost_idに紐づくCommentを新規に作成
  def create(post_id: String) = Action(parse.json) { implicit request =>
    request.body
      .validate[CommentForm]
      .map { form =>
        DB.localTx { implicit session =>
          val uuid = UUID.randomUUID

          //post_idに紐づくPostが存在するかどうかを確認
          Post.findPost(post_id) match {
            case Some(post) =>
              //Formに送信されたuser_idがuserテーブルに存在するかどうか確認
              User.findUser(form.user_id) match {
                //Formに送信されたuser_idがuserテーブルに存在した場合
                case Some(user) =>
                  if (form.text.length == 0) {
                    //文字列長が0の状態
                    BadRequest(Json.obj(
                      "meta" -> Json.toJson(Meta(400, "Can't be registered with null text"))))
                  } else if (form.text.length > 100) {
                    //文字列長が100より長い状態
                    BadRequest(Json.obj("meta" -> Json.toJson(
                      Meta(400, "Can't be registered with more than 100 characters"))))
                  } else {
                    Comment.create(uuid.toString, form.user_id, form.text, post_id)
                    //post_idとPostのidが一致するレコードのcomment_countカラムの値を+1する
                    Post.addCommentCount(post_id)
                    Ok(Json.obj("result" -> "OK"))
                  }
                //Formに送信されたuser_idがuserテーブルに存在しなかった場合
                case None =>
                  BadRequest(Json.obj(
                    "meta" -> Json.toJson(Meta(400, s"user_id : ${form.user_id} not found"))))
              }

            case None =>
              //post_idに紐づくPostが存在しなかった場合、そのpost_idに紐づくCommentがあるかどうか確認
              Comment.findComment(post_id) match {
                case Some(comment) =>
                  //Formに送信されたuser_idがuserテーブルに存在するかどうか確認
                  User.findUser(form.user_id) match {
                    //Formに送信されたuser_idがuserテーブルに存在した場合
                    case Some(user) =>
                      if (form.text.length == 0) {
                        //文字列長が0の状態
                        BadRequest(Json.obj(
                          "meta" -> Json.toJson(Meta(400, "Can't be registered with null text"))))
                      } else if (form.text.length > 100) {
                        //文字列長が100より長い状態
                        BadRequest(Json.obj("meta" -> Json.toJson(
                          Meta(400, "Can't be registered with more than 100 characters"))))
                      } else {
                        Comment.create(uuid.toString, form.user_id, form.text, post_id)
                        //post_idとCommentのidが一致するレコードのcomment_countカラムの値を+1する
                        Comment.addCommentCount(post_id)
                        Ok(Json.obj("result" -> "OK"))
                      }
                    //Formに送信されたuser_idがuserテーブルに存在しなかった場合
                    case None =>
                      BadRequest(Json.obj(
                        "meta" -> Json.toJson(Meta(400, s"user_id : ${form.user_id} not found"))))
                  }
                // post_idに紐づくレコードがPostテーブルにもCommentテーブルにも存在しない場合
                case None =>
                  BadRequest(
                    Json.obj("meta" -> Json.toJson(Meta(400, s"post_id : ${post_id} not found"))))
              }
          }
        }
      }
      .recoverTotal { error =>
        // Formが妥当で無い場合バリデーションエラーを返す
        BadRequest(Json.obj("result" -> "Failure", "Error" -> JsError.toJson(error)))
      }
  }
}
