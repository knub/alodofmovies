package lod2014group1.messaging.worker

import lod2014group1.merging.MovieMerger
import lod2014group1.triplification.TriplifyDistributor


class MatchWorker extends Worker {

  def execute(taskId: Long, params: Map[String, String]): TaskAnswer = {
    val fileName = params("fileName")
    val content = params("content")
    val graph = params("graph")

    val triplifier = new TriplifyDistributor
    val triples = triplifier.triplify(fileName, content).map { _.toRdfTripleString() }

    // TODO match movie

    val answerMap: Map[String, String] = Map("graph" -> graph)
    new TaskAnswer(taskId, answerMap, Nil, triples)
  }

}