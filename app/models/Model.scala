package models

/**
  * Created by Rachel on 2016. 11. 19..
  */

import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import scala.concurrent.Future


case class Model(id: Long, name: String, `type`: String)

class ModelRepo @Inject()()(protected val dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.driver.api._
  private val Models = TableQuery[ModelsTable]

  def all: Future[List[Model]] =
    db.run(Models.to[List].result)

  private class ModelsTable(tag: Tag) extends Table[Model](tag, "Models") {

    def id = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    def name = column[String]("NAME")
    def `type` = column[String]("TYPE")

    def * = (id, name, `type`) <> (Model.tupled, Model.unapply)
  }

}
