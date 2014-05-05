package lod2014group1.rdf

import org.joda.time.DateTime

case class RDFResource(val uri: String) extends RDFObject {
	def buildTriple(predicate: RDFResource, obj: RDFObject): RDFTriple = {
		RDFTriple(this, predicate, obj);
	}

	override def toString(): String = {
		"<" + uri + ">"
	}
}

class RDFMovieResource(resource: String) extends RDFResource(resource) {
	val releaseDateResource = new RDFResource("dbpediablabla/hasReleaseDate")

	def releasedOn(releaseDate: DateTime): RDFTriple = {
		this.buildTriple(releaseDateResource, RDFDate(releaseDate))
	}
}

object RDFMovieResource {
	implicit def fromRDFResource(resource: RDFResource): RDFMovieResource = {
		new RDFMovieResource(resource.uri)
	}
}
