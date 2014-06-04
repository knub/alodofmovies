package lod2014group1.amqp.worker

import lod2014group1.database.Task

object WorkerTask {
	def fromDatabaseTask(dbTask: Task): WorkerTask = {
		WorkerTask(dbTask.taskType, dbTask.id, Map("uri" -> dbTask.fileOrUrl))
	}
}

case class WorkerTask(`type`: String, taskId: Long, params: Map[String, String])

