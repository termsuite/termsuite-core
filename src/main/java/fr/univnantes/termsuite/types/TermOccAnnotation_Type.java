
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

/** 
 * Updated by JCasGen Mon Nov 28 18:03:45 CET 2016
 * @generated */
public class TermOccAnnotation_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (TermOccAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = TermOccAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new TermOccAnnotation(addr, TermOccAnnotation_Type.this);
  			   TermOccAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new TermOccAnnotation(addr, TermOccAnnotation_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = TermOccAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("fr.univnantes.termsuite.types.TermOccAnnotation");
 
  /** @generated */
  final Feature casFeat_words;
  /** @generated */
  final int     casFeatCode_words;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getWords(int addr) {
        if (featOkTst && casFeat_words == null)
      jcas.throwFeatMissing("words", "fr.univnantes.termsuite.types.TermOccAnnotation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_words);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setWords(int addr, int v) {
        if (featOkTst && casFeat_words == null)
      jcas.throwFeatMissing("words", "fr.univnantes.termsuite.types.TermOccAnnotation");
    ll_cas.ll_setRefValue(addr, casFeatCode_words, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public int getWords(int addr, int i) {
        if (featOkTst && casFeat_words == null)
      jcas.throwFeatMissing("words", "fr.univnantes.termsuite.types.TermOccAnnotation");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_words), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_words), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_words), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setWords(int addr, int i, int v) {
        if (featOkTst && casFeat_words == null)
      jcas.throwFeatMissing("words", "fr.univnantes.termsuite.types.TermOccAnnotation");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_words), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_words), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_words), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_pattern;
  /** @generated */
  final int     casFeatCode_pattern;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getPattern(int addr) {
        if (featOkTst && casFeat_pattern == null)
      jcas.throwFeatMissing("pattern", "fr.univnantes.termsuite.types.TermOccAnnotation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_pattern);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setPattern(int addr, int v) {
        if (featOkTst && casFeat_pattern == null)
      jcas.throwFeatMissing("pattern", "fr.univnantes.termsuite.types.TermOccAnnotation");
    ll_cas.ll_setRefValue(addr, casFeatCode_pattern, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public String getPattern(int addr, int i) {
        if (featOkTst && casFeat_pattern == null)
      jcas.throwFeatMissing("pattern", "fr.univnantes.termsuite.types.TermOccAnnotation");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_pattern), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_pattern), i);
	return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_pattern), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setPattern(int addr, int i, String v) {
        if (featOkTst && casFeat_pattern == null)
      jcas.throwFeatMissing("pattern", "fr.univnantes.termsuite.types.TermOccAnnotation");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_pattern), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_pattern), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_pattern), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_spottingRuleName;
  /** @generated */
  final int     casFeatCode_spottingRuleName;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getSpottingRuleName(int addr) {
        if (featOkTst && casFeat_spottingRuleName == null)
      jcas.throwFeatMissing("spottingRuleName", "fr.univnantes.termsuite.types.TermOccAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_spottingRuleName);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSpottingRuleName(int addr, String v) {
        if (featOkTst && casFeat_spottingRuleName == null)
      jcas.throwFeatMissing("spottingRuleName", "fr.univnantes.termsuite.types.TermOccAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_spottingRuleName, v);}
    
  
 
  /** @generated */
  final Feature casFeat_termKey;
  /** @generated */
  final int     casFeatCode_termKey;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getTermKey(int addr) {
        if (featOkTst && casFeat_termKey == null)
      jcas.throwFeatMissing("termKey", "fr.univnantes.termsuite.types.TermOccAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_termKey);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTermKey(int addr, String v) {
        if (featOkTst && casFeat_termKey == null)
      jcas.throwFeatMissing("termKey", "fr.univnantes.termsuite.types.TermOccAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_termKey, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public TermOccAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_words = jcas.getRequiredFeatureDE(casType, "words", "uima.cas.FSArray", featOkTst);
    casFeatCode_words  = (null == casFeat_words) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_words).getCode();

 
    casFeat_pattern = jcas.getRequiredFeatureDE(casType, "pattern", "uima.cas.StringArray", featOkTst);
    casFeatCode_pattern  = (null == casFeat_pattern) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_pattern).getCode();

 
    casFeat_spottingRuleName = jcas.getRequiredFeatureDE(casType, "spottingRuleName", "uima.cas.String", featOkTst);
    casFeatCode_spottingRuleName  = (null == casFeat_spottingRuleName) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_spottingRuleName).getCode();

 
    casFeat_termKey = jcas.getRequiredFeatureDE(casType, "termKey", "uima.cas.String", featOkTst);
    casFeatCode_termKey  = (null == casFeat_termKey) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_termKey).getCode();

  }
}



    