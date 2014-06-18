package lod2014group1.rdf

import org.slf4s.Logging
import org.joda.time.DateTime


object RdfPersonResource {
	implicit def personResourceFromRdfResource(resource: RdfResource): RdfPersonResource = {
		new RdfPersonResource(resource.uri)
	}

	def actor: RdfResource = {
		RdfResource("dbpedia-owl:Actor")
	}

	def person: RdfResource = {
		RdfResource("dbpedia-owl:Person")
	}

	def director: RdfResource = {
		RdfResource("dbpedia-owl:director")
	}

	def writer: RdfResource = {
		RdfResource("dbpedia-owl:Writer")
	}

	def producer: RdfResource = {
		RdfResource("dbpedia-owl:producer")
	}

	def coProducer: RdfResource = {
		RdfResource("dbpedia-owl:coProducer")
	}

	def makeUpArtist: RdfResource = {
		RdfResource("dbpedia-owl:makeUpArtist")
	}

	def costumeDesigner: RdfResource = {
		RdfResource("dbpedia-owl:costumeDesigner")
	}

	def specialEffects: RdfResource = {
		RdfResource("dbpedia-owl:specialEffects")
	}

	def setDesigner: RdfResource = {
		RdfResource("dbpedia-owl:setDesigner")
	}

	def storyEditor: RdfResource = {
		RdfResource("dbpedia-owl:storyEditor")
	}
}

class RdfPersonResource(resource: String) extends RdfResource(resource) with Logging {

	def hasImdbUrl(id: String) = sameAs("http://imdb.com/name/" + id)

	def hasFreebaseId(url: String) = sameAs("http://imdb.com" + url)

	def born(date: DateTime): RdfTriple = buildTriple(RdfResource("dbpprop:birthDate"), RdfDate(date))

	def died(date: DateTime): RdfTriple = buildTriple(RdfResource("dbpprop:died"), RdfDate(date))

	def hasBirthName(name: String): RdfTriple = buildTriple(RdfResource("dbpprop:birthName"), RdfString(name))

	def hasBirthPlace(place: String): RdfTriple = buildTriple(RdfResource("dbpprop:birthPlace"), RdfString(place))

	def playsCharacter(character: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:character"), character)

	def hasJob(job: String): RdfTriple = buildTriple(RdfResource("dbpprop:job"), RdfString(job)).addAlways
}
