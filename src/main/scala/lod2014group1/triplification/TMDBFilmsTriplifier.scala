package lod2014group1.triplification

import java.io.{FileReader, File}
import net.liftweb.json.JsonParser
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfResource
import lod2014group1.rdf.RdfMovieResource._

case class TmdbGenre(id: Long, name: String)
case class TmdbCollection(id: Long, name: String, poster_path: String, backdrop_path: String)
case class TmdbProductionCompanie(id: Long, name: String)
case class TmdbProductionCountry(iso_3166_1: String, name: String)
case class TmdbLanguage(iso_3166_1: String, ame: String)
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
case class TmdbTitle(iso_639_1: String, title: String)
case class TmdbAlternateTitles(titles: List[TmdbTitle])
case class TmdbCountry(iso_3166_1: String, certification: String, release_date: String)
case class TmdbReleases(ountries: List[TmdbCountry])
case class TmdbSimilarResult(adult: Boolean, backdrop_path: String, id: Long, original_title: String,
                             release_date: String, poster_path: String, popularity: String, title: String,
                             vote_average: Double, vote_count: Integer)
case class TmdbSimilar(page: Integer, results: List[TmdbSimilarResult], total_pages: Integer, total_results: Long)

case class TmdbJsonResponse1(adult: Boolean, backdrop_path: String, belongs_to_collection: List[TmdbCollection],
                            budget: Long, genres: List[TmdbGenre], homepage: String, id: Long, imdb_id: String,
                            original_title: String, overview: String, popularity: String,
                            poster_path: String, production_companies: List[TmdbProductionCompanie],
                            production_countries: List[TmdbProductionCountry], release_date: String
	                          )
case class TmdbJsonResponse2(revenue: Integer, runtime: Integer, spoken_language: List[TmdbLanguage],
                            status: String, tagline: String, title: String, vote_average: Double,
                            vote_count: Integer, credits: TmdbCredits, keywords: TmdbKeywords, videos: TmdbVideo,
                            alternate_titles: TmdbAlternateTitles, releases: TmdbReleases, similar: TmdbSimilar
	                          )



class TMDBFilmsTriplifier {

	def triplify(f: File): List[RdfTriple] = {
		implicit val formats = net.liftweb.json.DefaultFormats
		val json1: TmdbJsonResponse1 = JsonParser.parse(new FileReader(f)).extract[TmdbJsonResponse1]
		val json2: TmdbJsonResponse2 = JsonParser.parse(new FileReader(f)).extract[TmdbJsonResponse2]
		println(s"Movie id: ${json1.id} original_title: ${json1.original_title}")
		val imdb_id = json1.imdb_id
		if (!imdb_id.equals("")){
			val uri = RdfResource(s"https://www.themoviedb.org/movie/${json1.id}")
			val movie = RdfResource(s"lod:Movie${json1.imdb_id}")
			(movie sameAs uri.toString()) ::
			(movie hasTitle json1.original_title) ::
			addKeywords(movie, json2.keywords.keywords) :::
			addGenres(movie, json1.genres) :::
			addAlternativeTitles(movie, json2.alternate_titles.titles)
		} else {
			List()
		}
	}

	def addKeywords(movie: RdfResource, keywords: List[TmdbKeyword]): List[RdfTriple] = {
		keywords.map { keyword => movie hasKeyword keyword.name }
	}

	def addGenres(movie: RdfResource, genres: List[TmdbGenre]): List[RdfTriple] = {
		genres.map { genre => movie hasGenre genre.name }
	}

	def addAlternativeTitles(movie: RdfResource, titles: List[TmdbTitle]): List[RdfTriple] = {
		titles.map { title => movie hasAlternativeName title.title }
	}
}
