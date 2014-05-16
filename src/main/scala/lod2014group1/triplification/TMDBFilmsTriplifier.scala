package lod2014group1.triplification

import java.io.{FileReader, File}
import lod2014group1.rdf.RdfTriple
import net.liftweb.json.JsonParser

case class TmdbGenre(id: Long, name: String)
case class TmdbProductionCompanie(id: Long, name: String)
case class TmdbProductionCountry(iso_3166_1: String, name: String)
case class TmdbJsonResponse(adult: Boolean, budget: Long, genres: List[TmdbGenre], id: Long,
                            imdb_id: String, original_title: String, overview: String,
                            popularity: String, poster_path: String,
                            production_companies: List[TmdbProductionCompanie],
                            production_countries: List[TmdbProductionCountry], release_date: String,
                            revenue: Integer, runtime: Integer
	                          )


class TMDBFilmsTriplifier {

	def triplify(f: File): List[RdfTriple] = {
		implicit val formats = net.liftweb.json.DefaultFormats
		val json: TmdbJsonResponse = JsonParser.parse(new FileReader(f)).extract[TmdbJsonResponse]
		println(s"Movie id: ${json.id} original_title: ${json.original_title}")
		var triples: List[RdfTriple] = List()
		triples
	}
}
