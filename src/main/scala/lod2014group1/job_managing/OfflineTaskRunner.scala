package lod2014group1.job_managing

import lod2014group1.messaging.worker.{TaskAnswer, WorkerTask}
import lod2014group1.messaging.{WorkReceiver, AnswerHandler}
import lod2014group1.database.TaskDatabase
import scala.pickling._
import json._

class OfflineTaskRunner {

	val answerHandler = new AnswerHandler
	val workReceiver = new WorkReceiver(null, null)
	val taskDatabase = new TaskDatabase

	def runTask(task: WorkerTask): Unit = {
		val answer = workReceiver.forwardTask(task)
		val pickled = answer.pickle.value.getBytes
		try {
			new String(pickled, "UTF-8").unpickle[TaskAnswer]
		} catch {
			case _: Throwable =>
				println("It failed for")
				println(answer.header)
				println(answer.taskId)
				println(answer.triples)
		}
//		answerHandler.handleAnswer(answer)
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
