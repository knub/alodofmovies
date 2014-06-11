package lod2014group1.triplification

import java.io.{FileReader, File}
import net.liftweb.json.JsonParser
import lod2014group1.rdf._
import lod2014group1.rdf.RdfMovieResource._
import lod2014group1.crawling.TMDBMoviesListCrawler
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfResource

object TMDBFilmsTriplifier {
	val TmdbBaseUrl = "http://image.tmdb.org/t/p/original%s"
	val YouTubeBaseUrl = "www.youtube.com/watch?v=%s"
}

class TMDBFilmsTriplifier {
	val crawler = new TMDBMoviesListCrawler

	def triplify(f: File): List[RdfTriple] = {
		implicit val formats = net.liftweb.json.DefaultFormats
		val mainJson: TmdbMainJson = JsonParser.parse(new FileReader(f)).extract[TmdbMainJson]
		val appendJson: TmdbAppendJson = JsonParser.parse(new FileReader(f)).extract[TmdbAppendJson]
		println(s"Movie id: ${mainJson.id} original_title: ${mainJson.original_title}")

		appendJson.credits.cast.foreach(person => crawler.getFile(TMDBMoviesListCrawler.PERSON_URL.format(person.id)))
		appendJson.credits.crew.foreach(person => crawler.getFile(TMDBMoviesListCrawler.PERSON_URL.format(person.id)))

		val id = mainJson.id
		val collection = if (mainJson.belongs_to_collection != null) {
			mainJson.belongs_to_collection.name
		} else {
			""
		}
		val movie = new RdfResource(s"https://www.themoviedb.org/movie/$id")

		addString(movie.label(_: String), mainJson.title) :::
		addString(movie.sameAs(_: String), s"lod:Movie${mainJson.imdb_id}") :::
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
		addList(movie.shotInLanguage(_: String), mainJson.spoken_language.flatMap { language => List(language.iso_3166_1, language.name)})
		addString(movie.hasReleaseStatus(_: String), mainJson.status) :::
		addString(movie.hasTagline(_: String), mainJson.tagline) :::
		addString(movie.hasTitle(_: String), mainJson.title) :::
		addDouble(movie.tmdbVoteAverage(_: Double), mainJson.vote_average) :::
		addInteger(movie.tmdbVoteCount(_: Integer), mainJson.vote_count) :::
		//addCast
		addList(movie.hasKeyword(_: String), appendJson.keywords.keywords.map { keyword => keyword.name }) :::
		addList(movie.hasImage(_: String), appendJson.images.backdrops.map { image => TMDBFilmsTriplifier.TmdbBaseUrl.format(image.file_path) }) :::
		addList(movie.hasPoster(_: String), appendJson.images.posters.map { image => TMDBFilmsTriplifier.TmdbBaseUrl.format(image.file_path) }) :::
		addList(movie.hasVideo(_: String), appendJson.videos.results.map { video => TMDBFilmsTriplifier.YouTubeBaseUrl.format(video.key) } ) :::
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
		if ( obj != 0 ){
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
			createAlternativeTitle(movie, new RdfAkaResource(s"https://www.themoviedb.org/movie/$id/Aka$i"), obj)
		}
	}

	def createAlternativeTitle(movie: RdfMovieResource, aka: RdfAkaResource, obj: TmdbTitle): List[RdfTriple] = {
		List(aka inCountry obj.iso_3166_1, aka hasAkaName obj.title, movie alsoKnownAs aka,
			aka hasLabel obj.title)
	}

	def addReleaseInfo(movie: RdfMovieResource, id: Long, objs: List[TmdbCountry]): List[RdfTriple] = {
		objs.zipWithIndex.flatMap { case(obj, i) =>
			val releaseInfo = new RdfReleaseInfoResource(s"https://www.themoviedb.org/movie/$id/ReleaseInfo$i")
			List(releaseInfo inCountry obj.iso_3166_1, releaseInfo ageRating obj.certification,
					releaseInfo releasedOn obj.release_date, movie hasReleaseInfo releaseInfo,
					releaseInfo hasLabel obj.release_date)
		}.toList
	}

	def addCrew(movie: RdfMovieResource, id: Long, crew: List[TmdbCrew]): List[RdfTriple] = {
		crew.map { p =>
			val person = new RdfPersonResource(s"http://www.themoviedb.org/person/${p.id}")
		}
		Nil
	}

}
