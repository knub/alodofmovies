package lod2014group1.rdf

import org.slf4s.Logging
import org.joda.time.DateTime


object RdfActorResource {
	implicit def actorResourceFromRdfResource(resource: RdfResource): RdfActorResource = {
		new RdfActorResource(resource.uri)
	}

	def actor: RdfResource = {
		RdfResource("dbpedia-owl:Actor")
	}
}

class RdfActorResource(resource: String) extends RdfResource(resource) with Logging {

	def hasImdbUrl(url: String) = sameAs("http://imdb.com" + url)

	def born(date: DateTime): RdfTriple = buildTriple(RdfResource("dbpprop:birthDate"), RdfDate(date))

	def hasBirthName(name: String): RdfTriple = buildTriple(RdfResource("dbpprop:birthName"), RdfString(name))

	def hasBirthPlace(place: String): RdfTriple = buildTriple(RdfResource("dbpprop:birthPlace"), RdfString(place))

	def playsCharacter(character: RdfResource): RdfTriple = buildTriple(RdfResource("freebase:film/actor/film"), character)

}
