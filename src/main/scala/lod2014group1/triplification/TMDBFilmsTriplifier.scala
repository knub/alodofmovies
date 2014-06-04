package lod2014group1.triplification

import java.io.{FileReader, File}
import net.liftweb.json.JsonParser
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfResource
import lod2014group1.rdf.RdfMovieResource._
import lod2014group1.crawling.TMDBMoviesListCrawler

case class TmdbGenre(id: Long, name: String)
case class TmdbCollection(id: Long, name: String, poster_path: String, backdrop_path: String)
case class TmdbProductionCompanie(id: Long, name: String)
case class TmdbProductionCountry(iso_3166_1: String, name: String)
case class TmdbLanguage(iso_3166_1: String, name: String)
case class TmdbCast(cast_id: Long, character: String, credit_id: String, id: Long, name: String,
                    order: Integer, profile_path: String)
case class TmdbCrew(credit_id: String, department: String, id: Long, job: String, name: String, profile_path: String)
case class TmdbCredits(cast: List[TmdbCast], crew: List[TmdbCrew])
case class TmdbKeyword(id: Long, name: String)
case class TmdbKeywords(keywords: List[TmdbKeyword])
case class TmdbBackdrop(aspect_ratio: Double, file_path: String, height: Integer, iso_639_1: String,
                        vote_average: String, vote_count: Integer, width: Integer)
case class TmdbPoster(aspect_ratio: Double, file_path: String, height: Integer, id: String, iso_639_1: String,
                      vote_average: String, vote_count: Integer, width: Integer)
case class TmdbImages(backdrops: List[TmdbBackdrop], posters: List[TmdbImages])
case class TmdbVideoResult(id: String, iso_639_1: String, key: String, name: String, site: String,
                      size: Integer)
case class TmdbVideo(results: List[TmdbVideoResult])
case class TmdbTitle(iso_3166_1: String, title: String)
case class TmdbAlternateTitles(titles: List[TmdbTitle])
case class TmdbCountry(iso_3166_1: String, certification: String, release_date: String)
case class TmdbReleases(countries: List[TmdbCountry])
case class TmdbSimilarResult(adult: Boolean, backdrop_path: String, id: Long, original_title: String,
                             release_date: String, poster_path: String, popularity: String, title: String,
                             vote_average: Double, vote_count: Integer)
case class TmdbSimilar(page: Integer, results: List[TmdbSimilarResult], total_pages: Integer, total_results: Long)

case class TmdbMainJson(adult: Boolean, belongs_to_collection: TmdbCollection,
                            budget: Integer, genres: List[TmdbGenre], homepage: String, id: Long, imdb_id: String,
                            original_title: String, overview: String, popularity: String,
                            production_companies: List[TmdbProductionCompanie],
                            production_countries: List[TmdbProductionCountry], revenue: Integer, runtime: Integer,
                            spoken_language: List[TmdbLanguage], status: String, tagline: String, title: String,
                            vote_average: Double, vote_count: Integer
	                          )
case class TmdbAppendJson(credits: TmdbCredits, keywords: TmdbKeywords, images: TmdbImages,
                            videos: TmdbVideo, alternative_titles: TmdbAlternateTitles, releases: TmdbReleases,
                            similar: TmdbSimilar
	                          )



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
		val movie = RdfResource(s"https://www.themoviedb.org/movie/$id")

		addString(movie.sameAs(_: String), s"lod:Movie${mainJson.imdb_id}") :::
		addBoolean(movie.isAdult(_: Boolean), mainJson.adult) :::
		addString(movie.isPartOf(_: String), mainJson.belongs_to_collection.name) :::
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


//		addString(movie.hasTitle(_: String), appendJson.title) :::
//		 :::
//		addString(movie.isPartOf(_: String), mainJson.belongs_to_collection.name)
//		addString()
//		addList(movie.hasAlternativeName(_: String), appendJson.alternative_titles.titles.map( title => title.title))
//
//
//		List(movie label appendJson.title) :::
//		addImdb(movie, mainJson.imdb_id) :::
//		addTitle(movie, appendJson.title) :::
//		addCollection(movie, mainJson.belongs_to_collection) :::
//		addRuntime(movie, appendJson.runtime) :::
//		addTagline(movie, appendJson.tagline) :::
//		addBudget(movie, mainJson.budget) :::
//		addOverview(movie, mainJson.overview) :::
//		addKeywords(movie, appendJson.keywords.keywords) :::
//		addGenres(movie, mainJson.genres) :::
//		addAlternativeTitles(movie, appendJson.alternative_titles.titles) :::
//		addSpokenLanguage(movie, appendJson.spoken_language) :::
//		addProductionCompany(movie, mainJson.production_companies) :::
//		addProductionCountry(movie, mainJson.production_countries) :::
//		addString(movie.hasTitle(_: String), appendJson.title)
	}

//	def addAdult(movie: RdfResource, adult: Boolean): List[RdfTriple] = {
//		List(movie isAdult adult)
//	}


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
		if (obj != null && obj != 0 ){
			List(predicate(obj))
		} else {
			Nil
		}
	}

	def addList(predicate: String => RdfTriple, objs: List[String]): List[RdfTriple] = {
		objs.map { obj => predicate(obj) }
	}



	def addImdb(movie: RdfResource, imdb_id: String): List[RdfTriple] = {
		if (imdb_id != "") {
			List(movie sameAs s"lod:Movie${imdb_id}")
		} else {
			Nil
		}
	}

	def addTitle(movie: RdfResource, title: String): List[RdfTriple] = {
		if (title != ""){
			List(movie hasTitle title)
		} else {
			Nil
		}
	}

	def addTagline(movie: RdfResource, tagline: String): List[RdfTriple] = {
		if (tagline != ""){
			List(movie hasTagline tagline)
		} else {
			Nil
		}
	}

	def addCollection(movie: RdfResource, collection: TmdbCollection): List[RdfTriple] = {
		if (collection != null){
			List(movie isPartOf collection.name)
		} else {
			Nil
		}
	}

	def addOverview(movie: RdfResource, overview: String): List[RdfTriple] = {
		if (overview != ""){
			List(movie hasShortSummary overview)
		} else {
			Nil
		}
	}

//	def addOriginalTitle(movie: RdfResource, title: String): List[RdfTriple] = {
//		if (title != ""){
//			List(movie hasOriginalTitle title)
//		} else {
//			Nil
//		}
//	}

//	def addOverview(movie: RdfResource, overview: String): List[RdfTriple] = {
//		if (overview != ""){
//			List(movie overview overview)
//		} else {
//			Nil
//		}
//	}

	def addRuntime(movie: RdfResource, runtime: Integer): List[RdfTriple] = {
		if (runtime != null && runtime != 0){
			List(movie lasts runtime)
		} else {
			Nil
		}
	}

	def addBudget(movie: RdfResource, budget: Integer): List[RdfTriple] = {
		if (budget != null && budget != 0){
			List(movie hasBudget budget.toString)
		} else {
			Nil
		}
	}

//	def addRevenue(movie: RdfResource, revenue: Integer): List[RdfTriple] = {
//		if (revenue != null && revenue != 0){
//			List(movie hasRevenue title)
//		} else {
//			Nil
//		}
//	}

	def addKeywords(movie: RdfResource, keywords: List[TmdbKeyword]): List[RdfTriple] = {
		keywords.map { keyword => movie hasKeyword keyword.name }
	}

	def addGenres(movie: RdfResource, genres: List[TmdbGenre]): List[RdfTriple] = {
		genres.map { genre => movie hasGenre genre.name }
	}

	def addAlternativeTitles(movie: RdfResource, titles: List[TmdbTitle]): List[RdfTriple] = {
		titles.flatMap { title => List(movie hasAlternativeName title.title, movie hasAlternativeName title.iso_3166_1) }
	}

	def addSpokenLanguage(movie: RdfResource, languages: List[TmdbLanguage]): List[RdfTriple] = {
		languages.map {lang: TmdbLanguage => movie shotInLanguage lang.name }
	}

	def addProductionCompany(movie: RdfResource, companies: List[TmdbProductionCompanie]): List[RdfTriple] = {
		companies.map {comp: TmdbProductionCompanie => movie producedBy comp.name }
	}

	def addProductionCountry(movie: RdfResource, countries: List[TmdbProductionCountry]): List[RdfTriple] = {
		countries.map {country: TmdbProductionCountry => movie filmedInLocation country.name }
	}

//	def addRelease(movie: RdfResource, releases: List[TmdbReleases]): List[RdfTriple] = {
//		releases.map { release =>
//			val releaseInfo = RdfResource(s"lod:Movie${}/ReleaseInfo$releaseInfoCount")
//		}
//		countries.map {country: TmdbProductionCountry => movie filmedInLocation country.name }
//	}


}
