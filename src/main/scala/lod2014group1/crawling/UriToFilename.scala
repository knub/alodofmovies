package lod2014group1.crawling

import lod2014group1.messaging.worker.UriFile
import lod2014group1.Config

object UriToFilename {

	def parse(file: UriFile): String = {
		val dir = Config.DATA_FOLDER

		file.uri match {
			// Freebase
			case uri if uri.startsWith("http://www.freebase.com/") =>
				val id = uri.split("/").last
				// special flag for freebase, because for freebase we cannot determine whether its a movie
				// or a actor from the url
				file.flag match {
					case "actor" =>
						s"$dir/freebase/person/$id/main.json"
					case "movie" =>
						s"$dir/freebase/movie/$id/main.json"
					case _ =>
						""
				}

			// TMDB
			case uri if uri.startsWith("http://www.themoviedb.org/movie/") =>
				val id = uri.split("/").last
				s"$dir/themoviedb/movie/$id/main.json"

			case uri if uri.startsWith("http://www.themoviedb.org/person/") =>
				val id = uri.split("/").last
				s"$dir/themoviedb/person/$id/main.json"

			// IMDB
			case uri if uri.startsWith("http://www.imdb.com/title/") =>
				val uriSplit = uri.split("/")
				val id = uriSplit(2).substring(2)
				var imdbFileName = uriSplit.last
				if (imdbFileName.isEmpty)
					imdbFileName = "main.html"
				else
					imdbFileName = imdbFileName.split("?")(0) + ".html"
				s"$dir/IMDBMovie/$id/$imdbFileName"

			case uri if uri.startsWith("http://www.imdb.com/name/") =>
				val id = uri.split("/").last
				s"$dir/Actor/$id/main.html"

			// OFDB
			case uri if uri.startsWith("http://www.ofdb.de/film/") =>
				val id = uri.split("/").last
				s"$dir/OFDB/Movies/$id/film.html"
			
			case uri if uri.startsWith("http://www.ofdb.de/view.php?page=film_detail") =>
				val id = uri.split("/").last
				s"$dir/OFDB/Movies/$id/cast.html"

			case _ =>
				""
		}
	}
}
