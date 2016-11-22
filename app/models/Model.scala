package models

/**
  * Created by Rachel on 2016. 11. 19..
  */

import javax.inject.Inject
import java.sql.Timestamp
import java.util.Date

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import scala.concurrent.Future


case class Model(id: Long, name: String, `type`: String, createdAt: Timestamp, updatedAt: Timestamp)

class ModelRepo @Inject()()(protected val dbConfigProvider: DatabaseConfigProvider) {

  var now = new Timestamp(new Date().getTime)
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.driver.api._
  private val Models = TableQuery[ModelsTable]

  def all: Future[List[Model]] =
    db.run(Models.to[List].result)

  def create(id: Long, name: String, `type`: String): Future[Long] = {
    val model = Model(id, name, `type`, now, now)
    db.run(Models returning Models.map(_.id) += model)
  }

  private class ModelsTable(tag: Tag) extends Table[Model](tag, "Models") {

    def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
    def name = column[String]("name")
    def `type` = column[String]("type")
    def createdAt = column[Timestamp]("createdAt")
    def updatedAt = column[Timestamp]("updatedAt")

    def * = (id, name, `type`, createdAt, updatedAt) <> (Model.tupled, Model.unapply)
  }

}
