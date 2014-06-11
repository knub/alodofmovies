package lod2014group1.messaging.worker

import lod2014group1.rdf.RdfTripleString

case class TaskAnswer(taskId: Long, header: Map[String, String], files: List[UriFile], triples: List[RdfTripleString])
