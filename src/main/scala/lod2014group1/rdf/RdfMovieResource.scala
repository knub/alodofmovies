package lod2014group1.rdf

import org.slf4s.Logging
import org.joda.time.DateTime

object RdfMovieResource {
	implicit def movieResourceFromRdfResource(resource: RdfResource): RdfMovieResource = {
		new RdfMovieResource(resource.uri)
	}

	def film: RdfResource = {
		RdfResource("dbpedia-owl:Film")
	}

	def blackAndWhiteFilm(): RdfResource = {
		RdfResource("category:Black-and-white_films")
	}

	def fromImdbId(id: String): RdfResource = {
		RdfResource(s"lod:Movie$id")
	}

	def colorFilm(): RdfResource = {
		RdfResource("lod:category/color_films")
	}
}

class RdfMovieResource(resource: String) extends RdfResource(resource) with Logging {

	def hasTitle = name _
	def releasedInYear = year _
	def releasedInCountry = country _
	def hasShortSummary = abstractContent _
	def hasStoryLine = description _
	def belongsTo = subject _
	def hasPoster = image _

	def sameAsImdbUrl(id: String) = sameAs("http://imdb.com/title/tt" + id)

	def alsoKnownAs(aka: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:alternativeNames"), aka)

	def hasReleaseInfo(releaseInfo: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:released"), releaseInfo)

	def ageRating(age: String): RdfTriple = buildTriple(RdfResource("dbpprop:ageRating"), RdfString(age))

	def isPartOf(collection: String): RdfTriple = buildTriple(RdfResource("freebase:film/film/film_series"), RdfString(collection))

	def nextMovie(movie: RdfResource): RdfTriple = buildTriple(RdfResource("freebase:film/film/sequel"), movie)

	def previousMovie(movie: RdfResource): RdfTriple = buildTriple(RdfResource("freebase:film/film/prequel"), movie)

	def hasGenre(genre: String): RdfTriple = buildTriple(RdfResource("dbpprop:genre"), RdfString(genre))

	def shotInLanguage(language: String): RdfTriple = buildTriple(RdfResource("dbpprop:language"), RdfString(language))

	def lasts(runtime: Integer): RdfTriple = buildTriple(RdfResource("dbpprop:runtime"), RdfInteger(runtime))

	def rated(rating: String): RdfTriple = buildTriple(RdfResource("dbpprop:rated"), RdfString(rating))

	def scored(score: String): RdfTriple = buildTriple(RdfResource("lod:metascore"), RdfString(score))

	def releasedOn(releaseDate: DateTime): RdfTriple = buildTriple(RdfResource("dbpprop:initialRelease"), RdfDate(releaseDate))
	def releasedOn(releaseDate: String): RdfTriple = buildTriple(RdfResource("dbpprop:initialRelease"), RdfString(releaseDate))

	def hasKeyword(keyword: String): RdfTriple = buildTriple(RdfResource("lod:keyword"), RdfString(keyword))

	def directedBy(director: String): RdfTriple = buildTriple(RdfResource("dbpprop:director"), RdfString(director))
	def directedBy(director: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:director"), director)

	def writtenBy(writer: String): RdfTriple = buildTriple(RdfResource("dbpprop:writer"), RdfString(writer))
	def writtenBy(writer: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:writer"), writer)

	def screenplayBy(writer: String): RdfTriple = buildTriple(RdfResource("dbpprop:screenplay"), RdfString(writer))
	def screenplayBy(writer: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:screenplay"), writer)

	def stroyBy(writer: String): RdfTriple = buildTriple(RdfResource("dbpprop:story"), RdfString(writer))
	def stroyBy(writer: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:story"), writer)

	def novelBy(writer: String): RdfTriple = buildTriple(RdfResource("dbpprop:author"), RdfString(writer))
	def novelBy(writer: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:author"), writer)

	def playedBy(actor: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:starring"), actor)

	def producedBy(producer: String): RdfTriple = buildTriple(RdfResource("dbpprop:producer"), RdfString(producer))
	def producedBy(producer: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:producer"), producer)

	def coProducedBy(producer: String): RdfTriple = buildTriple(RdfResource("dbpprop:coProducer"), RdfString(producer))
	def coProducedBy(producer: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:coProducer"), producer)

	def setDesignedBy(setDesigner: String): RdfTriple = buildTriple(RdfResource("dbpprop:setDecorator"), RdfString(setDesigner))
	def setDesignedBy(setDesigner: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:setDecorator"), setDesigner)

	def makeupBy(makeupArtist: String): RdfTriple = buildTriple(RdfResource("dbpprop:makeupArtist"), RdfString(makeupArtist))
	def makeupBy(makeupArtist: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:makeupArtist"), makeupArtist)

	def specialEffectsBy(person: String): RdfTriple = buildTriple(RdfResource("dbpprop:specialEffects"), RdfString(person))
	def specialEffectsBy(person: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:specialEffects"), person)

	def visualEffectsBy(person: String): RdfTriple = buildTriple(RdfResource("dbpprop:visualEffects"), RdfString(person))
	def visualEffectsBy(person: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:visualEffects"), person)

	def editBy(editor: String): RdfTriple = buildTriple(RdfResource("dbpprop:editing"), RdfString(editor))
	def editBy(editor: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:editing"), editor)

	def costumeDesignedBy(costumeDesigner: String): RdfTriple = buildTriple(RdfResource("dbpprop:costume"), RdfString(costumeDesigner))
	def costumeDesignedBy(costumeDesigner: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:costume"), costumeDesigner)

	def cinematographyBy(cinematographer: String): RdfTriple = buildTriple(RdfResource("dbpprop:cinematography"), RdfString(cinematographer))
	def cinematographyBy(cinematographer: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:cinematography"), cinematographer)

	def musicBy(musicer: String): RdfTriple = buildTriple(RdfResource("dbpprop:music"), RdfString(musicer))
	def musicBy(musicer: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:music"), musicer)

	def castingBy(casting: String): RdfTriple = buildTriple(RdfResource("dbpprop:casting"), RdfString(casting))
	def castingBy(casting: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:casting"), casting)

	def stuntsBy(stunt: String): RdfTriple = buildTriple(RdfResource("dbpprop:stunts/acting"), RdfString(stunt))
	def stuntsBy(stunt: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:stunts/acting"), stunt)

	def productionDesignBy(designer: String): RdfTriple = buildTriple(RdfResource("dbpprop:productionDesigner"), RdfString(designer))
	def productionDesignBy(designer: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:productionDesigner"), designer)

	def artDirector(director: String): RdfTriple = buildTriple(RdfResource("dbpprop:artDirector"), RdfString(director))
	def artDirector(director: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:artDirector"), director)

	def belongsToOtherCrew(person: String): RdfTriple = buildTriple(RdfResource("dbpprop:otherCrew"), RdfString(person))
	def belongsToOtherCrew(person: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:otherCrew"), person)

	def distributedBy(distributor: String): RdfTriple = buildTriple(RdfResource("dbpprop:distributor"), RdfString(distributor))

	def hasPhotos(photos: String): RdfTriple = buildTriple(RdfResource("dbpprop:hasPhotoCollection"), RdfUrl(photos))

	def hasVideo(video: String): RdfTriple = buildTriple(RdfResource("dbpprop:video"), RdfUrl(video))

	def hasWebsite(website: String): RdfTriple = buildTriple(RdfResource("dbpprop:website"), RdfUrl(website))

	def filmedInLocation(location: String): RdfTriple = buildTriple(RdfResource("dbpprop:location"), RdfString(location))

	def hasBudget(budget: String): RdfTriple = buildTriple(RdfResource("dbpprop:budget"), RdfString(budget))

	def hasSoundMix(soundMix: String): RdfTriple = buildTriple(RdfResource("lod:soundmix"), RdfString(soundMix))

	def hasAspectRatio(ratio: String): RdfTriple = buildTriple(RdfResource("lod:aspectRatio"), RdfString(ratio))

	def hasTagline(tagline: String): RdfTriple = buildTriple(RdfResource("dbpprop:tagline"), RdfString(tagline))

	def hasRevenue(revenue: Integer): RdfTriple = buildTriple(RdfResource("dbpprop:revenue"), RdfInteger(revenue))

	def isAdult(adult: Boolean): RdfTriple = buildTriple(RdfResource("lod:adult"), RdfBoolean(adult))

	def hasOriginalTitle(originalTitle: String): RdfTriple = buildTriple(RdfResource("dbpprop:originalTitle"), RdfString(originalTitle))

	def hasReleaseStatus(releaseStatus: String): RdfTriple = buildTriple(RdfResource("dbpprop:status"), RdfString(releaseStatus))

}
