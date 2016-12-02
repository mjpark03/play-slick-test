package controllers

import models.{ModelRepo, Model, JoinResult}

import javax.inject.Inject

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller}
import play.api.libs.json._
import play.api.libs.json.Writes._
import scala.concurrent.Future


class Application @Inject()( modelRepo: ModelRepo)
  extends Controller {

  implicit val implicitModelWrites = new Writes[Model] {
    def writes(model: Model): JsValue = {
      Json.obj(
        "id" -> model.id,
        "name" -> model.name,
        "type" -> model.`type`
      )
    }
  }

  implicit val implicitJoinWrites = new Writes[JoinResult] {
    def writes(item: JoinResult): JsValue = {
      Json.obj(
        "id" -> item.id,
        "type" -> item.`type`,
        "modelId" -> item.modelId,
        "switcherId" -> item.switcherId,
        "macAddress" -> item.macAddress
      )
    }
  }

  def listModels = Action.async {
    import play.api.libs.json.Json

    val future: Future[List[Model]] = modelRepo.all
    future.map { model =>
      Ok(Json.toJson(model))
    }
  }

  def listJoin = Action.async {
    import play.api.libs.json.Json

    val future: Future[List[JoinResult]] = modelRepo.getJoinList
    future.map { model =>
      Ok(Json.toJson(model))
    }
  }

  def createModel(id: Long, name: String) = Action.async {
    val `type` = "1"
    val successResponse = Map("status" -> "success")
    modelRepo.create(id, name, `type`)
      .map(id => Ok(Json.toJson(successResponse)))
  }
}
