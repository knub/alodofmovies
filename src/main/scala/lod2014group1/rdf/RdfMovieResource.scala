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

	def hasReleaseInfo(releaseInfo: RdfResource): RdfTriple = buildTriple(RdfResource("lod:hasReleaseInfo"), releaseInfo)

	def alsoKnownAs(name: RdfResource): RdfTriple = buildTriple(RdfResource("lod:alsoKnownAs"), name)

	def hasAward(award: RdfResource): RdfTriple = buildTriple(RdfResource("lod:hasAward"), award)

	def hasTitle(title: String): RdfTriple = buildTriple(RdfResource("dbpprop:name"), RdfString(title))

	def isPartOf(collection: String): RdfTriple = {
		log.warn("Predicate not set yet.")
		this.buildTriple(RdfResource("somerdfname"), RdfString(collection))
	}

	def releasedInYear(year: Integer): RdfTriple = buildTriple(RdfResource("dbpprop:years"), RdfInteger(year))

	def hasGenre(genre: String): RdfTriple = buildTriple(RdfResource("dbpprop:genre"), RdfString(genre))

	def shotInLanguage(language: String): RdfTriple = buildTriple(RdfResource("dbpprop:language"), RdfString(language))

	def lasts(runtime: Integer): RdfTriple = buildTriple(RdfResource("dbpprop:runtime"), RdfInteger(runtime))

	def releasedInCountry(country: String): RdfTriple = buildTriple(RdfResource("dbpprop:country"), RdfString(country))

	def hasRating(rating: String): RdfTriple = {
		log.warn("Predicate not set yet.")
		this.buildTriple(RdfResource("somerdfname"), RdfString(rating))
	}

	def shotIn(blackAndWhite: RdfResource): RdfTriple = buildTriple(RdfResource("dcterms:subject"), blackAndWhite)

	def isReleased(released: String): RdfTriple = {
		log.warn("Predicate not set yet.")
		this.buildTriple(RdfResource("somerdfname"), RdfString(released))
	}

	def releasedOn(releaseDate: DateTime): RdfTriple = buildTriple(RdfResource("dbpprop:realeased"), RdfDate(releaseDate))

	def hasShortSummary(summary: String): RdfTriple = buildTriple(RdfResource("dbpedia-owl:abstact"), RdfString(summary))

	def hasStoryline(storyline: String): RdfTriple = buildTriple(RdfResource("dbpprop:description"), RdfString(storyline))

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

	def hasVideo(video: String): RdfTriple = {
		log.warn("Predicate not set yet.")
		this.buildTriple(RdfResource("somerdfname"), RdfUrl(video))
	}

	def hasWebsite(website: String): RdfTriple = buildTriple(RdfResource("dbpprop:website"), RdfUrl(website))

	def filmedInLocation(location: String): RdfTriple = buildTriple(RdfResource("dbpprop:location"), RdfString(location))

	def hasBudget(budget: Integer): RdfTriple = buildTriple(RdfResource("dbpprop:budget"), RdfInteger(budget))

	def hasRevenue(revenue: Integer): RdfTriple = buildTriple(RdfResource("dbpprop:revenue"), RdfInteger(revenue))

	def hasSoundMix(soundMix: String): RdfTriple = {
		log.warn("Predicate not set yet.")
		this.buildTriple(RdfResource("somerdfname"), RdfString(soundMix))
	}

	def hasSoundtrack(music: String): RdfTriple = buildTriple(RdfResource("dbpprop:music"), RdfString(music))

	def hasQuote(quote: String): RdfTriple = buildTriple(RdfResource("dbpprop:quote"), RdfUrl(quote))

	def hasAspectRatio(ratio: String): RdfTriple = {
		log.warn("Predicate not set yet.")
		this.buildTriple(RdfResource("somerdfname"), RdfString(ratio))
	}

	def hasTrivia(trivia: String): RdfTriple = {
		log.warn("Predicate not set yet.")
		this.buildTriple(RdfResource("somerdfname"), RdfString(trivia))
	}

	def hasGoofs(goofs: String): RdfTriple = {
		log.warn("Predicate not set yet.")
		this.buildTriple(RdfResource("somerdfname"), RdfString(goofs))
	}

	def hasReview(review: String): RdfTriple = {
		log.warn("Predicate not set yet.")
		this.buildTriple(RdfResource("somerdfname"), RdfString(review))
	}

}
