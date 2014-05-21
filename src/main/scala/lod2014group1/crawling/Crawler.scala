package lod2014group1.crawling

import java.net.{URLConnection, URL}
import org.slf4s.Logging
import java.nio.channels._
import org.apache.http.client.utils.URIBuilder
import java.io.{FileOutputStream, File}
import java.util.Random

/**
 * Base class for all crawlers,
 */
abstract class Crawler extends Logging {

	val r = new Random()

	/**
	 * Gets the file for a given URL. If the file is not yet downloaded, this is done automatically.
	 * @param urlString The URL of the file you want to process.
	 * @return The file on the hard-drive and a boolean, indicating success or failure.
	 */
	def getFile(urlString: String): (File, Boolean) = {
		val uriBuilder = new URIBuilder(urlString)
		val file = determineFileName(uriBuilder)
		if (file.exists()) {
			log.debug(s"File ${file.getName} already exists.")
			(file, false)
		} else {
			(downloadFile(uriBuilder.build().toURL, file), true)
		}
	}

	/**
	 * Downloads a file to the local hard-disk.
	 * @param url The java.net.URL of the file to download.
	 * @param file Where to store the file.
	 * @return The saved file on the hard-disk.
	 */
	def downloadFile(url: URL, file: File) = {
		// create folders in case they don't exist yet
		file.getParentFile.mkdirs()

		// faking that we are a chrome browser to get access to imdb
		val connection = url.openConnection()
		connection.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
		connection.addRequestProperty("Accept-Language", "de-DE,de;q=0.8,en-US;q=0.6,en;q=0.4")
		connection.addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36")

		// download file according to http://stackoverflow.com/questions/921262
		val inputStream = connection.getInputStream
		val channel = Channels.newChannel(inputStream)
		val fos = new FileOutputStream(file)
		fos.getChannel().transferFrom(channel, 0, Long.MaxValue)
		file
	}

	private def retry[T](n: Int)(fn: => T): T = {
		try {
			fn
		} catch {
			case e: Throwable =>
				if (n > 1) {
					Thread.sleep(1000 * 60 * 10)
					log.info("Waiting for better times.")
					retry(n - 1)(fn)
				}
				else throw e
		}
	}

	/**
	 * Generates a random time to wait for the next request (we do not want to ddos any website).
	 * @return A time in ms.
	 */
	def getNewRandomWaitingTime(): Long = {
		val d = Math.max(r.nextGaussian * 1 + 0.5, 0) * 1000
		log.debug(s"Waited $d");
		d.toLong
		0
	}

	/**
	 * Starts the crawling for this crawler.
	 */
	def crawl: Unit

	/**
	 * Maps a URL to an unique file name on the harddisk.
	 * @param url  The URL.
	 * @return The file name as a java.io.File.
	 */
	def determineFileName(url: URIBuilder): File
}

object Crawler extends Logging {
	/**
	 * Start crawling for all configured crawlers.
	 */
	def crawl: Unit = {
		val crawlers: List[Crawler] = List(
//			new lod2014group1.crawling.IMDBMoviesListCrawler(),
			new lod2014group1.crawling.ImdbMovieCrawler())
//			new lod2014group1.crawling.ImdbActorCrawler())

		log.debug("Start crawling.")
		crawlers.foreach(crawler => crawler.crawl)
	}
}
