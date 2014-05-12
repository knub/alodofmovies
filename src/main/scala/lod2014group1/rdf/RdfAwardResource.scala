package lod2014group1.rdf

import org.slf4s.Logging
import org.joda.time.DateTime


object RdfAwardResource {
	implicit def fromRdfResource(resource: RdfResource): RdfAwardResource = {
		new RdfAwardResource(resource.uri)
	}

	def award: RdfResource = {
		RdfResource("dbpedia-owl:Award")
	}
}

class RdfAwardResource(resource: String) extends RdfResource(resource) with Logging {

	def country(country: String): RdfTriple = buildTriple(RdfResource("dbpprop:country"), RdfString(country))

	def year(year: String): RdfTriple = buildTriple(RdfResource("dbpprop:years"), RdfString(year))

	def outcome(outcome: String): RdfTriple = buildTriple(RdfResource("lod:outcome"), RdfString(outcome))

	def category(category: String): RdfTriple = buildTriple(RdfResource("lod:awardCategory"), RdfString(category))

	def description(description: String): RdfTriple = buildTriple(RdfResource("dbpedia-owl:abstact"), RdfString(description))

	def details(details: String): RdfTriple = buildTriple(RdfResource("dbpprop:description"), RdfString(details))

	def nominee(nominee: RdfResource): RdfTriple = buildTriple(RdfResource("lod:nominee"), nominee)
}
