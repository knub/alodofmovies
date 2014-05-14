package lod2014group1.rdf

import org.slf4s.Logging
import org.joda.time.DateTime


object RdfAlternativeMovieNamesResource {
	implicit def alternativeMovieNameInfoResourceFromRdfResource(resource: RdfResource): RdfAlternativeMovieNamesResource = {
		new RdfAlternativeMovieNamesResource(resource.uri)
	}

	def alternativeMovieName: RdfResource = {
		RdfResource("lod:AlternativeMovieName")
	}
}

class RdfAlternativeMovieNamesResource(resource: String) extends RdfResource(resource) with Logging {

	def country(country: String): RdfTriple = buildTriple(RdfResource("dbpprop:country"), RdfString(country))

	def alternativeName(name: String): RdfTriple = buildTriple(RdfResource("dbpprop:alternativeNames"), RdfString(name))

	def description(description: String): RdfTriple = buildTriple(RdfResource("dbpprop:description"), RdfString(description))

}
