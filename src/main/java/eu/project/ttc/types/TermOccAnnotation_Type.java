
/* First created by JCasGen Wed Jun 24 15:15:04 CEST 2015 */
package eu.project.ttc.types;

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
 * Updated by JCasGen Wed Jun 24 15:15:04 CEST 2015
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
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("eu.project.ttc.types.TermOccAnnotation");
 
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
      jcas.throwFeatMissing("words", "eu.project.ttc.types.TermOccAnnotation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_words);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setWords(int addr, int v) {
        if (featOkTst && casFeat_words == null)
      jcas.throwFeatMissing("words", "eu.project.ttc.types.TermOccAnnotation");
    ll_cas.ll_setRefValue(addr, casFeatCode_words, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public int getWords(int addr, int i) {
        if (featOkTst && casFeat_words == null)
      jcas.throwFeatMissing("words", "eu.project.ttc.types.TermOccAnnotation");
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
      jcas.throwFeatMissing("words", "eu.project.ttc.types.TermOccAnnotation");
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
      jcas.throwFeatMissing("pattern", "eu.project.ttc.types.TermOccAnnotation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_pattern);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setPattern(int addr, int v) {
        if (featOkTst && casFeat_pattern == null)
      jcas.throwFeatMissing("pattern", "eu.project.ttc.types.TermOccAnnotation");
    ll_cas.ll_setRefValue(addr, casFeatCode_pattern, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public String getPattern(int addr, int i) {
        if (featOkTst && casFeat_pattern == null)
      jcas.throwFeatMissing("pattern", "eu.project.ttc.types.TermOccAnnotation");
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
      jcas.throwFeatMissing("pattern", "eu.project.ttc.types.TermOccAnnotation");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_pattern), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_pattern), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_pattern), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_ruleId;
  /** @generated */
  final int     casFeatCode_ruleId;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getRuleId(int addr) {
        if (featOkTst && casFeat_ruleId == null)
      jcas.throwFeatMissing("ruleId", "eu.project.ttc.types.TermOccAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_ruleId);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setRuleId(int addr, String v) {
        if (featOkTst && casFeat_ruleId == null)
      jcas.throwFeatMissing("ruleId", "eu.project.ttc.types.TermOccAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_ruleId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_frequency;
  /** @generated */
  final int     casFeatCode_frequency;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public double getFrequency(int addr) {
        if (featOkTst && casFeat_frequency == null)
      jcas.throwFeatMissing("frequency", "eu.project.ttc.types.TermOccAnnotation");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_frequency);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setFrequency(int addr, double v) {
        if (featOkTst && casFeat_frequency == null)
      jcas.throwFeatMissing("frequency", "eu.project.ttc.types.TermOccAnnotation");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_frequency, v);}
    
  
 
  /** @generated */
  final Feature casFeat_specificity;
  /** @generated */
  final int     casFeatCode_specificity;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public double getSpecificity(int addr) {
        if (featOkTst && casFeat_specificity == null)
      jcas.throwFeatMissing("specificity", "eu.project.ttc.types.TermOccAnnotation");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_specificity);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSpecificity(int addr, double v) {
        if (featOkTst && casFeat_specificity == null)
      jcas.throwFeatMissing("specificity", "eu.project.ttc.types.TermOccAnnotation");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_specificity, v);}
    
  
 
  /** @generated */
  final Feature casFeat_category;
  /** @generated */
  final int     casFeatCode_category;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getCategory(int addr) {
        if (featOkTst && casFeat_category == null)
      jcas.throwFeatMissing("category", "eu.project.ttc.types.TermOccAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_category);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setCategory(int addr, String v) {
        if (featOkTst && casFeat_category == null)
      jcas.throwFeatMissing("category", "eu.project.ttc.types.TermOccAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_category, v);}
    
  
 
  /** @generated */
  final Feature casFeat_lemma;
  /** @generated */
  final int     casFeatCode_lemma;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getLemma(int addr) {
        if (featOkTst && casFeat_lemma == null)
      jcas.throwFeatMissing("lemma", "eu.project.ttc.types.TermOccAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_lemma);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setLemma(int addr, String v) {
        if (featOkTst && casFeat_lemma == null)
      jcas.throwFeatMissing("lemma", "eu.project.ttc.types.TermOccAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_lemma, v);}
    
  
 
  /** @generated */
  final Feature casFeat_langset;
  /** @generated */
  final int     casFeatCode_langset;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getLangset(int addr) {
        if (featOkTst && casFeat_langset == null)
      jcas.throwFeatMissing("langset", "eu.project.ttc.types.TermOccAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_langset);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setLangset(int addr, String v) {
        if (featOkTst && casFeat_langset == null)
      jcas.throwFeatMissing("langset", "eu.project.ttc.types.TermOccAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_langset, v);}
    
  



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

 
    casFeat_ruleId = jcas.getRequiredFeatureDE(casType, "ruleId", "uima.cas.String", featOkTst);
    casFeatCode_ruleId  = (null == casFeat_ruleId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_ruleId).getCode();

 
    casFeat_frequency = jcas.getRequiredFeatureDE(casType, "frequency", "uima.cas.Double", featOkTst);
    casFeatCode_frequency  = (null == casFeat_frequency) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_frequency).getCode();

 
    casFeat_specificity = jcas.getRequiredFeatureDE(casType, "specificity", "uima.cas.Double", featOkTst);
    casFeatCode_specificity  = (null == casFeat_specificity) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_specificity).getCode();

 
    casFeat_category = jcas.getRequiredFeatureDE(casType, "category", "uima.cas.String", featOkTst);
    casFeatCode_category  = (null == casFeat_category) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_category).getCode();

 
    casFeat_lemma = jcas.getRequiredFeatureDE(casType, "lemma", "uima.cas.String", featOkTst);
    casFeatCode_lemma  = (null == casFeat_lemma) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_lemma).getCode();

 
    casFeat_langset = jcas.getRequiredFeatureDE(casType, "langset", "uima.cas.String", featOkTst);
    casFeatCode_langset  = (null == casFeat_langset) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_langset).getCode();

  }
}



    