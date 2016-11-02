package eu.project.ttc.termino.export;

import java.io.IOException;
import java.io.Writer;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import eu.project.ttc.api.TermSuiteException;
import eu.project.ttc.api.Traverser;
import eu.project.ttc.models.CompoundType;
import eu.project.ttc.models.Form;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermOccurrence;
import eu.project.ttc.models.TermRelation;

public class TbxExporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(TbxExporter.class);
	
	
	/** Prints float out numbers */
	private static final NumberFormat NUMBER_FORMATTER = NumberFormat.getNumberInstance(Locale.US);


	/** Prefix used in langset ids */
	private static final String LANGSET_ID_PREFIX = "langset-";

	/** Prefix used in langset ids */
	private static final String TERMENTRY_ID_PREFIX = "entry-";

	/** Prefix used in langset ids */
	private static final String TIG_ID_PREFIX = "term-";

	
	/* The tbx document */
	private Document document;

	private TermIndex termIndex;
	
	private Traverser traverser;

	private Writer writer;
	
	private TbxExporter(TermIndex termIndex, Writer writer, Traverser traverser) {
		NUMBER_FORMATTER.setMaximumFractionDigits(4);
		NUMBER_FORMATTER.setMinimumFractionDigits(4);
		NUMBER_FORMATTER.setRoundingMode(RoundingMode.UP);
		NUMBER_FORMATTER.setGroupingUsed(false);
		this.writer = writer;
		this.termIndex = termIndex;
		this.traverser = traverser;
	}

	private void doExport() {
		try {
			prepareTBXDocument();
			
			try {
				for(Term t: traverser.toList(termIndex)) {
					addTermEntry(t, false);
					for(TermRelation v:termIndex.getOutboundRelations(t))
						addTermEntry(v.getTo(), true);
				}
				exportTBXDocument();
			} catch (TransformerException | IOException e) {
				LOGGER.error("An error occurred when exporting term index to file");
				throw new TermSuiteException(e);
			}
			
		} catch (ParserConfigurationException e) {
			throw new TermSuiteException(e);
		}
	}

	public static void export(TermIndex termIndex, Writer writer) {
		export(termIndex, writer, Traverser.create());
	}

	public static void export(TermIndex termIndex, Writer writer, Traverser traverser) {
		new TbxExporter(termIndex, writer, traverser).doExport();
	}
	
	/**
     * Prepare the TBX document that will contain the terms.
     */
	private void prepareTBXDocument() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		this.document = builder.newDocument();

		Element martif = document.createElement("martif");
		martif.setAttribute("type", "TBX");
		document.appendChild(martif);

		Element header = document.createElement("martifHeader");
		martif.appendChild(header);

		Element fileDesc = document.createElement("fileDesc");
		header.appendChild(fileDesc);

		Element encodingDesc = document.createElement("encodingDesc");
		header.appendChild(encodingDesc);

		Element encodingP = document.createElement("p");
		encodingP.setAttribute("type", "XCSURI");
		encodingP.setTextContent("http://ttc-project.googlecode.com/files/ttctbx.xcs");
		encodingDesc.appendChild(encodingP);

		Element sourceDesc = document.createElement("sourceDesc");
		Element p = document.createElement("p");
//		p.setTextContent(workingDir.getAbsolutePath());
		sourceDesc.appendChild(p);
		fileDesc.appendChild(sourceDesc);

		Element text = document.createElement("text");
		martif.appendChild(text);

        Element body = document.createElement("body");
		text.appendChild(body);
    }


	
    /**
     * Export the TBX document to a file specified in parameter.
     *
     * @throws TransformerException
     */
    private void exportTBXDocument() throws TransformerException {
        // Prepare the transformer to persist the file
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
				"http://ttc-project.googlecode.com/files/tbxcore.dtd");
		transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		try {
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		} catch (IllegalArgumentException e) {
			throw new TransformerException(e);
		} // Ignore

        // Actually persist the file
		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(this.writer);
		transformer.transform(source, result);
	}
    
    /**
     * Add a term to the TBX document.
     *
     * @param doc
     * @param langsetId
     * @param term
     * @param isVariant
     * @throws IOException
     */
	private void addTermEntry(Term term, boolean isVariant)
			throws IOException {
		String langsetId = LANGSET_ID_PREFIX + term.getId();
        Node body = document.getElementsByTagName("body").item(0);

		Element termEntry = document.createElement("termEntry");
		termEntry.setAttribute("xml:id",
				TERMENTRY_ID_PREFIX + term.getId());
		body.appendChild(termEntry);
		Element langSet = document.createElement("langSet");
		langSet.setAttribute("xml:id", langsetId);
		langSet.setAttribute("xml:lang", this.termIndex.getLang().getCode());
		termEntry.appendChild(langSet);

		for (TermRelation variation : termIndex.getInboundTermRelations(term)) 
			this.addTermBase(langSet, variation.getFrom().getGroupingKey(), null);

		for (TermRelation variation : termIndex.getOutboundRelations(term)) {
			this.addTermVariant(langSet, String.format("langset-%d", variation.getTo().getId()),
					variation.getTo().getGroupingKey());
		}
		Collection<TermOccurrence> allOccurrences = termIndex.getOccurrenceStore().getOccurrences(term);
		this.addDescrip(langSet, langSet, "nbOccurrences", allOccurrences.size());

		Element tig = document.createElement("tig");
		tig.setAttribute("xml:id", TIG_ID_PREFIX + term.getId());
		langSet.appendChild(tig);
		Element termElmt = document.createElement("term");
		termElmt.setTextContent(term.getGroupingKey());
		tig.appendChild(termElmt);

		List<Form> forms = termIndex.getOccurrenceStore().getForms(term);
		addNote(langSet, tig, "termPilot", term.getPilot());

		this.addNote(langSet, tig, "termType", isVariant ? "variant" : "termEntry");
		this.addNote(
				langSet,
				tig,
				"partOfSpeech",
				term.isMultiWord() ? "noun" : term.firstWord().getSyntacticLabel());
		this.addNote(langSet, tig, "termPattern", term.firstWord().getSyntacticLabel());
		this.addNote(langSet, tig, "termComplexity",
				this.getComplexity(term));
		this.addDescrip(langSet, tig, "termSpecificity",
				NUMBER_FORMATTER.format(term.getSpecificity()));
		this.addDescrip(langSet, tig, "nbOccurrences",
				term.getFrequency());
		this.addDescrip(langSet, tig, "relativeFrequency",
				NUMBER_FORMATTER.format(term.getFrequency()));
		addDescrip(langSet, tig, "formList",
					buildFormListJSON(term, forms.size()));
		 this.addDescrip(langSet, tig, "domainSpecificity",
				 term.getSpecificity());
	}
	
	private void addDescrip(Element lang, Element element,
			String type, Object value) {
		Element descrip = document.createElement("descrip");
		element.appendChild(descrip);
		descrip.setAttribute("type", type);
		descrip.setTextContent(value.toString());
	}

	private void addTermBase(Element lang, String target, Object value) {
		Element descrip = document.createElement("descrip");
		lang.appendChild(descrip);
		descrip.setAttribute("type", "termBase");
		descrip.setAttribute("target", "#"+target);
		if (value != null) {
			descrip.setTextContent(value.toString());
		}
	}

	private void addTermVariant(Element lang, String target,
			Object value) {
		Element descrip = document.createElement("descrip");
		lang.appendChild(descrip);
		descrip.setAttribute("type", "termVariant");
		descrip.setAttribute("target", "#"+target);
		if (value != null) {
			descrip.setTextContent(value.toString());
		}
	}

	private void addNote(Element lang, Element element,
			String type, Object value) {
		Element termNote = document.createElement("termNote");
		element.appendChild(termNote);
		termNote.setAttribute("type", type);
		termNote.setTextContent(value.toString());
	}

	private String buildFormListJSON(Term term, int size) {
		StringBuilder sb = new StringBuilder("[");

		int i = 0;
		for (Form form:termIndex.getOccurrenceStore().getForms(term)) {
			if (i > 0)
				sb.append(", ");
			sb.append("{term=\"").append(form);
			sb.append("\", count=").append(form.getCount()).append("}");
			i++;
		}
		sb.append("]");
		return sb.toString();
	}

	private String getComplexity(Term term) {
		if (term.isSingleWord()) {
			if(term.isCompound()) {
				if(term.firstWord().getWord().getCompoundType() == CompoundType.NEOCLASSICAL)
					return "neoclassical-compound";
				else
					return "compound";
			} else
				return "single-word";
		}
			return "multi-word";
	}
}
