package lod2014group1.triplification

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import lod2014group1.Config
import scala.io.Source
import lod2014group1.rdf.{RdfReleaseInfoResource, RdfTriple, RdfResource}
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import scala.collection.JavaConversions._
import lod2014group1.rdf.RdfReleaseInfoResource._
import lod2014group1.rdf.RdfAkaResource._
import lod2014group1.rdf.RdfMovieResource._
import lod2014group1.rdf.RdfPersonResource._
import lod2014group1.rdf.RdfCharacterResource._
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}
import lod2014group1.rdf
import scala.io.Codec
import java.nio.charset.CodingErrorAction

class OfdbTriplifier(val ofdbId: Integer) {
	val OFDB_PATH = s"${Config.DATA_FOLDER}/OFDB"
	val movie = movieResourceFromRdfResource(RdfResource(s"lod:MovieOFDB$ofdbId"))
	implicit val codec = Codec("UTF-8")
	codec.onMalformedInput(CodingErrorAction.REPLACE)
	codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
	val filmPath = s"${OFDB_PATH}/Movies/$ofdbId/Film.html"
	val castPath = s"${OFDB_PATH}/Movies/$ofdbId/Cast.html"
	
	def triplify(): List[RdfTriple] = {
		var triples: List[RdfTriple] = List()
		triples = triplifyFilm()
		triples = triplifyCast() ::: triples
		triples
	}

	def triplifyFilm(): List[RdfTriple] = {		
		var triples: List[RdfTriple] = List()
		
		if (!Files.exists(Paths.get(filmPath))){
			println(s"$filmPath not found. Could not triplify.")
			return triples
		}
		val docString = Source.fromFile(filmPath)(codec).mkString
		
		if(docString.contains("Unter dieser ID existiert kein Film."))
			return triples

		val imdbIdSplit = """http://www.imdb.com/Title?"""
		if(docString.contains(imdbIdSplit)){
			val imdbId = "http://imdb.com/title/tt" + docString.split(imdbIdSplit, 2)(1).split(""" """, 1)(0)
			triples = (movie sameAs imdbId) :: triples
			triples = (movie sameAs s"lod:Moviett$imdbId") :: triples
		}
		
		val titleSplit = """<h1 itemprop="name"><font face="Arial,Helvetica,sans-serif" size="3"><b>"""
		if(docString.contains(titleSplit)){
			val title = docString.split(titleSplit, 2)(1).split("""</b>""", 1)(0)
			triples = (movie hasName title) :: triples
			triples = (movie hasLabel title) :: triples
		}
		
		val altTitlesSplit = """&nbsp;&nbsp;Alternativtitel:</font></td><td>"""
		if(docString.contains(altTitlesSplit)){
			val altTitlesParts = docString.split(altTitlesSplit, 2)(1).split("""</li></ul>""", 1)(0).split("""<b>""")
			for (i <- 1 to altTitlesParts.length){
				var altTitle = ""
				if(i == altTitlesParts.length){
					altTitle = altTitlesParts(i).split("""\n</b>""", 1)(0)
				}
				else{
					altTitle = altTitlesParts(i).split("""</b>""", 1)(0)
				}
				triples = (movie hasAlternativeName altTitle) :: triples
			}
		}
		
		val genreSplit = """<span itemprop="genre">"""
		if(docString.contains(genreSplit)){
			val genreParts = docString.split(genreSplit)
			for (i <- 1 to genreParts.length){
				val genre = genreParts(i).split("""</span>""", 1)(0)
				triples = (movie hasGenre genre) :: triples
			}
		}
		
		val originalTitleSplit = """Originaltitel:</font></td>
            <td>&nbsp;&nbsp;</td>
            <td width="99%"><font face="Arial,Helvetica,sans-serif" size="2" class="Daten"><b>"""
		if(docString.contains(originalTitleSplit)){
			val originalTitle = docString.split(originalTitleSplit, 2)(1).split("""\"""", 1)(0)
			triples = (movie hasOriginalTitle originalTitle) :: triples
		}
		
		val countrySplit = """Kat=Land&Text="""
		if(docString.contains(countrySplit)){
			val country = docString.split(countrySplit, 2)(1).split("""\"""", 1)(0)
			triples = (movie inCountry country) :: triples
		}
		
		val yearSplit = """Kat=Jahr&Text="""
		if(docString.contains(yearSplit)){
			val year = docString.split(yearSplit, 2)(1).split("""\"""", 1)(0)
			triples = (movie inYear year) :: triples
		}
		
		val ratingAverageSplit = """Note: <span itemprop="ratingValue">"""
		if(docString.contains(ratingAverageSplit)){
			val ratingAverage = docString.split(ratingAverageSplit, 2)(1).split("""</span>""", 1)(0)
			triples = (movie hasOfdbVoteAverage ratingAverage) :: triples
		}
		
		val ratingCountSplit = """Stimmen: <span itemprop="ratingCount">"""
		if(docString.contains(ratingCountSplit)){
			val ratingCount = docString.split(ratingCountSplit, 2)(1).split("""</span>""", 1)(0)
			triples = (movie hasOfdbVoteCount ratingCount) :: triples
		}
		
		val versionsSplit = """Fassungen <table> """
		if(docString.contains(versionsSplit)){
			val versionsCountryParts = docString.split(versionsSplit, 2)(1).split("""class="Normal">""")
			for (i <- 1 to versionsCountryParts.length){
				val versionCountry = versionsCountryParts(i).split("""&nbsp;""", 1)(0)
				val versionNameParts = versionsCountryParts(i).split("""SHADOW,true)">""")
				for (j <- 1 to versionNameParts.length){
					val version = versionCountry + " " + versionNameParts(j).split("""</a>""", 1)(0)
					triples = (movie hasVersion version) :: triples
				}
			}
		}

		triples
	}
	
	def triplifyCast(): List[RdfTriple] = {		
		var triples: List[RdfTriple] = List()
		
		if (!Files.exists(Paths.get(filmPath))){
			println(s"$filmPath not found. Could not triplify.")
			return triples
		}
		val docString = Source.fromFile(filmPath)(codec).mkString
		
		if(docString.contains("Unter dieser ID existiert kein Film."))
			return triples
		
		val jobTypeSplit = """</table>"""
		val jobStartSplit = """style="font-size: 15px;"><b><i>"""
		val jobEndSplit = """</i>"""
			
		val personSplit = """<tr valign="middle">"""
		val personNameStartSplit = """<b>"""
		val personNameEndSplit = """</b>"""
			
		val personAliasStartSplit = """als <i>"""
		val personAliasEndSplit = """</i>"""
			
		val personRoleStartSplit = """>... """
		val personRoleEndSplit = """</font>"""
		
		if(docString.contains(jobTypeSplit)){
			val jobTypeParts = docString.split(jobTypeSplit)
			for(i <- 0 to jobTypeParts.length-1){
				val jobTypePart = jobTypeParts(i)
				val jobName = jobTypePart.split(jobStartSplit, 2)(0).split(jobEndSplit, 1)(0)
	
				if(jobTypePart.contains(personSplit)){
					val persons = jobTypePart.split(personSplit)
					for(j <- 1 to persons.length){
						val personPart = persons(j)
						var personName = ""
						if(personPart.contains(personAliasStartSplit)){
							personName = personPart.split(personAliasStartSplit, 2)(1).split(personAliasEndSplit, 1)(0)
						}
						else{
							personName = personPart.split(personNameStartSplit, 2)(1).split(personNameEndSplit, 1)(0)
						}
						val currentPerson = personResourceFromRdfResource(RdfResource(s"lod:PersonOFDB$personName"))
						triples = (currentPerson isA person) :: triples
						triples = (currentPerson hasName personName) :: triples
						triples = (currentPerson hasLabel personName) :: triples						
						//get jobType
						if (jobName.equals("Regie")){
							triples = (currentPerson isA director) :: triples
							triples = (movie directedBy currentPerson) :: triples
							triples = (currentPerson hasJob "Director") :: triples
						}
						
						else if (jobName.equals("Drehbuchautor(in)")){
							triples = (currentPerson isA writer) :: triples
							triples = (movie writtenBy currentPerson) :: triples
							triples = (currentPerson hasJob "Writer") :: triples
						}
						
						else if (jobName.equals("Produzent(in)")){
							triples = (currentPerson isA producer) :: triples
							triples = (movie producedBy currentPerson) :: triples
							triples = (currentPerson hasJob "Producer") :: triples
						}
						
						else if (jobName.equals("Director of Photography (Kamera)")){
							triples = (movie cinematographyBy currentPerson) :: triples
							triples = (currentPerson hasJob "Cinematographer") :: triples
						}
						
						else if (jobName.equals("Cutter (Schnitt)")){
							triples = (movie hasOtherCrew currentPerson) :: triples
							triples = (currentPerson hasJob "Cutter") :: triples
						}
						
						else if (jobName.equals("Stunts")){
							triples = (movie stuntsBy currentPerson) :: triples
							triples = (currentPerson hasJob "Stuntman") :: triples
						}
						
						else if (jobName.equals("Soundtrack")){
							triples = (movie musicBy currentPerson) :: triples
							triples = (currentPerson hasJob "Composer") :: triples
						}
						
						else if (jobName.equals("Stimme/Sprecher")){}
						
						else if (jobName.equals("Darsteller")){
							triples = (currentPerson isA actor) :: triples
							triples = (movie starring currentPerson) :: triples
							if(personPart.contains(personRoleStartSplit)){
								var role = personPart.split(personRoleStartSplit, 2)(1).split(personRoleEndSplit, 1)(0)
								if (role.contains("(")){
									role = role.split("(", 1)(0).trim()
								}
								val currentCharacter = characterInfoResourceFromRdfResource(RdfResource(s"lod:CharacterOFDB$role"))
								val currentCharacterMovie = characterInfoResourceFromRdfResource(RdfResource(s"lod:MovieOFDB$ofdbId/CharacterOFDB$role"))
								triples = (currentCharacterMovie inMovie movie) :: triples
								triples = (currentPerson playsCharacter currentCharacterMovie) :: triples
								triples = (currentCharacterMovie isSubclassOf currentCharacter) :: triples
								triples = (currentCharacter hasLabel role) :: triples
								triples = (currentCharacterMovie hasLabel s"$role in OFDB Movie $ofdbId") :: triples
							}
						}
						
						else{
							triples = (movie hasOtherCrew currentPerson) :: triples
							if(personPart.contains(personRoleStartSplit)){
								var role = personPart.split(personRoleStartSplit, 2)(1).split(personRoleEndSplit, 1)(0)
								if (role.contains("(")){
									role = role.split("(", 1)(0).trim()
								}
								triples = (currentPerson hasJob role) :: triples
							}
						}
					}
				}
			}
		}
		triples
	}
}