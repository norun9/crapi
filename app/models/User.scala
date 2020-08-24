package models

import scalikejdbc._

case class User(id: String, name: String)

object User extends SQLSyntaxSupport[User] {

  val u = User.syntax("u")

  def apply(u: ResultName[User])(rs: WrappedResultSet): User =
    User(
      id = rs.string(u.id),
      name = rs.string(u.name)
    )
  def apply(u: SyntaxProvider[User], rs: WrappedResultSet): User = apply(u.resultName)(rs)

  // user_idとidが一致するUserのレコードを取得
  def findUser(user_id: String)(implicit session: DBSession = autoSession): Option[User] =
    DB readOnly { implicit session =>
      withSQL {
        select.from(User as u).where.eq(u.id, user_id)
      }.map(User(u.resultName)).single.apply()
    }
}
