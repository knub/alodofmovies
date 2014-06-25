package lod2014group1.rdf

object UriBuilder {
	def getMovieUriFromImdbId(id : String): String = {
		s"lod:Movie$id"
	}
	
	def getMovieUriFromFreebaseId(id : String): String = {
		s"lod:FreebaseMovie$id"
	}

	def getMovieUriFromTmdbId(id: Long): String = {
		s"lod:TmdbMovie$id"
	}
	
	def getPersonUriFromImdbId(id : String): String = {
		s"lod:MoviePerson$id"
	}

	def getPersonUriFromFreebaseId(id: String): String = {
		s"lod:FreebaseMoviePerson$id"
	}
	
	def getPersonUriFromTmdbId(id: Long): String = {
		s"lod:TmdbMoviePerson$id"
	}
	
	def getCharacterUriFromFreebaseId(id: String): String = {
		s"lod:FreebaseCharacter$id"
	}
	
	def getCharacterUriFromTmdbId(id: String): String = {
		s"lod:TmdbCharacter$id"
	}
	
	def getMovieCharacterUriFromFreebaseId(movieId: String, characterId: String): String = {
		s"${getMovieUriFromFreebaseId(movieId)}/Character$characterId"
	}

	def getMovieCharacterUriFromTmdbId(movieId: Long, characterId: String): String = {
		s"${getMovieUriFromTmdbId(movieId)}/Character$characterId"
	}

	def getReleaseInfoUriFromFreebaseId(id: String): String = {
		s"lod:FreebaseReleaseInfo$id"
	}

	def getReleaseInfoUriFromTmdbId(movieId: Long, infoId: Long): String = {
		s"${getMovieUriFromTmdbId(movieId)}/ReleaseInfo$infoId"
	}

	def getAkaUriFromTmdbId(movieId: Long, infoId: Long): String = {
		s"${getMovieUriFromTmdbId(movieId)}/Aka$infoId"
	}
	
	def getAwardUriFromFreebaseId(id: String): String = {
		s"lod:FreebaseAward$id"
	}

	def getImdbMovieUri(id: String): String = {
		s"http://www.imdb.com/title/$id"
	}

	def getFreebaseUri(id: String): String = {
		s"http://www.freebase.com$id"
	}

	def getTmdbMovieUri(id: String): String = {
		s"https://www.themoviedb.org/movie/$id"
	}

	def getTmdbPersonUri(id: String): String = {
		s"https://www.themoviedb.org/person/$id"
	}
	
	def getTmdbMovieUri(id: Long): String = {
		s"https://www.themoviedb.org/movie/$id"
	}

	def getTmdbPersonUri(id: Long): String = {
		s"https://www.themoviedb.org/person/$id"
	}
	
	def getOfdbMovieUri(id: String): String = {
		s"http://www.ofdb.de/film/$id,"
	}
	
	def getOfdbPersonUri(id: String): String = {
		s"lod:PersonOFDB$id"
	}
}