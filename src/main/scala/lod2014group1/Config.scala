package lod2014group1

object Config {
	def DATA_FOLDER = "data"
	def TMDB_API_KEY = ".config/tmdb.api-key"
	def FREEBASE_API_KEY = "./config/freebase.api-key"

	object Person extends Enumeration {
		type Person = Value
	    val Dominik, Rice, Stefan, Tanja, Tim = Value
	}
}
