package lod2014group1.crawling

import java.nio.channels._
import java.io.FileOutputStream
import java.io.File
import lod2014group1.Config

class CrawlingDataManager(val dataName: String) {
	// create the base folder
	val baseFolder = s"${Config.dataFolder}/$dataName"
	new File(baseFolder).mkdirs()

	def saveFile(channel: ReadableByteChannel, fileName: String*): String = {
		// create every folder in-between (last list element is filename, so remove that)
		val remainingFileName = fileName.init.mkString("/")
		val file = new File(s"$baseFolder/$remainingFileName")
		file.mkdirs()

		val completeFileName = s"$baseFolder/$remainingFileName/${fileName.last}"
		val fos = new FileOutputStream(completeFileName)
		// copy over
		fos.getChannel().transferFrom(channel, 0, Long.MaxValue)
		completeFileName
	}
}