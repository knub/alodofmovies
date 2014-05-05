package lod2014group1

object Config {
	def DATA_FOLDER = "data"
	def TMDB_API = "eac7eb8f612f92c83adf6a95ef34cdcc"

	object Person extends Enumeration {
		type Person = Value
	    val Dominik, Rice, Stefan, Tanja, Tim = Value
	}
}
