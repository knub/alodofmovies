package lod2014group1.messaging

import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

object Gzipper  {

	def compress(data: Array[Byte]): Array[Byte] = {
		val baos = new ByteArrayOutputStream
		val gzos = new GZIPOutputStream(baos)
		gzos.write(data)
		gzos.finish()
		gzos.close()
		baos.close()
		baos.toByteArray
	}

	def uncompress(data: Array[Byte]): Array[Byte] = {
		val bais   = new ByteArrayInputStream(data)
		val gzis   = new GZIPInputStream(bais)
		val baos   = new ByteArrayOutputStream
		val buf    = new Array[Byte](10000)
		var read   = gzis.read(buf)

		while (read > 0) {
			baos.write(buf, 0, read)
			read = gzis.read(buf)
		}

		gzis.close()
		baos.close()
		baos.toByteArray
	}
}
