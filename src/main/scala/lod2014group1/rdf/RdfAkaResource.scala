package lod2014group1.rdf

import org.slf4s.Logging
import org.joda.time.DateTime


object RdfAkaResource {
	implicit def alternativeMovieNameInfoResourceFromRdfResource(resource: RdfResource): RdfAkaResource = {
		new RdfAkaResource(resource.uri)
	}

	def alternativeMovieName: RdfResource = {
		RdfResource("lod:Aka")
	}
}

class RdfAkaResource(resource: String) extends RdfResource(resource) with Logging {

	def hasAkaName = name _

}
