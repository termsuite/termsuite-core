

/* First created by JCasGen Mon Nov 28 18:03:45 CET 2016 */
package fr.univnantes.termsuite.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** Stores detailed information about the original source document from which the current CAS was initialized. All information (like size) refers to the source document and not to the document in the CAS which may be converted and filtered by a CAS Initializer. For example this information will be written to the Semantic Search index so that the original document contents can be retrieved by queries.
 * Updated by JCasGen Mon Nov 28 18:03:45 CET 2016
 * XML source: /home/cram-d/git/termsuite-core/src/main/resources/TermSuite_TS.xml
 * @generated */
public class SourceDocumentInformation extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(SourceDocumentInformation.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected SourceDocumentInformation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public SourceDocumentInformation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public SourceDocumentInformation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public SourceDocumentInformation(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: uri

  /** getter for uri - gets URI of document. (For example, file:///MyDirectory/myFile.txt for a simple file or http://incubator.apache.org/uima/index.html for content from a web source.)
   * @generated
   * @return value of the feature 
   */
  public String getUri() {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_uri == null)
      jcasType.jcas.throwFeatMissing("uri", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_uri);}
    
  /** setter for uri - sets URI of document. (For example, file:///MyDirectory/myFile.txt for a simple file or http://incubator.apache.org/uima/index.html for content from a web source.) 
   * @generated
   * @param v value to set into the feature 
   */
  public void setUri(String v) {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_uri == null)
      jcasType.jcas.throwFeatMissing("uri", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    jcasType.ll_cas.ll_setStringValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_uri, v);}    
   
    
  //*--------------*
  //* Feature: offsetInSource

  /** getter for offsetInSource - gets Byte offset of the start of document content within original source file or other input source. Only used if the CAS document was retrieved from an source where one physical source file contained several conceptual documents. Zero otherwise.
   * @generated
   * @return value of the feature 
   */
  public int getOffsetInSource() {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_offsetInSource == null)
      jcasType.jcas.throwFeatMissing("offsetInSource", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    return jcasType.ll_cas.ll_getIntValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_offsetInSource);}
    
  /** setter for offsetInSource - sets Byte offset of the start of document content within original source file or other input source. Only used if the CAS document was retrieved from an source where one physical source file contained several conceptual documents. Zero otherwise. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setOffsetInSource(int v) {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_offsetInSource == null)
      jcasType.jcas.throwFeatMissing("offsetInSource", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    jcasType.ll_cas.ll_setIntValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_offsetInSource, v);}    
   
    
  //*--------------*
  //* Feature: documentIndex

  /** getter for documentIndex - gets The index of the document in the order of processing.
   * @generated
   * @return value of the feature 
   */
  public int getDocumentIndex() {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_documentIndex == null)
      jcasType.jcas.throwFeatMissing("documentIndex", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    return jcasType.ll_cas.ll_getIntValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_documentIndex);}
    
  /** setter for documentIndex - sets The index of the document in the order of processing. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setDocumentIndex(int v) {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_documentIndex == null)
      jcasType.jcas.throwFeatMissing("documentIndex", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    jcasType.ll_cas.ll_setIntValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_documentIndex, v);}    
   
    
  //*--------------*
  //* Feature: nbDocuments

  /** getter for nbDocuments - gets The total number of documents in the collection.
   * @generated
   * @return value of the feature 
   */
  public int getNbDocuments() {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_nbDocuments == null)
      jcasType.jcas.throwFeatMissing("nbDocuments", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    return jcasType.ll_cas.ll_getIntValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_nbDocuments);}
    
  /** setter for nbDocuments - sets The total number of documents in the collection. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setNbDocuments(int v) {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_nbDocuments == null)
      jcasType.jcas.throwFeatMissing("nbDocuments", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    jcasType.ll_cas.ll_setIntValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_nbDocuments, v);}    
   
    
  //*--------------*
  //* Feature: documentSize

  /** getter for documentSize - gets Size of original document in bytes before processing by CAS Initializer. Either absolute file size of size within file or other source.
   * @generated
   * @return value of the feature 
   */
  public int getDocumentSize() {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_documentSize == null)
      jcasType.jcas.throwFeatMissing("documentSize", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    return jcasType.ll_cas.ll_getIntValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_documentSize);}
    
  /** setter for documentSize - sets Size of original document in bytes before processing by CAS Initializer. Either absolute file size of size within file or other source. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setDocumentSize(int v) {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_documentSize == null)
      jcasType.jcas.throwFeatMissing("documentSize", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    jcasType.ll_cas.ll_setIntValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_documentSize, v);}    
   
    
  //*--------------*
  //* Feature: cumulatedDocumentSize

  /** getter for cumulatedDocumentSize - gets Cumulated sum of sizes of original document in bytes before processing by CAS Initializer. Either absolute file size of size within file or other source.
   * @generated
   * @return value of the feature 
   */
  public long getCumulatedDocumentSize() {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_cumulatedDocumentSize == null)
      jcasType.jcas.throwFeatMissing("cumulatedDocumentSize", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    return jcasType.ll_cas.ll_getLongValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_cumulatedDocumentSize);}
    
  /** setter for cumulatedDocumentSize - sets Cumulated sum of sizes of original document in bytes before processing by CAS Initializer. Either absolute file size of size within file or other source. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setCumulatedDocumentSize(long v) {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_cumulatedDocumentSize == null)
      jcasType.jcas.throwFeatMissing("cumulatedDocumentSize", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    jcasType.ll_cas.ll_setLongValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_cumulatedDocumentSize, v);}    
   
    
  //*--------------*
  //* Feature: corpusSize

  /** getter for corpusSize - gets Sum of sizes of original documents in bytes in the input collection.
   * @generated
   * @return value of the feature 
   */
  public long getCorpusSize() {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_corpusSize == null)
      jcasType.jcas.throwFeatMissing("corpusSize", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    return jcasType.ll_cas.ll_getLongValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_corpusSize);}
    
  /** setter for corpusSize - sets Sum of sizes of original documents in bytes in the input collection. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setCorpusSize(long v) {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_corpusSize == null)
      jcasType.jcas.throwFeatMissing("corpusSize", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    jcasType.ll_cas.ll_setLongValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_corpusSize, v);}    
   
    
  //*--------------*
  //* Feature: lastSegment

  /** getter for lastSegment - gets For a CAS that represents a segment of a larger source document, this flag indicates whether this CAS is the final segment of the source document.  This is useful for downstream components that want to take some action after having seen all of the segments of a particular source document.
   * @generated
   * @return value of the feature 
   */
  public boolean getLastSegment() {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_lastSegment == null)
      jcasType.jcas.throwFeatMissing("lastSegment", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_lastSegment);}
    
  /** setter for lastSegment - sets For a CAS that represents a segment of a larger source document, this flag indicates whether this CAS is the final segment of the source document.  This is useful for downstream components that want to take some action after having seen all of the segments of a particular source document. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setLastSegment(boolean v) {
    if (SourceDocumentInformation_Type.featOkTst && ((SourceDocumentInformation_Type)jcasType).casFeat_lastSegment == null)
      jcasType.jcas.throwFeatMissing("lastSegment", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((SourceDocumentInformation_Type)jcasType).casFeatCode_lastSegment, v);}    
  }

    