package it.cnr.istc.stlab.executor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.QC;

import eu.spice.json2rdf.transformers.JSONTransformer;

public class TupleOpExecutor extends OpExecutor {

	protected TupleOpExecutor(ExecutionContext execCxt) {
		super(execCxt);
	}

	protected QueryIterator execute(final OpGraph opGraph, QueryIterator input) {
		if (opGraph.getNode().isURI()) {
			if (detectTupleURI(opGraph.getNode().getURI())) {
				MimeType mime = detectMimeType(opGraph.getNode().getURI());
				switch (mime) {
				case JSON:
					try {
						Graph g = DatasetFactory.create(tuplifyJSON(getFileURL(opGraph.getNode().getURI())))
								.asDatasetGraph().getDefaultGraph();
						ExecutionContext cxt2 = new ExecutionContext(execCxt, g);
						return QC.execute(opGraph.getSubOp(), input, cxt2);
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				default:
					break;
				}
			}
		}
		return super.execute(opGraph, input);
	}

	protected URL getFileURL(String s) throws MalformedURLException {
		return new URL(s.replace("tuple://", ""));
	}

	protected boolean detectTupleURI(String uri) {
		if (uri.startsWith("tuple://")) {
			return true;
		}
		return false;
	}

	protected MimeType detectMimeType(String uri) {
		if (uri.endsWith(".json")) {
			return MimeType.JSON;
		}
		return null;
	}

	protected Model tuplifyJSON(URL url) throws IOException {

		JSONTransformer jt = new JSONTransformer(url.toString() + "/");
		jt.setURIRoot(url.toString());
		return jt.transformJSONFromURL(url);

	}

}
