package lod2014group1.triplification

import java.io.{FileReader, File}
import lod2014group1.rdf.{UriBuilder, RdfPersonResource, RdfTriple}
import net.liftweb.json.JsonParser
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime

class TMDBPersonTriplifier {

	def triplify(f: File): List[RdfTriple] = {
		implicit val formats = net.liftweb.json.DefaultFormats
		val personJson: TmdbPersonJson = JsonParser.parse(new FileReader(f)).extract[TmdbPersonJson]

		val formatter = DateTimeFormat.forPattern("YYYY-MM-dd")

		val id = personJson.id
		val person = new RdfPersonResource(UriBuilder.getPersonUriFromTmdbId(id))
		List(person isA RdfPersonResource.person, person hasLabel personJson.name,
			person sameAs UriBuilder.getTmdbPersonUri(id)) :::
			handleAlternativeNames(person, personJson.also_known_as) :::
			addString(person.hasName(_: String), personJson.name) :::
			addString(person.abstractContent(_: String), personJson.biography) :::
			addDate(person.born(_: DateTime), personJson.birthday) :::
			addDate(person.died(_: DateTime), personJson.deathday) :::
			addString(person.hasBirthPlace(_: String), personJson.place_of_birth) :::
			addImdbId(person.sameAs(_: String), personJson.imdb_id) :::
			addFreebaseId(person.sameAs(_: String), personJson.imdb_id)
	}

	def addString(predicate: String => RdfTriple, obj: String): List[RdfTriple] = {
		if (obj != null && obj != "" ){
			List(predicate(obj))
		} else {
			Nil
		}
	}

	def addDate(predicate: DateTime => RdfTriple, dateString: String): List[RdfTriple] = {
		if (dateString != null && dateString != "" ){
			val formatter = DateTimeFormat.forPattern("YYYY-MM-dd")
			val date = formatter.parseDateTime(dateString)
			List(predicate(date))
		} else {
			Nil
		}
	}

	def addImdbId(predicate: String => RdfTriple, id: String): List[RdfTriple] = {
		if (id != null && id != "" ){
			List(predicate(UriBuilder.getPersonUriFromImdbId(id)))
		} else {
			Nil
		}
	}

	def addFreebaseId(predicate: String => RdfTriple, id: String): List[RdfTriple] = {
		if (id != null && id != "" ){
			List(predicate(UriBuilder.getFreebaseUri(id)))
		} else {
			Nil
		}
	}

	def handleAlternativeNames(person: RdfPersonResource, akas: List[String]): List[RdfTriple] = {
		akas.map { name =>
			person hasAlternativeName name
		}
	}
}
