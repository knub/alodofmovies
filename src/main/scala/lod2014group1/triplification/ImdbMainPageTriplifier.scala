package lod2014group1.triplification

import java.io.File
import lod2014group1.rdf.{RdfMovieResource, RdfAkaResource, RdfResource, RdfTriple}
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import scala.collection.JavaConversions._
import lod2014group1.rdf.RdfMovieResource._
import org.joda.time.format.DateTimeFormat

class ImdbMainPageTriplifier(val imdbId: String) {

	val movie = RdfResource(s"lod:Movie$imdbId")

	def triplify(content: String): List[RdfTriple] = {
		val doc = Jsoup.parse(content)

		var triples: List[RdfTriple] = List(movie sameAsImdbUrl imdbId, movie isA RdfMovieResource.film)

		val overviewDiv = doc.select(".article.title-overview")
		triples = handleOverviewDiv(overviewDiv) ::: triples

		val mediaDiv = doc.select(".article#titleMediaStrip")
		triples = handleMediaDiv(mediaDiv) ::: triples

		val storyLineDiv = doc.select("#titleStoryLine")
		triples = handleStoryLineDiv(storyLineDiv) ::: triples

		val detailDiv = doc.select("#titleDetails")
		triples = handleDetailDiv(detailDiv) ::: triples

		triples
	}

	def handleOverviewDiv(div: Elements): List[RdfTriple] = {
		var triples: List[RdfTriple] = List()

		val poster = div.select(".image img").attr("src")
		if (!poster.isEmpty) triples = (movie hasPoster poster) :: triples

		val title = div.select(".header [itemprop=name]").text();
		if (!title.isEmpty) triples = List(movie hasTitle title, movie hasLabel title) ::: triples

		val originalTitleDiv = div.select("span.title-extra[itemprop=name]")
		if (originalTitleDiv.size() == 1) {
			val originalTitle = originalTitleDiv.first().ownText()
			triples = List(movie hasOriginalTitle originalTitle) ::: triples
		}

		val year = div.select(".header .nobr a").text();
		if (!year.isEmpty) triples = (movie releasedInYear year) :: triples

		val runtime = div.select(".infobar [itemprop=duration]").text().split(" ")(0).replaceAll("\\D", "");
		if (!runtime.isEmpty) triples = (movie lasts runtime.toInt) :: triples

		val ageRating = div.select(".infobar [itemprop=contentRating]").attr("content")
		if (!ageRating.isEmpty) triples = (movie ageRating ageRating) :: triples

		val genres = div.select((".infobar [itemprop=genre]"))
		if (genres != null)
			genres.foreach(genre => {
				triples = (movie hasGenre genre.text()) :: triples
			})

		val metascoreDiv = div.select(".star-box-details")
		if (metascoreDiv.size() > 1) {
			val metascore = metascoreDiv.text().split("Metascore: ")(1).split(" ")(0)
			if (!metascore.isEmpty) triples = (movie scored metascore) :: triples
		}

		val rating = div.select(".star-box-details [itemprop=ratingValue]").text();
		if (!rating.isEmpty) triples = (movie rated rating) :: triples

		val description = div.select("p[itemprop=description]").text();
		if (!description.isEmpty) triples = (movie hasShortSummary description) :: triples

		triples
	}

	def handleMediaDiv(div: Elements): List[RdfTriple] = {
		var triples: List[RdfTriple] = List()

		// trailers and other videos
		val videos = div.select(".mediastrip_big a")
		videos.foreach(video => {
			val url = "imdb.com" + video.attr("href")
			if (!url.contains("amazon")) {
				triples = (movie hasVideo url) :: triples
			}
		})

		// photo collection
		val photos = div.select(".combined-see-more.see-more a")
		photos.foreach(photo => {
			if (photo.text().contains("photo")) {
				val url = "imdb.com" + photo.attr("href")
				triples = (movie hasPhotos url) :: triples
			}
		})

		triples
	}

	def handleStoryLineDiv(div: Elements): List[RdfTriple] = {
		var triples: List[RdfTriple] = List()

		val storyLineDiv = div.select(".inline.canwrap[itemprop=description] p")
		if (storyLineDiv.size() > 1) {
			val storyLine = storyLineDiv.first().ownText()
			if (!storyLine.isEmpty) triples = (movie hasStoryLine storyLine) :: triples
		}

		val textBlocks = div.select(".txt-block")
		textBlocks.foreach(block => {
			val heading = block.select("h4").text()
			if (heading == "Taglines:") {
				val tagline = block.text().substring(heading.length + 1).dropRight(11)
				triples = (movie hasTagline tagline) :: triples
			}
		})

		val keywordDiv = div.select("div[itemprop=keywords]")
		if (keywordDiv.select("nobr").size() == 0) {
			keywordDiv.select("span[itemprop=keywords]").foreach( keyword => {
				triples = (movie hasKeyword keyword.text()) :: triples
			})
		}

		triples
	}

	def handleDetailDiv(div: Elements): List[RdfTriple] = {
		var triples: List[RdfTriple] = List()

		val textBlocks = div.select(".txt-block")
		textBlocks.foreach(block => {
			val heading = block.select("h4").text()

			heading match {
				case "Country:" =>
					block.select("a").foreach(country => {
						triples = (movie releasedInCountry country.text()) :: triples
					})
				case "Language:" =>
					block.select("a").foreach(language => {
						triples = (movie shotInLanguage language.text()) :: triples
					})
				case "Release Date:" =>
					val formatter = DateTimeFormat.forPattern("dd MMM yyyy");
					val dateStr = (block.text().substring(heading.length + 1)).split("\\(")(0).dropRight(1)
					try {
						val date = formatter.parseDateTime(dateStr)
						triples = (movie releasedOn date) :: triples
					} catch {
						case e: Exception =>
							triples = (movie releasedOn dateStr) :: triples
					}
				case "Filming Locations:" =>
					if (block.select("span.see-more.inline").size == 0) {
						block.select("a").foreach(location => {
							triples = (movie filmedInLocation location.text()) :: triples
						})
					}
				case "Also Known As:" =>
					if (block.select("span.see-more.inline").size == 0) {
						block.select("a").zipWithIndex.foreach{ case(aka, index) => {
							val akaRes = RdfResource(s"lod:Movie$imdbId/Aka$index")

							triples = List(movie alsoKnownAs akaRes,
								akaRes isAn RdfAkaResource.alternativeMovieName,
								akaRes hasName aka.text,
								akaRes hasLabel aka.text
							) ::: triples
						}}
					}
				case "Budget:" =>
					val budget = block.text().substring(heading.length + 1)
					triples = (movie hasBudget budget) :: triples
				case "Production Co:" =>
					block.select("span[itemprop=creator] a").foreach(company => {
						triples = (movie distributedBy company.text()) :: triples
					})
				case "Sound Mix:" =>
					block.select("a").foreach(mix => {
						triples = (movie hasSoundMix mix.text()) :: triples
					})
				case "Color:" =>
					block.select("a").foreach(a => {
						a.text() match {
							case "Color" => triples = (movie belongsTo colorFilm) :: triples
							case "Black and White" =>  triples = (movie belongsTo blackAndWhiteFilm) :: triples
						}
					})
				case "Aspect Ratio:" =>
					val ratio = block.text().substring(heading.length + 1)
					triples = (movie hasAspectRatio ratio) :: triples
				case _ =>
			}
		})

		triples
	}

}