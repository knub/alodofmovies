package lod2014group1.job_managing

import lod2014group1.messaging.worker.WorkerTask
import lod2014group1.messaging.{WorkReceiver, AnswerHandler}
import lod2014group1.database.TaskDatabase
import scala.slick.driver.MySQLDriver.simple._
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
//		val tasks =  taskDatabase.runInDatabase { tasks => implicit session =>
//			tasks.sortBy(t => t.id).take(n).list()
//		}
		val tasks = taskDatabase.getNextNTasks(n, 0)

		tasks.zipWithIndex.foreach { case (task, i) =>
				runTask(WorkerTask.fromDatabaseTask(task))
				if (task.id % 1000 == 0)
					log.info(task.id.toString)
		}
		//answerHandler.finish()
	}

}
