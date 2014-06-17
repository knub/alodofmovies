package lod2014group1.job_managing

import lod2014group1.messaging.worker.WorkerTask
import lod2014group1.messaging.{WorkReceiver, AnswerHandler}
import lod2014group1.database.TaskDatabase

class OfflineTaskRunner {

	val answerHandler = new AnswerHandler
	val workReceiver = new WorkReceiver(null, null)
	val taskDatabase = new TaskDatabase

	def runTask(task: WorkerTask): Unit = {
		val answer = workReceiver.forwardTask(task)
		answerHandler.handleAnswer(answer)
	}

	def runTasks(n: Int): Unit = {
		val tasks = taskDatabase.getNextNTasks(n, 10000)
		tasks.zipWithIndex.foreach { case (task, i) =>
				runTask(WorkerTask.fromDatabaseTask(task))
				if (i % 100 == 0)
					println(i)
		}
	}

}
