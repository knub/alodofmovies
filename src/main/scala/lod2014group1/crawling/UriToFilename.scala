package lod2014group1.crawling

import lod2014group1.messaging.worker.UriFile
import lod2014group1.Config

object UriToFilename {

	def parse(file: UriFile): String = {

		file.uri match {
			// Freebase
			case uri if uri.startsWith("http://www.freebase.com/") =>
				val id = uri.split("/").last
				// special flag for freebase, because for freebase we cannot determine whether its a movie
				// or a actor from the url
				file.flag match {
					case "movie" =>
						s"Freebase/film/$id"
					case _ =>
						""
				}

			// TMDB
			case uri if uri.startsWith("http://www.themoviedb.org/movie/") =>
				val id = uri.split("/").last
				s"TMDBMoviesList/movie/$id.json"

			case uri if uri.startsWith("http://www.themoviedb.org/person/") =>
				val id = uri.split("/").last
        s"TMDBMoviesList/person/$id.json"

			// IMDB
			case uri if uri.startsWith("http://www.imdb.com/title/") =>
				val uriSplit = uri.split("/")
				val id = uriSplit(4)
				var imdbFileName = uriSplit.last
				if (imdbFileName.equals(id))
					imdbFileName = "main.html"
				else
					imdbFileName = imdbFileName.split("\\?")(0) + ".html"
				s"IMDBMovie/$id/$imdbFileName"

			case uri if uri.startsWith("http://www.imdb.com/name/") =>
				val id = uri.split("/").last
				s"Actor/$id/main.html"

			// OFDB
			case uri if uri.startsWith("http://www.ofdb.de/film/") =>
				val id = uri.split("/").last
				s"OFDB/Movies/$id/Film.html"
			
			case uri if uri.startsWith("http://www.ofdb.de/view.php?page=film_detail") =>
				val id = uri.split("/").last
				s"OFDB/Movies/$id/Cast.html"

			case _ =>
				""
		}
	}
}
