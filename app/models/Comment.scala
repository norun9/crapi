package models

import java.time._
import java.util._
import scalikejdbc._

// Commentがネストする子Comment
case class nestComment(
    id: String = UUID.randomUUID.toString,
    user_id: String,
    text: String,
    parent_post_id: String,
    comment_count: Int,
    posted_at: LocalDateTime
)

object nestComment extends SQLSyntaxSupport[nestComment] {

  var nc = nestComment.syntax("nc")

  override val tableName = "comment"

  override val columns =
    Seq("id", "user_id", "text", "parent_post_id", "comment_count", "posted_at")

  def apply(nc: ResultName[nestComment])(rs: WrappedResultSet): nestComment = new nestComment(
    id = rs.string(nc.id),
    user_id = rs.string(nc.user_id),
    text = rs.string(nc.text),
    parent_post_id = rs.string(nc.parent_post_id),
    comment_count = rs.int(nc.comment_count),
    posted_at = rs.localDateTime(nc.posted_at)
  )
  def apply(nc: SyntaxProvider[nestComment])(rs: WrappedResultSet): nestComment =
    apply(nc.resultName)(rs)
}

case class Comment(
    id: String = UUID.randomUUID.toString,
    user_id: String,
    text: String,
    parent_post_id: String,
    comment_count: Int,
    posted_at: LocalDateTime
)

object Comment extends SQLSyntaxSupport[Comment] {

  def apply(c: ResultName[Comment])(rs: WrappedResultSet): Comment = new Comment(
    id = rs.string(c.id),
    user_id = rs.string(c.user_id),
    text = rs.string(c.text),
    parent_post_id = rs.string(c.parent_post_id),
    comment_count = rs.int(c.comment_count),
    posted_at = rs.localDateTime(c.posted_at)
  )
  def apply(c: SyntaxProvider[Comment])(rs: WrappedResultSet): Comment = apply(c.resultName)(rs)

  var c = Comment.syntax("c")

  //post_idとidが一致するPostかCommentに対する全投稿を取得
  def findAllComments(post_id: String = UUID.randomUUID.toString)(implicit session: DBSession =
                                                                    autoSession): Seq[Comment] = {
    withSQL {
      select.from(Comment as c).where.eq(c.parent_post_id, post_id).orderBy(c.posted_at.desc)
    }.map(Comment(c.resultName))
      .list
      .apply()
  }

  // 任意のcomment_idとidが一致するCommentのレコードを取得
  def findComment(comment_id: String = UUID.randomUUID.toString)(implicit session: DBSession =
                                                                   autoSession): Option[Comment] = {
    withSQL {
      select.from(Comment as c).where.eq(c.id, comment_id)
    }.map(Comment(c.resultName)).single.apply()
  }

  // 特定のPostに紐付いた新規のCommentを作成
  def create(id: String = UUID.randomUUID.toString,
             user_id: String,
             text: String,
             parent_post_id: String = UUID.randomUUID.toString)(implicit session: DBSession =
                                                                  autoSession): Unit = {
    withSQL {
      insert.into(Comment).values(id, user_id, text, parent_post_id, 0, ZonedDateTime.now())
    }.update.apply()
  }

  //親Commentのcomment_countを+1
  def addCommentCount(comment_id: String = UUID.randomUUID.toString) =
    DB autoCommit { implicit session =>
      sql"""
       UPDATE comment SET comment_count = comment_count + 1
       WHERE id = ${comment_id}
      """.update.apply()
    }

}
