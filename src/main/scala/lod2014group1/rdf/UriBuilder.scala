package lod2014group1.rdf

object UriBuilder {
	def getMovieUriFromImdbID(id : String): String = {
		s"lod:Movie$id"
	}
	
	def getMovieUriFromFreebaseID(id : String): String = {
		s"lod:FreebaseMovie$id"
	}
	
	def getPersonUriFromFreebaseID(id: String): String = {
		s"lod:FreebasePerson$id"
	}
	
	def getPersonUriFromImdbID(id : String): String = {
		s"lod:MoviePerson$id"
	}
	
	def getCharacterUriFromFreebaseID(id :String): String = {
		s"lod:FreebaseCharacter$id"
	}
	
	def getMovieCharacterUriFromFreebaseID(movieId: String, personid:String): String = {
		s"lod:FreebaseMovie$movieId/Character$personid"
	} 
	
	def getReleaseInfoUriFromFreebaseID(id:String): String = {
		s"lod:FreebaseReleaseInfo$id"
	}
	
	def getAwardUriFromFreebaseID(id:String): String = {
		s"lod:FreebaseAward$id"
	}
	
	def freebaseUri(id:String): String = {
		s"http://www.freebase.com$id"
	}
}