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

	def atDate(dateStr: String) = dateRes(dateStr)
	def atDate(dateObj: DateTime) = dateRes(dateObj)

}
