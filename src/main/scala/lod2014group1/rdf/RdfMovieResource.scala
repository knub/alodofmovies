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

	def blackAndWhite(): RdfResource = {
		RdfResource("category:Black-and-white_films")
	}
}

class RdfMovieResource(resource: String) extends RdfResource(resource) with Logging {
	/*
		PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
		PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
		PREFIX dbpprop: <http://dbpedia.org/property/>
		PREFIX owl: <http://www.w3.org/2002/07/owl#>
		PREFIX dcterms: <http://dublincore.org/2010/10/11/dcterms.rdf#>
		PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>
		PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
		PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
		PREFIX foaf: <http://xmlns.com/foaf/0.1/>
	*/


	def hasTitle = name _
	def releasedInYear = year _
	def releasedInCountry = country _
	def shotIn = subject _
	def hasShortSummary = abstractContent _
	def hasStoryLine = description _


	def alsoKnownAs(aka: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:alternativeNames"), aka)

	def hasReleaseInfo(releaseInfo: RdfResource): RdfTriple = buildTriple(RdfResource("dbpprop:released"), releaseInfo)

	def isPartOf(collection: String): RdfTriple = buildTriple(RdfResource("freebase:film/film/film_series"), RdfString(collection))

	def nextMovie(movie: RdfResource): RdfTriple = buildTriple(RdfResource("freebase:film/film/sequel"), movie)

	def previousMovie(movie: RdfResource): RdfTriple = buildTriple(RdfResource("freebase:film/film/prequel"), movie)

	def hasGenre(genre: String): RdfTriple = buildTriple(RdfResource("dbpprop:genre"), RdfString(genre))

	def shotInLanguage(language: String): RdfTriple = buildTriple(RdfResource("dbpprop:language"), RdfString(language))

	def lasts(runtime: Integer): RdfTriple = buildTriple(RdfResource("dbpprop:runtime"), RdfInteger(runtime))

	def rated(rating: String): RdfTriple = buildTriple(RdfResource("freebase:film/film/rating"), RdfString(rating))

	def isReleased(released: String): RdfTriple = buildTriple(RdfResource("lod:isReleased"), RdfString(released))

	def releasedOn(releaseDate: DateTime): RdfTriple = buildTriple(RdfResource("dbpedia-owl:releaseDate"), RdfDate(releaseDate))

	def hasKeyword(keyword: String): RdfTriple = buildTriple(RdfResource("lod:keyword"), RdfString(keyword))

	def directedBy(director: String): RdfTriple = buildTriple(RdfResource("dbpprop:director"), RdfString(director))

	def writtenBy(writer: String): RdfTriple = buildTriple(RdfResource("dbpprop:writer"), RdfString(writer))

	def playedBy(actor: String): RdfTriple = buildTriple(RdfResource("dbpprop:starring"), RdfString(actor))

	def producedBy(producer: String): RdfTriple = buildTriple(RdfResource("dbpprop:producer"), RdfString(producer))

	def setDesignedBy(setDesigner: String): RdfTriple = buildTriple(RdfResource("dbpedia-owl:setDesigner"), RdfString(setDesigner))

	def editBy(editor: String): RdfTriple = buildTriple(RdfResource("dbpprop:editing"), RdfString(editor))

	def costumeDesignedBy(costumeDesigner: String): RdfTriple = buildTriple(RdfResource("dbpprop:costume"), RdfString(costumeDesigner))

	def MakeupBy(makeupArtist: String): RdfTriple = buildTriple(RdfResource("dbpprop:makeupArtist"), RdfString(makeupArtist))

	def shotBy(camera: String): RdfTriple = buildTriple(RdfResource("dbpprop:camera"), RdfString(camera))

	def distributedBy(distributor: String): RdfTriple = buildTriple(RdfResource("dbpprop:distributor"), RdfString(distributor))

	def filmedInStudio(studio: String): RdfTriple = buildTriple(RdfResource("dbpprop:studio"), RdfString(studio))

	def hasPoster(poster: String): RdfTriple = buildTriple(RdfResource("dbpprop:image"), RdfString(poster))

	def hasPhotos(photos: String): RdfTriple = buildTriple(RdfResource("dbpprop:hasPhotoCollection"), RdfUrl(photos))

	def hasTrailer(video: String): RdfTriple = buildTriple(RdfResource("freebase:film/film/trailers"), RdfUrl(video))

	def hasWebsite(website: String): RdfTriple = buildTriple(RdfResource("dbpprop:website"), RdfUrl(website))

	def filmedInLocation(location: String): RdfTriple = buildTriple(RdfResource("dbpprop:location"), RdfString(location))

	def hasBudget(budget: Integer): RdfTriple = buildTriple(RdfResource("dbpprop:budget"), RdfInteger(budget))

	def hasRevenue(revenue: Integer): RdfTriple = buildTriple(RdfResource("dbpprop:revenue"), RdfInteger(revenue))

	def hasSoundMix(soundMix: String): RdfTriple = buildTriple(RdfResource("lod:soundmix"), RdfString(soundMix))

	def hasSoundtrack(music: String): RdfTriple = buildTriple(RdfResource("freebase:film/film/soundtrack"), RdfString(music))

	def hasQuote(quote: String): RdfTriple = buildTriple(RdfResource("dbpprop:quote"), RdfUrl(quote))

	def hasAspectRatio(ratio: String): RdfTriple = buildTriple(RdfResource("lod:aspectRatio"), RdfString(ratio))

	def hasTrivia(trivia: String): RdfTriple = buildTriple(RdfResource("lod:trivia"), RdfString(trivia))

	def hasGoofs(goofs: String): RdfTriple = buildTriple(RdfResource("lod:goofs"), RdfString(goofs))

	def hasReview(review: String): RdfTriple = buildTriple(RdfResource("lod:review"), RdfString(review))

}
