package models

import java.util.UUID
import java.time.ZonedDateTime
import scalikejdbc._
import java.time._

case class Post(id: String = UUID.randomUUID.toString,
                user_id: String,
                text: String,
                comment_count: Int,
                posted_at: LocalDateTime)

object Post extends SQLSyntaxSupport[Post] {

  import Comment.c
  import nestComment.nc

  def apply(p: ResultName[Post])(rs: WrappedResultSet): Post =
    Post(
      id = rs.string(p.id),
      user_id = rs.string(p.user_id),
      text = rs.string(p.text),
      comment_count = rs.int(p.comment_count),
      posted_at = rs.localDateTime(p.posted_at)
    )
  def apply(p: SyntaxProvider[Post], rs: WrappedResultSet): Post = apply(p.resultName)(rs)

  val p = Post.syntax("p")

  // 任意のpost_idとidが一致するPostのレコードを取得
  def findPost(post_id: String = UUID.randomUUID.toString)(
      implicit session: DBSession = autoSession): Option[Post] = {
    withSQL {
      select.from(Post as p).where.eq(p.id, post_id)
    }.map(Post(p.resultName)).single.apply()
  }

  // 全投稿一覧を取得
  def findAllPosts(
      implicit session: DBSession = autoSession): Seq[(Post, Seq[Comment], Seq[nestComment])] = {
    withSQL[Post] {
      select
        .from(Post.as(p))
        .leftJoin(Comment.as(c))
        .on(p.id, c.parent_post_id)
        .leftJoin(nestComment.as(nc))
        .on(c.id, nc.parent_post_id)
        .orderBy(p.posted_at.desc)
    }.one(Post(p.resultName))
      .toManies(
        rs => rs.stringOpt(c.resultName.parent_post_id).map(_ => Comment(c)(rs)),
        rs => rs.stringOpt(nc.resultName.parent_post_id).map(_ => nestComment(nc)(rs)),
      )
      .map((post, comments, nestComments) => (post, comments, nestComments))
      .list
      .apply()
  }

  // 新規のPostを作成
  def create(id: String = UUID.randomUUID.toString, user_id: String, text: String)(
      implicit session: DBSession = autoSession): Unit = {
    withSQL {
      insert.into(Post).values(id, user_id, text, 0, ZonedDateTime.now())
    }.update.apply()
  }

  //親Postのcomment_countを+1
  def addCommentCount(post_id: String = UUID.randomUUID.toString) = {
    DB autoCommit { implicit session =>
      sql"""
        UPDATE post SET comment_count = comment_count + 1
        WHERE id = ${post_id}
      """.update.apply()
    }
  }

}
