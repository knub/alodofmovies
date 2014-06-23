package lod2014group1.job_managing

import lod2014group1.messaging.worker.{TaskAnswer, WorkerTask}
import lod2014group1.messaging.{WorkReceiver, AnswerHandler}
import lod2014group1.database.TaskDatabase
import net.liftweb.json.Serialization.{read, write}
import org.slf4s.Logging

class OfflineTaskRunner extends Logging {

	val answerHandler = new AnswerHandler
	val workReceiver = new WorkReceiver(null, null)
	val taskDatabase = new TaskDatabase
	implicit val formats = net.liftweb.json.DefaultFormats

	def runTask(task: WorkerTask): Unit = {
		val answer = workReceiver.forwardTask(task)
		answerHandler.handleAnswer(answer)
	}

	def runTasks(n: Int): Unit = {
		val tasks = taskDatabase.getNextNTasks(n, 1586500)
		tasks.zipWithIndex.foreach { case (task, i) =>
				runTask(WorkerTask.fromDatabaseTask(task))
				if (task.id % 100 == 0)
					log.info(task.id.toString)
		}
	}

}
