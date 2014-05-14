package lod2014group1.rdf

import org.slf4s.Logging
import org.joda.time.DateTime


object RdfReleaseInfoResource {
	implicit def releaseInfoResourceFromRdfResource(resource: RdfResource): RdfReleaseInfoResource = {
		new RdfReleaseInfoResource(resource.uri)
	}

	def releaseInfo: RdfResource = {
		RdfResource("lod:ReleaseInfo")
	}
}

class RdfReleaseInfoResource(resource: String) extends RdfResource(resource) with Logging {

	def country(country: String): RdfTriple = buildTriple(RdfResource("dbpprop:country"), RdfString(country))

	def date(date: DateTime): RdfTriple = buildTriple(RdfResource("dbpprop:date"), RdfDate(date))

	def description(description: String): RdfTriple = buildTriple(RdfResource("dbpprop:description"), RdfString(description))
	
}
