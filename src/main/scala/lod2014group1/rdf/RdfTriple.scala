package lod2014group1.rdf

import org.joda.time.DateTime

case class RdfResource(val uri: String) extends RdfObject {
	def buildTriple(predicate: RdfResource, obj: RdfObject): RdfTriple = {
		RdfTriple(this, predicate, obj);
	}

	override def toString(): String = {
		if (uri startsWith "http")
			"<" + uri + ">"
		else
			uri
	}

	def isA(entity: RdfResource): RdfTriple = this.buildTriple(RdfResource("rdf:type"), entity)
	def isAn = isA _

	def sameAs(url: String): RdfTriple = this.buildTriple(RdfResource("owl:sameAs"), RdfResource(url)).addAlways

	def name(name: String): RdfTriple = buildTriple(RdfResource("dbpprop:name"), RdfString(name))
	def hasName = name _

	def hasAlternativeName(name: String): RdfTriple = buildTriple(RdfResource("dbpprop:alternativeNames"), RdfString(name)).addAlways

	def label(name: String): RdfTriple = buildTriple(RdfResource("rdfs:label"), RdfString(name))
	def hasLabel = label _

	def country(country: String): RdfTriple = buildTriple(RdfResource("dbpprop:country"), RdfString(country))
	def inCountry = country _

	def year(year: String): RdfTriple = buildTriple(RdfResource("dbpprop:years"), RdfString(year))
	def inYear = year _

	def description(description: String): RdfTriple = buildTriple(RdfResource("dbpprop:description"), RdfString(description)).addAlways
	def hasDescription = description _

	def shortDescription(shortDescription: String): RdfTriple = buildTriple(RdfResource("dbpprop:shortDescription"), RdfString(shortDescription)).addAlways
	def hasShortDescription = shortDescription _

	def abstractContent(abstractStr: String): RdfTriple = buildTriple(RdfResource("dbpedia-owl:abstact"), RdfString(abstractStr))

	def dateRes(date: String): RdfTriple = buildTriple(RdfResource("dbpprop:date"), RdfString(date))

	def dateRes(date: DateTime): RdfTriple = buildTriple(RdfResource("dbpprop:date"), RdfDate(date))

	def subject(resource: RdfResource): RdfTriple = buildTriple(RdfResource("dcterms:subject"), resource).addAlways

	def hasAward(award: RdfResource): RdfTriple = buildTriple(RdfResource("lod:hasAward"), award)

	def image(image: String): RdfTriple = buildTriple(RdfResource("dbpprop:image"), RdfUrl(image)).addAlways
	def hasImage = image _
}
case class RdfTripleString(s: String, p: String, o: String) {
	override def toString(): String = {
		s"$s $p $o ."
	}
}

case class RdfTriple(s: RdfResource, p: RdfResource, o: RdfObject) {

	var addAlwaysFlag: Boolean = false

	def addAlways: RdfTriple = {
		addAlwaysFlag = true
		this
	}
	
	override def toString: String = {
		s"$s $p $o ."
	}

	def toRdfTripleString(): RdfTripleString = {
		RdfTripleString(s.toString, p.toString, o.toString)
	}
	
	
}
