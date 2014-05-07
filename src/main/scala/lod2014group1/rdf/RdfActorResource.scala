package lod2014group1.rdf

import org.slf4s.Logging

object RdfActorResource {
	implicit def fromRdfResource(resource: RdfResource): RdfActorResource = {
		new RdfActorResource(resource.uri)
	}
}

class RdfActorResource(resource: String) extends RdfResource(resource) with Logging {

	def name(name: String): RdfTriple = buildTriple(RdfResource("dbpprop:name"), RdfString(name))

	def born(date: DateTime): RdfTriple = buildTriple(RdfResource("dbpprop:birthDate"), RdfDate(date))

	def hasBirthName(name: DateTime): RdfTriple = buildTriple(RdfResource("dbpprop:birthName"), RdfDate(date))

	def hasBirthPlace(date: DateTime): RdfTriple = buildTriple(RdfResource("dbpprop:birthPlace"), RdfDate(date))

}

