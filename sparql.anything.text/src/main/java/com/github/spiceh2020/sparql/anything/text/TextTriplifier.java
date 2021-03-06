package com.github.spiceh2020.sparql.anything.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.IOUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.spiceh2020.sparql.anything.model.IRIArgument;
import com.github.spiceh2020.sparql.anything.model.Triplifier;

public class TextTriplifier implements Triplifier {

	private static Logger logger = LoggerFactory.getLogger(TextTriplifier.class);

	public static final String REGEX = "txt.regex", GROUP = "txt.group", SPLIT = "txt.split";

	public DatasetGraph triplify(String value, Properties properties) throws IOException {
		logger.trace("Triplifying \"{}\"", value);
		DatasetGraph dg = DatasetGraphFactory.create();
		boolean blank_nodes = Triplifier.getBlankNodeArgument(properties);
		Graph g = triplifyValue(properties, blank_nodes, NodeFactory.createBlankNode(), value);
		logger.trace("Number of triples: {}", g.size());
		dg.setDefaultGraph(g);
		return dg;
	}

	@Override
	public DatasetGraph triplify(Properties properties) throws IOException {
		DatasetGraph dg = DatasetGraphFactory.create();

		URL url = Triplifier.getLocation(properties);

		if (url == null) {
			if (properties.containsKey(IRIArgument.CONTENT.toString())) {
				return triplify(properties.getProperty(IRIArgument.CONTENT.toString()), properties);
			}
			return dg;
		}

		String root = Triplifier.getRootArgument(properties, url);
		Charset charset = Triplifier.getCharsetArgument(properties);
		boolean blank_nodes = Triplifier.getBlankNodeArgument(properties);

		String value;
		try {
			value = readFromURL(url, properties, charset.toString());

			Node rootResource;
			if (!blank_nodes) {
				if (root == null) {
					rootResource = NodeFactory.createURI(url.toString());
				} else {
					rootResource = NodeFactory.createURI(root);
				}

			} else {
				rootResource = NodeFactory.createBlankNode();
			}

			Graph g = triplifyValue(properties, blank_nodes, rootResource, value);

			dg.addGraph(NodeFactory.createURI(url.toString()), g);
			dg.setDefaultGraph(g);

		} catch (ArchiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dg;
	}

	private Graph triplifyValue(Properties properties, boolean blank_nodes, Node rootResource, String value) {
		logger.trace("Triplifying {}", value);
		Graph g = GraphFactory.createGraphMem();
		g.add(new Triple(rootResource, RDF.type.asNode(), NodeFactory.createURI(Triplifier.FACADE_X_TYPE_ROOT)));

		Pattern pattern = null;
		if (properties.containsKey(REGEX)) {
			String regexString = properties.getProperty(REGEX);
			try {
				pattern = Pattern.compile(regexString);
				// TODO flags
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage());
				pattern = null;
			}

		}

		int group = -1;
		if (properties.contains(GROUP) && pattern != null) {
			try {
				int gr = Integer.parseInt(properties.getProperty(GROUP));
				if (gr >= 0) {
					group = gr;
				} else {
					logger.warn("Group number is supposed to be a positive integer, using default (group 0)");
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage());
			}
		}

		if (pattern != null) {
			Matcher m = pattern.matcher(value);
			int count = 1;
			while (m.find()) {
				if (group > 1) {
					g.add(new Triple(rootResource, RDF.li(count).asNode(),
							NodeFactory.createLiteralByValue(m.group(group), XSDDatatype.XSDstring)));
				} else {
					g.add(new Triple(rootResource, RDF.li(count).asNode(),
							NodeFactory.createLiteralByValue(m.group(), XSDDatatype.XSDstring)));

				}
				count++;
			}
		} else {
			logger.trace("No pattern set");
			if (properties.containsKey(SPLIT)) {
				logger.trace("Splitting regex: {}", properties.getProperty(SPLIT));
				String[] split = value.split(properties.getProperty(SPLIT));
				for (int i = 0; i < split.length; i++) {
					g.add(new Triple(rootResource, RDF.li(i + 1).asNode(),
							NodeFactory.createLiteralByValue(split[i], XSDDatatype.XSDstring)));
				}

			} else {
				g.add(new Triple(rootResource, RDF.li(1).asNode(),
						NodeFactory.createLiteralByValue(value, XSDDatatype.XSDstring)));
			}

		}

		return g;

	}

	private static String readFromURL(URL url, Properties properties, String charset)
			throws IOException, ArchiveException {
		StringWriter sw = new StringWriter();
//		IOUtils.copy(url.openStream(), sw, Charset.forName(charset));
		InputStream is = Triplifier.getInputStream(url, properties, Charset.forName(charset));
		IOUtils.copy(is, sw, Charset.forName(charset));
		return sw.toString();

	}

	@Override
	public Set<String> getMimeTypes() {
		return Sets.newHashSet("text/plain");
	}

	@Override
	public Set<String> getExtensions() {
		return Sets.newHashSet("txt");
	}
}
