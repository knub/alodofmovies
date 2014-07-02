package lod2014group1.triplification

import java.io.{FileReader, File}
import net.liftweb.json.JsonParser
import lod2014group1.rdf._
import lod2014group1.rdf.RdfMovieResource._
import lod2014group1.crawling.TMDBMoviesListCrawler
import lod2014group1.rdf.RdfTriple
import org.apache.commons.io.FileUtils

object TmdbMovieTriplifier {
	val TmdbBaseUrl = "http://image.tmdb.org/t/p/original%s"
	val YouTubeBaseUrl = "www.youtube.com/watch?v=%s"
}

class TmdbMovieTriplifier extends Triplifier {
	val crawler = new TMDBMoviesListCrawler

	def triplify(file: File): List[RdfTriple] = {
		triplify(FileUtils.readFileToString(file))

	}
	def triplify(content: String): List[RdfTriple] = {
		implicit val formats = net.liftweb.json.DefaultFormats

		val mainJson: TmdbMainJson = JsonParser.parse(content).extract[TmdbMainJson]
		val appendJson: TmdbAppendJson = JsonParser.parse(content).extract[TmdbAppendJson]

//		println(s"Movie id: ${mainJson.id} original_title: ${mainJson.original_title}")

//		appendJson.credits.cast.foreach { person => crawler.getFile(TMDBMoviesListCrawler.PERSON_URL.format(person.id)) }
//		appendJson.credits.crew.foreach { person => crawler.getFile(TMDBMoviesListCrawler.PERSON_URL.format(person.id)) }

		val id = mainJson.id
		val collection = if (mainJson.belongs_to_collection != null) {
			mainJson.belongs_to_collection.name
		} else {
			""
		}
		val movie = new RdfMovieResource(UriBuilder.getMovieUriFromTmdbId(id))
		List(movie sameAs UriBuilder.getTmdbMovieUri(id),
			movie isA RdfMovieResource.film) :::
		addString(movie.label(_: String), mainJson.title) :::
		addString(movie.sameAsImdbUrl(_: String), mainJson.imdb_id) :::
		addBoolean(movie.isAdult(_: Boolean), mainJson.adult) :::
		addString(movie.isPartOf(_: String), collection) :::
		addString(movie.hasBudget(_: String), mainJson.budget.toString) :::
		addList(movie.hasGenre(_: String), mainJson.genres.map { genre => genre.name }) :::
		addString(movie.hasWebsite(_: String), mainJson.homepage) :::
		addString(movie.hasOriginalTitle(_ : String), mainJson.original_title) :::
		addString(movie.hasShortSummary(_ : String), mainJson.overview) :::
		addList(movie.distributedBy(_ : String), mainJson.production_companies.map { comp => comp.name }) :::
		addList(movie.country(_ : String), mainJson.production_countries.flatMap { country => List(country.iso_3166_1, country.name) } ) :::
		addInteger(movie.hasRevenue(_ : Integer), mainJson.revenue) :::
		addInteger(movie.lasts(_: Integer), mainJson.runtime) :::
		addList(movie.shotInLanguage(_: String), mainJson.spoken_language.flatMap { language => List(language.iso_3166_1, language.name)}) :::
		addString(movie.hasReleaseStatus(_: String), mainJson.status) :::
		addString(movie.hasTagline(_: String), mainJson.tagline) :::
		addString(movie.hasTitle(_: String), mainJson.title) :::
		addDouble(movie.tmdbVoteAverage(_: Double), mainJson.vote_average) :::
		addInteger(movie.tmdbVoteCount(_: Integer), mainJson.vote_count) :::
		addCast(movie, id, appendJson.credits.cast) :::
		addCrew(movie, appendJson.credits.crew) :::
		addList(movie.hasKeyword(_: String), appendJson.keywords.keywords.map { keyword => keyword.name }) :::
		addList(movie.hasImage(_: String), appendJson.images.backdrops.map { image => TmdbMovieTriplifier.TmdbBaseUrl.format(image.file_path) }) :::
		addList(movie.hasPoster(_: String), appendJson.images.posters.map { image => TmdbMovieTriplifier.TmdbBaseUrl.format(image.file_path) }) :::
		addList(movie.hasVideo(_: String), appendJson.videos.results.map { video => TmdbMovieTriplifier.YouTubeBaseUrl.format(video.key) } ) :::
		addAlternativeTitles(movie, id, appendJson.alternative_titles.titles) :::
		addReleaseInfo(movie, id, appendJson.releases.countries)
	}


	def addBoolean(predicate: Boolean => RdfTriple, obj: Boolean): List[RdfTriple] = {
		List(predicate(obj))
	}

	def addString(predicate: String => RdfTriple, obj: String): List[RdfTriple] = {
		if (obj != null && obj != "" ){
			List(predicate(obj))
		} else {
			Nil
		}
	}

	def addInteger(predicate: Integer => RdfTriple, obj: Integer): List[RdfTriple] = {
		if ( obj != null && obj != 0 ){
			List(predicate(obj))
		} else {
			Nil
		}
	}

	def addDouble(predicate: Double => RdfTriple, obj: Double): List[RdfTriple] = {
		if ( obj != 0 ){
			List(predicate(obj))
		} else {
			Nil
		}
	}

	def addList(predicate: String => RdfTriple, objs: List[String]): List[RdfTriple] = {
		objs.map { obj => predicate(obj) }
	}

	def addAlternativeTitles(movie: RdfMovieResource, id: Long, objs: List[TmdbTitle]): List[RdfTriple] = {
		objs.zipWithIndex.flatMap { case(obj, i) =>
			createAlternativeTitle(movie, new RdfAkaResource(UriBuilder.getAkaUriFromTmdbId(id, i)), obj)
		}
	}

	def createAlternativeTitle(movie: RdfMovieResource, aka: RdfAkaResource, obj: TmdbTitle): List[RdfTriple] = {
		List(aka inCountry obj.iso_3166_1, aka hasAkaName obj.title, movie alsoKnownAs aka,
			aka hasLabel obj.title, aka isAn RdfAkaResource.alternativeMovieName)
	}

	def addReleaseInfo(movie: RdfMovieResource, id: Long, objs: List[TmdbCountry]): List[RdfTriple] = {
		objs.zipWithIndex.flatMap { case(obj, i) =>
			val releaseInfo = new RdfReleaseInfoResource(UriBuilder.getReleaseInfoUriFromTmdbId(id, i))
			List(releaseInfo inCountry obj.iso_3166_1, releaseInfo ageRating obj.certification,
					releaseInfo releasedOn obj.release_date, movie hasReleaseInfo releaseInfo,
					releaseInfo hasLabel obj.release_date, releaseInfo isA RdfReleaseInfoResource.releaseInfo)
		}.toList
	}

	def addCast(movie: RdfMovieResource, id: Long, cast: List[TmdbCast]): List[RdfTriple] = {
		cast.flatMap { c => handleCast(movie, id, c)}
	}

	def handleCast(movie: RdfMovieResource, id: Long, member: TmdbCast): List[RdfTriple] = {
		val actor = new RdfPersonResource(UriBuilder.getPersonUriFromTmdbId(member.id))
		val charName = member.character
		val character = new RdfCharacterResource(UriBuilder.getCharacterUriFromTmdbId(charName.replace(" ", "_")))
		val characterMovie = new RdfCharacterResource(UriBuilder.getMovieCharacterUriFromTmdbId(id, charName.replace(" ", "_")))
		List(actor sameAs UriBuilder.getTmdbPersonUri(member.id),
			character name charName,
			characterMovie name charName,
			actor hasLabel member.name,
			actor hasName member.name,
		  character hasLabel charName,
		  characterMovie hasLabel charName,
		  movie starring actor,
			characterMovie inMovie movie,
		  actor playsCharacter characterMovie,
		  characterMovie isSubclassOf character,
			characterMovie isA RdfCharacterResource.character,
			character isA RdfCharacterResource.character,
			actor isAn RdfPersonResource.actor,
			actor isA RdfPersonResource.person)
	}

	def addCrew(movie: RdfMovieResource, crew: List[TmdbCrew]): List[RdfTriple] = {
		crew.flatMap { p =>
			handleCrew(movie, p)
		}
	}

	def handleCrew(movie: RdfMovieResource, member: TmdbCrew): List[RdfTriple] = {
		val person = new RdfPersonResource(UriBuilder.getPersonUriFromTmdbId(member.id))
		val rel = (member.department, member.job) match {
			case ("Directing", "Director") => movie directedBy person
			case ("Directing", "Special Guest Director") => movie directedBy person
			case ("Production", "Co-Producer") => movie coProducedBy person
			case ("Production", "Co-Executive Producer") => movie coProducedBy person
			case ("Production", "Producer") => movie producedBy person
			case ("Production", "Executive Producer") => movie producedBy person
			case ("Production", "Casting") => movie castingBy person
			case ("Production", "Production Manager") => movie productionManagedBy person
			case ("Writing", "Author") => movie storyBy person
			case ("Writing", "Novel") => movie novelBy person
			case ("Writing", "Screenplay") => movie screenplayBy person
			case ("Writing", _) => movie writtenBy person
			case ("Sound", "Original Music Composer") => movie musicBy person
			case ("Camera", "Director of Photography") => movie musicBy person
			case ("Editing", "Editor") => movie editBy person
			case ("Art", "Production Design") => movie productionDesignBy person
			case ("Art", "Art Director") => movie artDirector person
			case ("Art", "Set Decorator") => movie setDecoratedBy person
			case ("Costume & Make-up", "Costume Design") => movie costumeDesignedBy person
			case ("Costume & Make-up", "Makeup Artist") => movie makeupBy person
			case ("Visual Effects", _) => movie visualEffectsBy person
			case ("Crew", "Special Effects") => movie specialEffectsBy person
			case ("Crew", "Stunts") => movie stuntsBy person
			case ("Actors", "Actor") => movie starring person
			case default => movie hasOtherCrew person
		}
		List(rel, person hasJob member.job,
			person hasName member.name,
			person label member.name,
			person isA RdfPersonResource.person,
			person sameAs UriBuilder.getTmdbPersonUri(member.id)
		)
	}

}
