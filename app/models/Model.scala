package models

/**
  * Created by Rachel on 2016. 11. 19..
  */

import javax.inject.Inject
import java.sql.Timestamp
import java.util.Date

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global


case class Model(id: Long, name: Option[String], `type`: Option[String], createdAt: Timestamp, updatedAt: Timestamp)
case class SwitcherDetail(id: Long, switcherId: Option[Long], modelId: Option[Long], createdAt: Timestamp, updatedAt: Timestamp)
case class Switcher(id: Long, macAddress: Option[String], createdAt: Timestamp, updatedAt: Timestamp)

case class JoinResult(id: Long, `type`: String, modelId: Long, switcherId: Long, macAddress: String)

class ModelRepo @Inject()()(protected val dbConfigProvider: DatabaseConfigProvider) {

  var now = new Timestamp(new Date().getTime)
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db

  import dbConfig.driver.api._

  private val Models = TableQuery[ModelsTable]
  private val SwitcherDetails = TableQuery[SwitcherDetailsTable]
  private val Switchers = TableQuery[SwitchersTable]

  def all: Future[List[Model]] =
    db.run(Models.to[List].result)

  def create(id: Long, name: String, `type`: String): Future[Long] = {
    val model = Model(id, Some(name), Some(`type`), now, now)
    db.run(Models returning Models.map(_.id) += model)
  }

  def getJoinList = {
    val joinQuery = for {
      models <- Models
      details <- SwitcherDetails.filter(_.modelId === models.id)
      switchers <- Switchers.filter(_.id === details.switcherId)
    } yield (models, details, switchers)

    db.run(joinQuery.result).map { resultOption =>
      resultOption.map { case(m, d, s) =>
        JoinResult(
          m.id,
          m.`type`.get,
          d.modelId.get,
          d.switcherId.get,
          s.macAddress.get
        )
      }.distinct.toList
    }
  }

  private class ModelsTable(tag: Tag) extends Table[Model](tag, "Models") {

    def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
    def name = column[Option[String]]("name")
    def `type` = column[Option[String]]("type")
    def createdAt = column[Timestamp]("createdAt")
    def updatedAt = column[Timestamp]("updatedAt")

    def * = (id, name, `type`, createdAt, updatedAt) <> (Model.tupled, Model.unapply)
  }

  private class SwitcherDetailsTable(tag: Tag) extends Table[SwitcherDetail](tag, "SwitcherDetails") {

    def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
    def switcherId = column[Option[Long]]("switcherId")
    def modelId = column[Option[Long]]("modelId")
    def createdAt = column[Timestamp]("createdAt")
    def updatedAt = column[Timestamp]("updatedAt")

    def * = (id, switcherId, modelId, createdAt, updatedAt) <> (SwitcherDetail.tupled, SwitcherDetail.unapply)
  }

  private class SwitchersTable(tag: Tag) extends Table[Switcher](tag, "Switchers") {

    def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
    def macAddress = column[Option[String]]("macAddress")
    def createdAt = column[Timestamp]("createdAt")
    def updatedAt = column[Timestamp]("updatedAt")

    def * = (id, macAddress, createdAt, updatedAt) <> (Switcher.tupled, Switcher.unapply)
  }

}
