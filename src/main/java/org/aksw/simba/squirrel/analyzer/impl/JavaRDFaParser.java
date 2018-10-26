package org.aksw.simba.squirrel.analyzer.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import javax.xml.bind.JAXB;

import org.aksw.simba.squirrel.analyzer.Analyzer;
import org.aksw.simba.squirrel.data.uri.CrawleableUri;
import org.aksw.simba.squirrel.sink.Sink;
import org.apache.jena.graph.Factory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.xerces.jaxp.JAXPConstants;
import org.hamcrest.core.Is;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import ch.qos.cal10n.util.Parser;
import net.rootdev.javardfa.ParserFactory;
import net.rootdev.javardfa.ParserFactory.Format;
import net.rootdev.javardfa.jena.JenaStatementSink;
import net.rootdev.javardfa.Setting;
import net.rootdev.javardfa.StatementSink;

public class JavaRDFaParser implements Analyzer {

	@Override
	public Iterator<byte[]> analyze(CrawleableUri curi, File data, Sink sink) {
		try {
			Model m = ModelFactory.createDefaultModel();
			
	        StatementSink statesink = new org.aksw.simba.squirrel.analyzer.impl.JenaStatementSink(m);
	        //statesink.setBase(curi.getUri().toString());
	        XMLReader parser = ParserFactory.createReaderForFormat(statesink, Format.XHTML, Setting.OnePointOne);
	        //parser.setProperty(JAXPConstants.JAXP_SCHEMA_SOURCE, curi.getUri().toString());
				parser.parse(data.getAbsolutePath());
				String syntax = "N-TRIPLE"; //"N-TRIPLE" and "TURTLE"
				StringWriter out = new StringWriter();
				m.write(out, syntax,curi.getUri().toString());
				String result = out.toString();
				result = replaceBaseUri(result,curi.getUri().toString(),data.getPath());
				result = addVocabulary(result);
				sink.addData(curi, result);
		} catch (SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		}	
	
	private String addVocabulary(String text) {
		text = text.replace("rdf:", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		text = text.replace("xsd:", "http://www.w3.org/2001/XMLSchema#");
		text = text.replace("xhv:", "http://www.w3.org/1999/xhtml/vocab#");
		text = text.replace("foaf:", "http://xmlns.com/foaf/0.1/");
		text = text.replace("owl:", "http://www.w3.org/2002/07/owl#");
		text = text.replace("earl:", "http://www.w3.org/ns/earl#");
		text = text.replace("dc11:", "http://purl.org/dc/elements/1.1/");
		text = text.replace("ical:", "http://www.w3.org/2002/12/cal/icaltzd#");
		text = text.replace("dcterms:", "http://purl.org/dc/terms/");
		text = text.replace("ex:", "http://example.org/rdf/");
		text = text.replace("vcard:", "http://www.w3.org/2006/vcard/ns#");
		text = text.replace("rdfa:", "http://www.w3.org/ns/rdfa#");
		text = text.replace("xpr:", "http://rdfa.info/test-suite/test-cases/rdfa1.1/xml/relative/uri#");
		text = text.replace("pr:", "http://rdfa.info/test-suite/test-cases/rdfa1.1/xml/relative/iri#");
		text = text.replace("schema:", "http://schema.org/");
		text = text.replace("cc:", "http://creativecommons.org/ns#");
		text = text.replace("dc:", "http://purl.org/dc/terms/");
		text = text.replace("rdfs:", "http://www.w3.org/2000/01/rdf-schema#");
		return text;
	}
	
	private String replaceBaseUri(String result,String base,String oldbase) {
		oldbase = "file:///"+oldbase.replace("\\", "/");
		oldbase = oldbase.substring(0, oldbase.lastIndexOf("/"));
		base = base.substring(0, base.lastIndexOf("/"));
		result = result.replace(oldbase, base);
		//System.out.println(result);
		//System.out.println(base);
		//System.out.println(oldbase);
		return result;
	}
	
}