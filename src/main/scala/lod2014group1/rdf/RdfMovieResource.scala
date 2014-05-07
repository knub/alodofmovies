package lod2014group1.rdf

import org.slf4s.Logging
import org.joda.time.DateTime

object RdfMovieResource {
	implicit def fromRdfResource(resource: RdfResource): RdfMovieResource = {
		new RdfMovieResource(resource.uri)
	}

	def film: RdfResource = {
		RdfResource("dbpedia-owl:Film")
	}

	def actor: RdfResource = {
		RdfResource("dbpedia-owl:Actor")
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
	val titleResource = RdfResource("dbpprop:name")
	val alternativeTitleResource = RdfResource("dbpprop:alternativeNames")
	//val partOfResource = RdfResource("")
	val yearResource = RdfResource("dbpprop:years")
	val genreResource = RdfResource("dbpprop:genre")
	val runtimeResource = RdfResource("dbpprop:runtime")
	val languageResource = RdfResource("dbpprop:language")
	val countryResource = RdfResource("dbpprop:country")
	//val ratingResource = RdfResource("")
	val subjectResource = RdfResource("dcterms:subject")
	//val releasedStatusResource = RdfResource("")
	val releaseDateResource = RdfResource("dbpprop:realeased")
	val shortSummaryResource = RdfResource("dbpedia-owl:abstact")
	val storylineResource = RdfResource("dbpprop:description")
	//val keywordsResource = RdfResource("")
	val directorResource = RdfResource("dbpprop:director")
	val writerResource = RdfResource("dbpprop:writer")
	val actorResource = RdfResource("dbpprop:starring")
	val productorResource = RdfResource("dbpprop:producer")
	val setDesignerResource = RdfResource("dbpedia-owl:setDesigner")
	val editorResource = RdfResource("dbpprop:editing")
	val costumeDesignerResource = RdfResource("dbpprop:costume")
	val makeupArtistResource = RdfResource("dbpprop:makeupArtist")
	val cameraResource = RdfResource("dbpprop:camera")
	val distributorResource = RdfResource("dbpprop:distributor")
	val studioResource = RdfResource("dbpprop:studio")
	val posterResource = RdfResource("dbpprop:image")
	val photosResource = RdfResource("dbpprop:hasPhotoCollection")
	//val videosResource = RdfResource("")
	val websitesResource = RdfResource("dbpprop:website")
	val filmingLocationResource = RdfResource("dbpprop:location")
	val budgetResource = RdfResource("dbpprop:budget")
	val revenueResource = RdfResource("dbpprop:revenue")
	//val soundMixResource = RdfResource("")
	val soundtracktResource = RdfResource("dbpprop:music")
	val quoteResource = RdfResource("dbpprop:quote")
	//val aspectRatioResource = RdfResource("")
	//val triviaResource = RdfResource("")
	//val goofsResource = RdfResource("")
	//val reviewsResource = RdfResource("")

	def hasTitle(title: String): RdfTriple = {
		this.buildTriple(titleResource, RdfString(title))
	}

	def alsoKnownAs(title: String): RdfTriple = {
		this.buildTriple(alternativeTitleResource, RdfString(title))
	}

	//	def isPartOf(collection: String): RdfTriple = {
	//		this.buildTriple(partOfResource, RdfString(collection))
	//	}

	def releasedInYear(year: Integer): RdfTriple = {
		this.buildTriple(yearResource, RdfInteger(year))
	}

	def hasGenre(genre: String): RdfTriple = {
		this.buildTriple(genreResource, RdfString(genre))
	}

	def shootInLanguage(language: String): RdfTriple = {
		this.buildTriple(languageResource, RdfString(language))
	}

	def takes(runtime: Integer): RdfTriple = {
		this.buildTriple(runtimeResource, RdfInteger(runtime))
	}

	def releasedInCountry(country: String): RdfTriple = {
		this.buildTriple(countryResource, RdfString(country))
	}

	//	def hasRating(rating: Integer): RdfTriple = {
	//		this.buildTriple(ratingResource, rating(country))
	//	}

	def shotIn(blackAndWhite: RdfResource): RdfTriple = {
		this.buildTriple(subjectResource, blackAndWhite)
	}

	//	def isReleased(released: Boolean): RdfTriple = {
	//		this.buildTriple(releasedStatusResource, RdfString(released))
	//	}

	def releasedOn(releaseDate: DateTime): RdfTriple = {
		this.buildTriple(releaseDateResource, RdfDate(releaseDate))
	}

	def hasShortSummary(summary: String): RdfTriple = {
		this.buildTriple(shortSummaryResource, RdfString(summary))
	}

	def hasStoryline(storyline: String): RdfTriple = {
		this.buildTriple(storylineResource, RdfString(storyline))
	}

	//	def hasKeyword(keyword: String): RdfTriple = {
	//		this.buildTriple(keywordsResource, RdfString(keyword))
	//	}

	def directedBy(director: String): RdfTriple = {
		this.buildTriple(directorResource, RdfString(director))
	}

	def writtenBy(writer: String): RdfTriple = {
		this.buildTriple(writerResource, RdfString(writer))
	}

	def playedBy(actor: String): RdfTriple = {
		this.buildTriple(actorResource, RdfString(actor))
	}

	def producedBy(producer: String): RdfTriple = {
		this.buildTriple(productorResource, RdfString(producer))
	}

	def setDesignedBy(setDesigner: String): RdfTriple = {
		this.buildTriple(setDesignerResource, RdfString(setDesigner))
	}

	def editBy(editor: String): RdfTriple = {
		this.buildTriple(editorResource, RdfString(editor))
	}

	def costumeDesignedBy(costumeDesigner: String): RdfTriple = {
		this.buildTriple(costumeDesignerResource, RdfString(costumeDesigner))
	}

	def MakeupBy(makeupArtist: String): RdfTriple = {
		this.buildTriple(makeupArtistResource, RdfString(makeupArtist))
	}

	def shootBy(camera: String): RdfTriple = {
		this.buildTriple(cameraResource, RdfString(camera))
	}

	def distributedBy(distributor: String): RdfTriple = {
		this.buildTriple(distributorResource, RdfString(distributor))
	}

	def filmedInStudio(studio: String): RdfTriple = {
		this.buildTriple(studioResource, RdfString(studio))
	}

	def hasPoster(poster: String): RdfTriple = {
		this.buildTriple(posterResource, RdfString(poster))
	}

	def hasPhotos(photos: String): RdfTriple = {
		this.buildTriple(photosResource, RdfUrl(photos))
	}

	//	def hasVideo(video: String): RdfTriple = {
	//		this.buildTriple(videosResource, RdfUrl(video))
	//	}

	def hasWebsite(website: String): RdfTriple = {
		this.buildTriple(websitesResource, RdfUrl(website))
	}

	def filmedInLocation(location: String): RdfTriple = {
		this.buildTriple(filmingLocationResource, RdfString(location))
	}

	def hasBudget(budget: Integer): RdfTriple = {
		this.buildTriple(budgetResource, RdfInteger(budget))
	}

	def hasRevenue(revenue: Integer): RdfTriple = {
		this.buildTriple(revenueResource, RdfInteger(revenue))
	}

	//	def hasSoundMix(soundMix: String): RdfTriple = {
	//		this.buildTriple(soundMixResource, RdfString(soundMix))
	//	}

	def hasSoundtrack(music: String): RdfTriple = {
		this.buildTriple(soundtracktResource, RdfString(music))
	}

	def hasQuote(quote: String): RdfTriple = {
		this.buildTriple(quoteResource, RdfUrl(quote))
	}

	//	def hasAspectRatio(ratio: String): RdfTriple = {
	//		this.buildTriple(aspectRatioResource, RdfString(ratio))
	//	}

	//	def hasTrivia(trivia: String): RdfTriple = {
	//		this.buildTriple(triviaResource, RdfString(trivia))
	//	}

	//	def hasGoofs(goofs: String): RdfTriple = {
	//		this.buildTriple(goofsResource, RdfString(goofs))
	//	}

	//	def hasReview(review: String): RdfTriple = {
	//		this.buildTriple(reviewResource, RdfString(review))
	//	}

}
