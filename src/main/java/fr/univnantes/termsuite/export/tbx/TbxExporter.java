package fr.univnantes.termsuite.export.tbx;

import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.io.Writer;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.framework.Export;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.CompoundType;
import fr.univnantes.termsuite.model.Form;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermOccurrence;
import fr.univnantes.termsuite.model.Relation;

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
	
	@Export
	public void export(TerminologyService termino, Writer writer, OccurrenceStore occurrenceStore) {
		NUMBER_FORMATTER.setMaximumFractionDigits(4);
		NUMBER_FORMATTER.setMinimumFractionDigits(4);
		NUMBER_FORMATTER.setRoundingMode(RoundingMode.UP);
		NUMBER_FORMATTER.setGroupingUsed(false);
		try {
			Document tbxDocument = prepareTBXDocument();
			
			try {
				for(Term t: termino.getTerms()) {
					addTermEntry(tbxDocument, termino, occurrenceStore, t, false);
					termino.outboundRelations(t).forEach(v->
						addTermEntry(tbxDocument, termino, occurrenceStore, v.getTo(), true));
				}
				exportTBXDocument(tbxDocument, writer);
			} catch (TransformerException e) {
				LOGGER.error("An error occurred when exporting term index to file");
				throw new TermSuiteException(e);
			}
			
		} catch (ParserConfigurationException e) {
			throw new TermSuiteException(e);
		}
	}

	
	/**
     * Prepare the TBX document that will contain the terms.
     */
	private Document prepareTBXDocument() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document tbxDocument = builder.newDocument();

		Element martif = tbxDocument.createElement("martif");
		martif.setAttribute("type", "TBX");
		tbxDocument.appendChild(martif);

		Element header = tbxDocument.createElement("martifHeader");
		martif.appendChild(header);

		Element fileDesc = tbxDocument.createElement("fileDesc");
		header.appendChild(fileDesc);

		Element encodingDesc = tbxDocument.createElement("encodingDesc");
		header.appendChild(encodingDesc);

		Element encodingP = tbxDocument.createElement("p");
		encodingP.setAttribute("type", "XCSURI");
		encodingP.setTextContent("http://ttc-project.googlecode.com/files/ttctbx.xcs");
		encodingDesc.appendChild(encodingP);

		Element sourceDesc = tbxDocument.createElement("sourceDesc");
		Element p = tbxDocument.createElement("p");
//		p.setTextContent(workingDir.getAbsolutePath());
		sourceDesc.appendChild(p);
		fileDesc.appendChild(sourceDesc);

		Element text = tbxDocument.createElement("text");
		martif.appendChild(text);

        Element body = tbxDocument.createElement("body");
		text.appendChild(body);
		return tbxDocument;
    }


	
    /**
     * Export the TBX document to a file specified in parameter.
     *
     * @throws TransformerException
     */
    private void exportTBXDocument(Document tbxDocument, Writer writer) throws TransformerException {
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
		DOMSource source = new DOMSource(tbxDocument);
		StreamResult result = new StreamResult(writer);
		transformer.transform(source, result);
	}
    
    
    private int currentId=0;
    private Map<Term, Integer> ids = new HashMap<>();
    
    private int getId(Term t) {
    	if(!ids.containsKey(t))
    		ids.put(t, ++currentId);
    	return ids.get(t);
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
	private void addTermEntry(Document tbxDocument, TerminologyService termino, OccurrenceStore occurrenceStore, Term term, boolean isVariant) {
		String langsetId = LANGSET_ID_PREFIX + getId(term);
        Node body = tbxDocument.getElementsByTagName("body").item(0);

		Element termEntry = tbxDocument.createElement("termEntry");
		termEntry.setAttribute("xml:id",
				TERMENTRY_ID_PREFIX + getId(term));
		body.appendChild(termEntry);
		Element langSet = tbxDocument.createElement("langSet");
		langSet.setAttribute("xml:id", langsetId);
		langSet.setAttribute("xml:lang", termino.getLang().getCode());
		termEntry.appendChild(langSet);

		for (Relation variation : termino.inboundRelations(term).collect(toSet())) 
			this.addTermBase(tbxDocument, langSet, variation.getFrom().getGroupingKey(), null);

		for (Relation variation : termino.outboundRelations(term).collect(toSet())) {
			this.addTermVariant(tbxDocument, langSet, String.format("langset-%d", getId(variation.getTo())),
					variation.getTo().getGroupingKey());
		}
		Collection<TermOccurrence> allOccurrences = occurrenceStore.getOccurrences(term);
		this.addDescrip(tbxDocument, langSet, langSet, "nbOccurrences", allOccurrences.size());

		Element tig = tbxDocument.createElement("tig");
		tig.setAttribute("xml:id", TIG_ID_PREFIX + getId(term));
		langSet.appendChild(tig);
		Element termElmt = tbxDocument.createElement("term");
		termElmt.setTextContent(term.getGroupingKey());
		tig.appendChild(termElmt);

		List<Form> forms = occurrenceStore.getForms(term);
		addNote(tbxDocument, langSet, tig, "termPilot", term.getPilot());

		this.addNote(tbxDocument, langSet, tig, "termType", isVariant ? "variant" : "termEntry");
		this.addNote(tbxDocument, 
				langSet,
				tig,
				"partOfSpeech",
				term.isMultiWord() ? "noun" : term.getWords().get(0).getSyntacticLabel());
		this.addNote(tbxDocument, langSet, tig, "termPattern", term.getWords().get(0).getSyntacticLabel());
		this.addNote(tbxDocument, langSet, tig, "termComplexity",
				this.getComplexity(term));
		this.addDescrip(tbxDocument, langSet, tig, "termSpecificity",
				NUMBER_FORMATTER.format(term.getSpecificity()));
		this.addDescrip(tbxDocument, langSet, tig, "nbOccurrences",
				term.getFrequency());
		this.addDescrip(tbxDocument, langSet, tig, "relativeFrequency",
				NUMBER_FORMATTER.format(term.getFrequency()));
		addDescrip(tbxDocument, langSet, tig, "formList",
					buildFormListJSON(occurrenceStore, term, forms.size()));
		 this.addDescrip(tbxDocument, langSet, tig, "domainSpecificity",
				 term.getSpecificity());
	}
	
	private void addDescrip(Document tbxDocument, Element lang, Element element,
			String type, Object value) {
		Element descrip = tbxDocument.createElement("descrip");
		element.appendChild(descrip);
		descrip.setAttribute("type", type);
		descrip.setTextContent(value.toString());
	}

	private void addTermBase(Document tbxDocument, Element lang, String target, Object value) {
		Element descrip = tbxDocument.createElement("descrip");
		lang.appendChild(descrip);
		descrip.setAttribute("type", "termBase");
		descrip.setAttribute("target", "#"+target);
		if (value != null) {
			descrip.setTextContent(value.toString());
		}
	}

	private void addTermVariant(Document tbxDocument, Element lang, String target,
			Object value) {
		Element descrip = tbxDocument.createElement("descrip");
		lang.appendChild(descrip);
		descrip.setAttribute("type", "termVariant");
		descrip.setAttribute("target", "#"+target);
		if (value != null) {
			descrip.setTextContent(value.toString());
		}
	}

	private void addNote(Document tbxDocument, Element lang, Element element,
			String type, Object value) {
		Element termNote = tbxDocument.createElement("termNote");
		element.appendChild(termNote);
		termNote.setAttribute("type", type);
		termNote.setTextContent(value.toString());
	}

	private String buildFormListJSON(OccurrenceStore occurrenceStore, Term term, int size) {
		StringBuilder sb = new StringBuilder("[");

		int i = 0;
		for (Form form:occurrenceStore.getForms(term)) {
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
				if(term.getWords().get(0).getWord().getCompoundType() == CompoundType.NEOCLASSICAL)
					return "neoclassical-compound";
				else
					return "compound";
			} else
				return "single-word";
		}
			return "multi-word";
	}
}
