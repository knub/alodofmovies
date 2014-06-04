package lod2014group1.triplification

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
case class TmdbImages(backdrops: List[TmdbBackdrop], posters: List[TmdbPoster])
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
