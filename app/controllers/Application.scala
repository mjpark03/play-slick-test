package controllers

import models.{ModelRepo, Model}

import javax.inject.Inject

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller}
import play.api.libs.json._
import play.api.libs.json.Writes._
import scala.concurrent.Future


class Application @Inject()( modelRepo: ModelRepo)
  extends Controller {

  implicit val implicitFooWrites = new Writes[Model] {
    def writes(model: Model): JsValue = {
      Json.obj(
        "id" -> model.id,
        "name" -> model.name,
        "type" -> model.`type`
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
}
