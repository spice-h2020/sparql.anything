package com.github.spiceh2020.sparql.anything.json.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.RDF;
import org.junit.Ignore;
import org.junit.Test;

import com.github.spiceh2020.sparql.anything.json.JSONTriplifier;
import com.github.spiceh2020.sparql.anything.model.IRIArgument;
import com.github.spiceh2020.sparql.anything.model.Triplifier;
import com.jsoniter.spi.JsonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSON2RDFTransformerTest {

	private String ontologyPrefix = "https://w3id.org/resource/ontology/";
	private Logger log = LoggerFactory.getLogger(JSON2RDFTransformerTest.class);

	@Test
	public void testEmptyAndNull() {

		Triplifier jt = new JSONTriplifier();

		try {
			Properties p1 = new Properties();
			p1.setProperty(IRIArgument.LOCATION.toString(),
					getClass().getClassLoader().getResource("./emptyobject.json").toString());
			DatasetGraph g1 = jt.triplify(p1);

			Properties p2 = new Properties();
			p2.setProperty(IRIArgument.LOCATION.toString(),
					getClass().getClassLoader().getResource("./emptyarray.json").toString());
			DatasetGraph g2 = jt.triplify(p2);
			assertEquals(1L, g1.getDefaultGraph().size());
			assertEquals(1L, g2.getDefaultGraph().size());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		boolean jsonException = false;
		try {
			Properties p3 = new Properties();
			p3.setProperty(IRIArgument.LOCATION.toString(),
					getClass().getClassLoader().getResource("./emptyfile").toString());
			jt.triplify(p3);
		} catch (JsonException e) {
			jsonException = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(jsonException);

		boolean nullPointerException = false;
		try {
			jt.triplify(null);
		} catch (NullPointerException e) {
			nullPointerException = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(nullPointerException);

	}

	@Test
	public void testPrimitive() {
		{

			{
				Triplifier jt = new JSONTriplifier();
				Properties properties = new Properties();
				properties.setProperty(IRIArgument.NAMESPACE.toString(), ontologyPrefix);
				Model m = ModelFactory.createDefaultModel();
				Resource r = m.createResource();
				m.add(r, RDF.type, m.createResource(Triplifier.FACADE_X_TYPE_ROOT));
				m.add(r, m.createProperty(ontologyPrefix + "a"), m.createTypedLiteral(1));
				m.add(r, m.createProperty(ontologyPrefix + "string"), m.createTypedLiteral("string"));
				m.add(r, m.createProperty(ontologyPrefix + "bool"), m.createTypedLiteral(true));
				m.add(r, m.createProperty(ontologyPrefix + "boolf"), m.createTypedLiteral(false));
				m.add(r, m.createProperty(ontologyPrefix + "zero"), m.createTypedLiteral(0));

				DatasetGraph g1;
				try {
					properties.setProperty(IRIArgument.LOCATION.toString(),
							getClass().getClassLoader().getResource("./testprimitive.json").toString());
					g1 = jt.triplify(properties);
					assertTrue(m.getGraph().isIsomorphicWith(g1.getDefaultGraph()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			{
				Triplifier jt = new JSONTriplifier();
				Properties properties = new Properties();
				String root = "https://w3id.org/spice/resource/root";
				properties.setProperty(IRIArgument.NAMESPACE.toString(), ontologyPrefix);
				properties.setProperty(IRIArgument.ROOT.toString(), root);
				properties.setProperty(IRIArgument.BLANK_NODES.toString(), "false");

				Model mn = ModelFactory.createDefaultModel();
				Resource rn = mn.createResource(root);
				mn.add(rn, RDF.type, mn.createResource(Triplifier.FACADE_X_TYPE_ROOT));
				mn.add(rn, mn.createProperty(ontologyPrefix + "a"), mn.createTypedLiteral(1));
				mn.add(rn, mn.createProperty(ontologyPrefix + "string"), mn.createTypedLiteral("string"));
				mn.add(rn, mn.createProperty(ontologyPrefix + "bool"), mn.createTypedLiteral(true));
				mn.add(rn, mn.createProperty(ontologyPrefix + "boolf"), mn.createTypedLiteral(false));
				mn.add(rn, mn.createProperty(ontologyPrefix + "zero"), mn.createTypedLiteral(0));

				DatasetGraph g1;
				try {
					properties.setProperty(IRIArgument.LOCATION.toString(),
							getClass().getClassLoader().getResource("./testprimitive.json").toString());
					g1 = jt.triplify(properties);
					assertTrue(mn.getGraph().isIsomorphicWith(g1.getDefaultGraph()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Test
	public void testNegative() {
		Triplifier jt = new JSONTriplifier();
		Properties properties = new Properties();
		properties.setProperty(IRIArgument.NAMESPACE.toString(), ontologyPrefix);
		Model m = ModelFactory.createDefaultModel();
		Resource r = m.createResource();
		m.add(r, RDF.type, m.createResource(Triplifier.FACADE_X_TYPE_ROOT));
		m.add(r, m.createProperty(ontologyPrefix + "a"), m.createTypedLiteral(1));
		m.add(r, m.createProperty(ontologyPrefix + "string"), m.createTypedLiteral("string"));
		m.add(r, m.createProperty(ontologyPrefix + "bool"), m.createTypedLiteral(true));
		m.add(r, m.createProperty(ontologyPrefix + "boolf"), m.createTypedLiteral(false));
		m.add(r, m.createProperty(ontologyPrefix + "zero"), m.createTypedLiteral(0));
		m.add(r, m.createProperty(ontologyPrefix + "neg"), m.createTypedLiteral(-1));
		m.add(r, m.createProperty(ontologyPrefix + "float"), m.createTypedLiteral(0.1f));

		DatasetGraph g1;
		try {
			properties.setProperty(IRIArgument.LOCATION.toString(),
					getClass().getClassLoader().getResource("./testnumbers.json").toString());
			g1 = jt.triplify(properties);
//			ModelFactory.createModelForGraph(g1.getDefaultGraph()).write(System.out, "TTL");
//			m.write(System.out, "TTL");
			assertTrue(m.getGraph().isIsomorphicWith(g1.getDefaultGraph()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void keys() {
		Triplifier jt = new JSONTriplifier();
		Properties properties = new Properties();
		Model m = ModelFactory.createDefaultModel();
		Resource r = m.createResource();
		m.add(r, RDF.type, m.createResource(Triplifier.FACADE_X_TYPE_ROOT));
		m.add(r, m.createProperty(Triplifier.XYZ_NS + "ab%20cd"), m.createTypedLiteral("ef"));
		m.add(r, m.createProperty(Triplifier.XYZ_NS + "ab%2Dcd"), m.createTypedLiteral("ef"));

		DatasetGraph g1;
		try {
			properties.setProperty(IRIArgument.LOCATION.toString(),
					getClass().getClassLoader().getResource("./whitespaceKeys.json").toString());
			g1 = jt.triplify(properties);
			ModelFactory.createModelForGraph(g1.getDefaultGraph()).write(System.out, "TTL");
//			m.write(System.out,"TTL");
			assertTrue(m.getGraph().isIsomorphicWith(g1.getDefaultGraph()));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testBlankNodeProperty() {
		Triplifier jt = new JSONTriplifier();
		Properties properties = new Properties();
		String root = "https://w3id.org/spice/resource/root";
		properties.setProperty(IRIArgument.NAMESPACE.toString(), ontologyPrefix);
		properties.setProperty(IRIArgument.ROOT.toString(), root);
		properties.setProperty(IRIArgument.BLANK_NODES.toString(), "true");

		Model m = ModelFactory.createDefaultModel();
		Resource r = m.createResource();
		m.add(r, RDF.type, m.createResource(Triplifier.FACADE_X_TYPE_ROOT));
		m.add(r, m.createProperty(ontologyPrefix + "a"), m.createTypedLiteral(1));
		m.add(r, m.createProperty(ontologyPrefix + "string"), m.createTypedLiteral("string"));
		m.add(r, m.createProperty(ontologyPrefix + "bool"), m.createTypedLiteral(true));
		m.add(r, m.createProperty(ontologyPrefix + "boolf"), m.createTypedLiteral(false));
		m.add(r, m.createProperty(ontologyPrefix + "zero"), m.createTypedLiteral(0));

		DatasetGraph g1;
		try {
			properties.setProperty(IRIArgument.LOCATION.toString(),
					getClass().getClassLoader().getResource("./testprimitive.json").toString());
			g1 = jt.triplify(properties);
//			ModelFactory.createModelForGraph(g1.getDefaultGraph()).write(System.out, "TTL");
			assertTrue(m.getGraph().isIsomorphicWith(g1.getDefaultGraph()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testBlankNodeFalse() {
		Triplifier jt = new JSONTriplifier();
		Properties properties = new Properties();
		String root = "https://w3id.org/spice/resource/root";
		properties.setProperty(IRIArgument.NAMESPACE.toString(), ontologyPrefix);
		properties.setProperty(IRIArgument.ROOT.toString(), root);
		properties.setProperty(IRIArgument.BLANK_NODES.toString(), "false");

		Model mn = ModelFactory.createDefaultModel();
		Resource rn = mn.createResource(root);
		mn.add(rn, RDF.type, mn.createResource(Triplifier.FACADE_X_TYPE_ROOT));
		mn.add(rn, mn.createProperty(ontologyPrefix + "a"), mn.createTypedLiteral(1));
		mn.add(rn, mn.createProperty(ontologyPrefix + "string"), mn.createTypedLiteral("string"));
		mn.add(rn, mn.createProperty(ontologyPrefix + "bool"), mn.createTypedLiteral(true));
		mn.add(rn, mn.createProperty(ontologyPrefix + "boolf"), mn.createTypedLiteral(false));
		mn.add(rn, mn.createProperty(ontologyPrefix + "zero"), mn.createTypedLiteral(0));

		DatasetGraph g1;
		try {
			properties.setProperty(IRIArgument.LOCATION.toString(),
					getClass().getClassLoader().getResource("./testprimitive.json").toString());
			g1 = jt.triplify(properties);
//			ModelFactory.createModelForGraph(g1.getDefaultGraph()).write(System.out, "TTL");

			assertTrue(mn.getGraph().isIsomorphicWith(g1.getDefaultGraph()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testBlankNodeFalseNoRoot() {
		Triplifier jt = new JSONTriplifier();
		Properties properties = new Properties();
		String root = getClass().getClassLoader().getResource("./testprimitive.json").toString();
		properties.setProperty(IRIArgument.NAMESPACE.toString(), ontologyPrefix);
		properties.setProperty(IRIArgument.ROOT.toString(), root);
		properties.setProperty(IRIArgument.BLANK_NODES.toString(), "false");

		Model mn = ModelFactory.createDefaultModel();
		Resource rn = mn.createResource(root);
		mn.add(rn, RDF.type, mn.createResource(Triplifier.FACADE_X_TYPE_ROOT));
		mn.add(rn, mn.createProperty(ontologyPrefix + "a"), mn.createTypedLiteral(1));
		mn.add(rn, mn.createProperty(ontologyPrefix + "string"), mn.createTypedLiteral("string"));
		mn.add(rn, mn.createProperty(ontologyPrefix + "bool"), mn.createTypedLiteral(true));
		mn.add(rn, mn.createProperty(ontologyPrefix + "boolf"), mn.createTypedLiteral(false));
		mn.add(rn, mn.createProperty(ontologyPrefix + "zero"), mn.createTypedLiteral(0));

		DatasetGraph g1;
		try {
			properties.setProperty(IRIArgument.LOCATION.toString(),
					getClass().getClassLoader().getResource("./testprimitive.json").toString());
			g1 = jt.triplify(properties);
//			ModelFactory.createModelForGraph(g1.getDefaultGraph()).write(System.out, "TTL");

			assertTrue(mn.getGraph().isIsomorphicWith(g1.getDefaultGraph()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void s() {
		{
			{
				Model m = ModelFactory.createDefaultModel();
				Resource r = m.createResource();
				m.add(r, RDF.type, m.createResource(Triplifier.FACADE_X_TYPE_ROOT));
				m.add(r, RDF.li(1), m.createTypedLiteral(1));
				m.add(r, RDF.li(2), m.createTypedLiteral("abcd"));
				Resource o = m.createResource();
				m.add(r, RDF.li(3), o);
				m.add(o, m.createProperty(ontologyPrefix + "a"), m.createTypedLiteral("a"));
				m.add(r, RDF.li(5), m.createTypedLiteral(4));
				Resource arr = m.createResource();
				m.add(o, m.createProperty(ontologyPrefix + "arr"), arr);
				m.add(arr, RDF.li(1), m.createTypedLiteral(0));
				m.add(arr, RDF.li(2), m.createTypedLiteral(1));

				Properties properties = new Properties();
				properties.setProperty(IRIArgument.NAMESPACE.toString(), ontologyPrefix);
				Triplifier jt = new JSONTriplifier();

				DatasetGraph g1;
				try {
					properties.setProperty(IRIArgument.LOCATION.toString(),
							getClass().getClassLoader().getResource("./testarray.json").toString());
					g1 = jt.triplify(properties);

					ModelFactory.createModelForGraph(g1.getDefaultGraph()).write(System.out, "TTL");
					assertTrue(m.getGraph().isIsomorphicWith(g1.getDefaultGraph()));
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			{
				String root = "https://w3id.org/spice/resource/root";
				Model m = ModelFactory.createDefaultModel();
				Resource r = m.createResource(root);
				m.add(r, RDF.type, m.createResource(Triplifier.FACADE_X_TYPE_ROOT));
				m.add(r, RDF.li(1), m.createTypedLiteral(1));
				m.add(r, RDF.li(2), m.createTypedLiteral("abcd"));
				Resource o = m.createResource(root + "/_3");
				m.add(r, RDF.li(3), o);
				m.add(o, m.createProperty(ontologyPrefix + "a"), m.createTypedLiteral("a"));
				m.add(r, RDF.li(5), m.createTypedLiteral(4));
				Resource arr = m.createResource(root + "/_3/arr");
				m.add(o, m.createProperty(ontologyPrefix + "arr"), arr);
				m.add(arr, RDF.li(1), m.createTypedLiteral(0));
				m.add(arr, RDF.li(2), m.createTypedLiteral(1));
				Properties properties = new Properties();

				properties.setProperty(IRIArgument.NAMESPACE.toString(), ontologyPrefix);
				properties.setProperty(IRIArgument.ROOT.toString(), root);
				properties.setProperty(IRIArgument.BLANK_NODES.toString(), "false");
				Triplifier jt = new JSONTriplifier();

				DatasetGraph g1;
				try {
					properties.setProperty(IRIArgument.LOCATION.toString(),
							getClass().getClassLoader().getResource("./testarray.json").toString());
					g1 = jt.triplify(properties);
					Iterator<Triple> ii = g1.getDefaultGraph().find();
					while (ii.hasNext()) {
						System.err.println(ii.next());
					}
					System.err.println("---");
					ii = m.getGraph().find();
					while (ii.hasNext()) {
						System.err.println(ii.next());
					}
					System.err.println("---");

					assertTrue(m.getGraph().isIsomorphicWith(g1.getDefaultGraph()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	@Ignore // Not sure how to reproduce with the new library
	@Test(expected = JsonException.class)
	public void testDuplicateKeyJSON() {
		{
			Triplifier jt = new JSONTriplifier();
			try {
				Properties p = new Properties();
				p.setProperty(IRIArgument.LOCATION.toString(),
						getClass().getClassLoader().getResource("./testduplicatekey.json").toString());
				jt.triplify(p);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	@Test
	public void testUTF8encoded() {
//		/collection/artworks/p/052/p05259-15628.json {namespace=http://sparql.xyz/facade-x/data/, location=./collection/artworks/p/052/p05259-15628.json
		String f = "./p05259-15628.json";
		Triplifier jt = new JSONTriplifier();
		try {
			Properties p = new Properties();
			p.setProperty(IRIArgument.LOCATION.toString(), getClass().getClassLoader().getResource(f).toString());
			jt.triplify(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Ignore // this seems to work now but keeping the test for future reference
	@Test
	public void testContainsZerosDebug() {
//		/collection/artworks/p/052/p05259-15628.json {namespace=http://sparql.xyz/facade-x/data/, location=./collection/artworks/p/052/p05259-15628.json
		String f = "./t09122-23226.json";
		Triplifier jt = new JSONTriplifier();
		try {
			Properties p = new Properties();
			p.setProperty(IRIArgument.LOCATION.toString(), getClass().getClassLoader().getResource(f).toString());
			DatasetGraph ds = jt.triplify(p);
			Iterator<Quad> i = ds.find(null);
			while (i.hasNext()) {
				Quad q = i.next();
				log.info("{} {} {} {}", new Object[] { q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject() });
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
