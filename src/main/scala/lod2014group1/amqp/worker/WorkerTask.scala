package lod2014group1.amqp.worker

import lod2014group1.database.Task

object WorkerTask {
	def fromDatabaseTask(dbTask: Task): WorkerTask = {
		WorkerTask(dbTask.taskType, Map("task_id" -> dbTask.id.toString, "uri" -> dbTask.fileOrUrl))
	}
}

case class WorkerTask(`type`: String, params: Map[String, String])

