package lod2014group1.rdf

import org.joda.time.DateTime

case class RdfResource(val uri: String) extends RdfObject {
	def buildTriple(predicate: RdfResource, obj: RdfObject): RdfTriple = {
		RdfTriple(this, predicate, obj);
	}

	override def toString(): String = {
		"<" + uri + ">"
	}
}

class RdfMovieResource(resource: String) extends RdfResource(resource) {
	val releaseDateResource = new RdfResource("dbpediablabla/hasReleaseDate")

	def releasedOn(releaseDate: DateTime): RdfTriple = {
		this.buildTriple(releaseDateResource, RdfDate(releaseDate))
	}
}

object RdfMovieResource {
	implicit def fromRdfResource(resource: RdfResource): RdfMovieResource = {
		new RdfMovieResource(resource.uri)
	}
}
