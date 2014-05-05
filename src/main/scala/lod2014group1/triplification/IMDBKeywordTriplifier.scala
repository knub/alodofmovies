package lod2014group1.triplification

import java.io.File
import lod2014group1.rdf.RDFTriple
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import scala.collection.JavaConversions._

class IMDBKeywordTriplifier {

  def triplify(f: File): List[RDFTriple] = {
    val doc = Jsoup.parse(f, null)

    val keywordsDiv = doc.getElementById("keywords_content")
    val keywordsTable = keywordsDiv.select("table")

    triplifyKeywords(keywordsTable)

    List()
  }

  def triplifyKeywords(table: Elements): List[RDFTriple] = {
    table.select("tr td a").foreach( keyword => {
      System.out.println("keyword: " + keyword.ownText())
      System.out.println("url: " + keyword.attr("href"))
    })

    List()
  }

}