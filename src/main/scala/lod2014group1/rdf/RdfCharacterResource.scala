package lod2014group1.rdf

import org.slf4s.Logging


object RdfCharacterResource {
	implicit def characterInfoResourceFromRdfResource(resource: RdfResource): RdfCharacterResource = {
		new RdfCharacterResource(resource.uri)
	}

	def character: RdfResource = {
		RdfResource("freebase:film/performance")
	}
}

class RdfCharacterResource(resource: String) extends RdfResource(resource) with Logging {

	def inMovie(movie: RdfResource): RdfTriple = buildTriple(RdfResource("freebase:film/performance/film"), movie)

	def playedBy(actor: RdfResource): RdfTriple = buildTriple(RdfResource("freebase:film/performance/actor"), actor)

}
