package lod2014group1.rdf

import org.slf4s.Logging

object RdfActorResource {
	implicit def fromRdfResource(resource: RdfResource): RdfActorResource = {
		new RdfActorResource(resource.uri)
	}
}

class RdfActorResource(resource: String) extends RdfResource(resource) with Logging {

	def name(name: String): RdfTriple = {
		log.warn("Predicate not set yet.")
		this.buildTriple(RdfResource("somerdfname"), RdfString(name))
	}
}

