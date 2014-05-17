package lod2014group1.rdf

import org.slf4s.Logging
import org.joda.time.DateTime


object RdfAwardResource {
	implicit def awardResourceFromRdfResource(resource: RdfResource): RdfAwardResource = {
		new RdfAwardResource(resource.uri)
	}

	def award: RdfResource = {
		RdfResource("dbpedia-owl:Award")
	}
}

class RdfAwardResource(resource: String) extends RdfResource(resource) with Logging {

	def hasOutcome(outcome: String): RdfTriple = buildTriple(RdfResource("lod:outcome"), RdfString(outcome))

	def hasDetails(details: String): RdfTriple = buildTriple(RdfResource("dbpprop:details"), RdfString(details))

	def inCategory(category: String): RdfTriple = buildTriple(RdfResource("lod:awardCategory"), RdfString(category))

	def forNominee(nominee: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:nominee"), nominee)

	def withRole(role: String): RdfTriple = buildTriple(RdfResource("lod:role"), RdfString(role))
}
