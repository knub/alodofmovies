package lod2014group1

object Config {
	def DATA_FOLDER = "data"
	def TMDB_API_KEY = "./config/tmdb.api-key"
	def FREEBASE_API_KEY = "./config/freebase.api-key"

	def IMDB_GRAPH = "http://172.16.22.196/imdb"
	def IMDB_UPDATING_GRAPH = "http://172.16.22.196/imdb-updating"
	def FREEBASE_GRAPH = "http://172.16.22.196/freebase"
	def OFDB_GRAPH = "http://172.16.22.196/ofdb"
	def TMDB_GRAPH = "http://172.16.22.196/tmdb"
	def DBPEDIA_GRAPH = "http://172.16.22.196/dbpedia"

	def SPARQL_ENDPOINT = "http://172.16.22.196:8890/sparql"

	def DELETE_FIRST_FLAG = "deleteFirst"
		
	def LOD_PREFIX = "http://purl.org/hpi/movie#"	
		
	object Person extends Enumeration {
		type Person = Value
	    val Dominik, Rice, Stefan, Tanja, Tim, Server, RealServer = Value
	}
}
