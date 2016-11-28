
/* First created by JCasGen Mon Nov 28 18:03:45 CET 2016 */
package fr.univnantes.termsuite.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** Stores detailed information about the original source document from which the current CAS was initialized. All information (like size) refers to the source document and not to the document in the CAS which may be converted and filtered by a CAS Initializer. For example this information will be written to the Semantic Search index so that the original document contents can be retrieved by queries.
 * Updated by JCasGen Mon Nov 28 18:03:45 CET 2016
 * @generated */
public class SourceDocumentInformation_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (SourceDocumentInformation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = SourceDocumentInformation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new SourceDocumentInformation(addr, SourceDocumentInformation_Type.this);
  			   SourceDocumentInformation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new SourceDocumentInformation(addr, SourceDocumentInformation_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = SourceDocumentInformation.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("fr.univnantes.termsuite.types.SourceDocumentInformation");
 
  /** @generated */
  final Feature casFeat_uri;
  /** @generated */
  final int     casFeatCode_uri;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getUri(int addr) {
        if (featOkTst && casFeat_uri == null)
      jcas.throwFeatMissing("uri", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_uri);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setUri(int addr, String v) {
        if (featOkTst && casFeat_uri == null)
      jcas.throwFeatMissing("uri", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    ll_cas.ll_setStringValue(addr, casFeatCode_uri, v);}
    
  
 
  /** @generated */
  final Feature casFeat_offsetInSource;
  /** @generated */
  final int     casFeatCode_offsetInSource;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getOffsetInSource(int addr) {
        if (featOkTst && casFeat_offsetInSource == null)
      jcas.throwFeatMissing("offsetInSource", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    return ll_cas.ll_getIntValue(addr, casFeatCode_offsetInSource);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setOffsetInSource(int addr, int v) {
        if (featOkTst && casFeat_offsetInSource == null)
      jcas.throwFeatMissing("offsetInSource", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    ll_cas.ll_setIntValue(addr, casFeatCode_offsetInSource, v);}
    
  
 
  /** @generated */
  final Feature casFeat_documentIndex;
  /** @generated */
  final int     casFeatCode_documentIndex;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getDocumentIndex(int addr) {
        if (featOkTst && casFeat_documentIndex == null)
      jcas.throwFeatMissing("documentIndex", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    return ll_cas.ll_getIntValue(addr, casFeatCode_documentIndex);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setDocumentIndex(int addr, int v) {
        if (featOkTst && casFeat_documentIndex == null)
      jcas.throwFeatMissing("documentIndex", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    ll_cas.ll_setIntValue(addr, casFeatCode_documentIndex, v);}
    
  
 
  /** @generated */
  final Feature casFeat_nbDocuments;
  /** @generated */
  final int     casFeatCode_nbDocuments;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getNbDocuments(int addr) {
        if (featOkTst && casFeat_nbDocuments == null)
      jcas.throwFeatMissing("nbDocuments", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    return ll_cas.ll_getIntValue(addr, casFeatCode_nbDocuments);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setNbDocuments(int addr, int v) {
        if (featOkTst && casFeat_nbDocuments == null)
      jcas.throwFeatMissing("nbDocuments", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    ll_cas.ll_setIntValue(addr, casFeatCode_nbDocuments, v);}
    
  
 
  /** @generated */
  final Feature casFeat_documentSize;
  /** @generated */
  final int     casFeatCode_documentSize;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getDocumentSize(int addr) {
        if (featOkTst && casFeat_documentSize == null)
      jcas.throwFeatMissing("documentSize", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    return ll_cas.ll_getIntValue(addr, casFeatCode_documentSize);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setDocumentSize(int addr, int v) {
        if (featOkTst && casFeat_documentSize == null)
      jcas.throwFeatMissing("documentSize", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    ll_cas.ll_setIntValue(addr, casFeatCode_documentSize, v);}
    
  
 
  /** @generated */
  final Feature casFeat_cumulatedDocumentSize;
  /** @generated */
  final int     casFeatCode_cumulatedDocumentSize;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public long getCumulatedDocumentSize(int addr) {
        if (featOkTst && casFeat_cumulatedDocumentSize == null)
      jcas.throwFeatMissing("cumulatedDocumentSize", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    return ll_cas.ll_getLongValue(addr, casFeatCode_cumulatedDocumentSize);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setCumulatedDocumentSize(int addr, long v) {
        if (featOkTst && casFeat_cumulatedDocumentSize == null)
      jcas.throwFeatMissing("cumulatedDocumentSize", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    ll_cas.ll_setLongValue(addr, casFeatCode_cumulatedDocumentSize, v);}
    
  
 
  /** @generated */
  final Feature casFeat_corpusSize;
  /** @generated */
  final int     casFeatCode_corpusSize;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public long getCorpusSize(int addr) {
        if (featOkTst && casFeat_corpusSize == null)
      jcas.throwFeatMissing("corpusSize", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    return ll_cas.ll_getLongValue(addr, casFeatCode_corpusSize);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setCorpusSize(int addr, long v) {
        if (featOkTst && casFeat_corpusSize == null)
      jcas.throwFeatMissing("corpusSize", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    ll_cas.ll_setLongValue(addr, casFeatCode_corpusSize, v);}
    
  
 
  /** @generated */
  final Feature casFeat_lastSegment;
  /** @generated */
  final int     casFeatCode_lastSegment;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public boolean getLastSegment(int addr) {
        if (featOkTst && casFeat_lastSegment == null)
      jcas.throwFeatMissing("lastSegment", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_lastSegment);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setLastSegment(int addr, boolean v) {
        if (featOkTst && casFeat_lastSegment == null)
      jcas.throwFeatMissing("lastSegment", "fr.univnantes.termsuite.types.SourceDocumentInformation");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_lastSegment, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public SourceDocumentInformation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_uri = jcas.getRequiredFeatureDE(casType, "uri", "uima.cas.String", featOkTst);
    casFeatCode_uri  = (null == casFeat_uri) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_uri).getCode();

 
    casFeat_offsetInSource = jcas.getRequiredFeatureDE(casType, "offsetInSource", "uima.cas.Integer", featOkTst);
    casFeatCode_offsetInSource  = (null == casFeat_offsetInSource) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_offsetInSource).getCode();

 
    casFeat_documentIndex = jcas.getRequiredFeatureDE(casType, "documentIndex", "uima.cas.Integer", featOkTst);
    casFeatCode_documentIndex  = (null == casFeat_documentIndex) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_documentIndex).getCode();

 
    casFeat_nbDocuments = jcas.getRequiredFeatureDE(casType, "nbDocuments", "uima.cas.Integer", featOkTst);
    casFeatCode_nbDocuments  = (null == casFeat_nbDocuments) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_nbDocuments).getCode();

 
    casFeat_documentSize = jcas.getRequiredFeatureDE(casType, "documentSize", "uima.cas.Integer", featOkTst);
    casFeatCode_documentSize  = (null == casFeat_documentSize) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_documentSize).getCode();

 
    casFeat_cumulatedDocumentSize = jcas.getRequiredFeatureDE(casType, "cumulatedDocumentSize", "uima.cas.Long", featOkTst);
    casFeatCode_cumulatedDocumentSize  = (null == casFeat_cumulatedDocumentSize) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_cumulatedDocumentSize).getCode();

 
    casFeat_corpusSize = jcas.getRequiredFeatureDE(casType, "corpusSize", "uima.cas.Long", featOkTst);
    casFeatCode_corpusSize  = (null == casFeat_corpusSize) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_corpusSize).getCode();

 
    casFeat_lastSegment = jcas.getRequiredFeatureDE(casType, "lastSegment", "uima.cas.Boolean", featOkTst);
    casFeatCode_lastSegment  = (null == casFeat_lastSegment) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_lastSegment).getCode();

  }
}



    