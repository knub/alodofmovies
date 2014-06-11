package lod2014group1.messaging.worker

import lod2014group1.database.Task
import lod2014group1.messaging.TaskType
import org.apache.commons.io.FileUtils
import java.io.File
import lod2014group1.Config

object WorkerTask {
	def fromDatabaseTask(dbTask: Task): WorkerTask = {
		if (TaskType.withName(dbTask.taskType) == TaskType.Triplify)
			WorkerTask(dbTask.taskType, dbTask.id, Map(
				"fileName" -> dbTask.fileOrUrl,
				"content" -> FileUtils.readFileToString(new File(s"${Config.DATA_FOLDER}/${dbTask.fileOrUrl}")))
			)
		else if (TaskType.withName(dbTask.taskType) == TaskType.Crawl)
			WorkerTask(dbTask.taskType, dbTask.id, Map("uri" -> dbTask.fileOrUrl))
		else
			throw new RuntimeException("Not handled yet.")

	}
}

case class WorkerTask(`type`: String, taskId: Long, params: Map[String, String])

