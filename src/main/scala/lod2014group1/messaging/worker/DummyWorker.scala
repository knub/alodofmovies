package lod2014group1.messaging.worker

import org.slf4s.Logging

class DummyWorker extends Worker with Logging {
	def execute(taskId: Long, params: Map[String, String]): TaskAnswer = {
		log.error("Using a dummy work, doing no actual work.")
		throw new DummyWorkerException()
	}
}

class DummyWorkerException extends RuntimeException { }
