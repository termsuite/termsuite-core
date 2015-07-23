

/* First created by JCasGen Wed Jun 24 15:15:04 CEST 2015 */
package eu.project.ttc.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.cas.StringArray;


/** 
 * Updated by JCasGen Wed Jun 24 15:15:04 CEST 2015
 * XML source: /home/cram-d/git/termSuite/trunk/ttc-term-suite/resources/eu/project/ttc/types/TermSuiteTypeSystem.xml
 * @generated */
public class TermOccAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(TermOccAnnotation.class);
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
  protected TermOccAnnotation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public TermOccAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public TermOccAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public TermOccAnnotation(JCas jcas, int begin, int end) {
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
  //* Feature: words

  /** getter for words - gets 
   * @generated
   * @return value of the feature 
   */
  public FSArray getWords() {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_words == null)
      jcasType.jcas.throwFeatMissing("words", "eu.project.ttc.types.TermOccAnnotation");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_words)));}
    
  /** setter for words - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setWords(FSArray v) {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_words == null)
      jcasType.jcas.throwFeatMissing("words", "eu.project.ttc.types.TermOccAnnotation");
    jcasType.ll_cas.ll_setRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_words, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for words - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public WordAnnotation getWords(int i) {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_words == null)
      jcasType.jcas.throwFeatMissing("words", "eu.project.ttc.types.TermOccAnnotation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_words), i);
    return (WordAnnotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_words), i)));}

  /** indexed setter for words - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setWords(int i, WordAnnotation v) { 
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_words == null)
      jcasType.jcas.throwFeatMissing("words", "eu.project.ttc.types.TermOccAnnotation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_words), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_words), i, jcasType.ll_cas.ll_getFSRef(v));}
   
    
  //*--------------*
  //* Feature: pattern

  /** getter for pattern - gets The list of regex matcher labels (e.g. [N, A, A, C, A] )
   * @generated
   * @return value of the feature 
   */
  public StringArray getPattern() {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_pattern == null)
      jcasType.jcas.throwFeatMissing("pattern", "eu.project.ttc.types.TermOccAnnotation");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_pattern)));}
    
  /** setter for pattern - sets The list of regex matcher labels (e.g. [N, A, A, C, A] ) 
   * @generated
   * @param v value to set into the feature 
   */
  public void setPattern(StringArray v) {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_pattern == null)
      jcasType.jcas.throwFeatMissing("pattern", "eu.project.ttc.types.TermOccAnnotation");
    jcasType.ll_cas.ll_setRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_pattern, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for pattern - gets an indexed value - The list of regex matcher labels (e.g. [N, A, A, C, A] )
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getPattern(int i) {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_pattern == null)
      jcasType.jcas.throwFeatMissing("pattern", "eu.project.ttc.types.TermOccAnnotation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_pattern), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_pattern), i);}

  /** indexed setter for pattern - sets an indexed value - The list of regex matcher labels (e.g. [N, A, A, C, A] )
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setPattern(int i, String v) { 
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_pattern == null)
      jcasType.jcas.throwFeatMissing("pattern", "eu.project.ttc.types.TermOccAnnotation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_pattern), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_pattern), i, v);}
   
    
  //*--------------*
  //* Feature: ruleId

  /** getter for ruleId - gets The unique name/id of the matched rule
   * @generated
   * @return value of the feature 
   */
  public String getRuleId() {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_ruleId == null)
      jcasType.jcas.throwFeatMissing("ruleId", "eu.project.ttc.types.TermOccAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_ruleId);}
    
  /** setter for ruleId - sets The unique name/id of the matched rule 
   * @generated
   * @param v value to set into the feature 
   */
  public void setRuleId(String v) {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_ruleId == null)
      jcasType.jcas.throwFeatMissing("ruleId", "eu.project.ttc.types.TermOccAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_ruleId, v);}    
   
    
  //*--------------*
  //* Feature: frequency

  /** getter for frequency - gets 
   * @generated
   * @return value of the feature 
   */
  public double getFrequency() {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_frequency == null)
      jcasType.jcas.throwFeatMissing("frequency", "eu.project.ttc.types.TermOccAnnotation");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_frequency);}
    
  /** setter for frequency - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setFrequency(double v) {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_frequency == null)
      jcasType.jcas.throwFeatMissing("frequency", "eu.project.ttc.types.TermOccAnnotation");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_frequency, v);}    
   
    
  //*--------------*
  //* Feature: specificity

  /** getter for specificity - gets 
   * @generated
   * @return value of the feature 
   */
  public double getSpecificity() {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_specificity == null)
      jcasType.jcas.throwFeatMissing("specificity", "eu.project.ttc.types.TermOccAnnotation");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_specificity);}
    
  /** setter for specificity - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSpecificity(double v) {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_specificity == null)
      jcasType.jcas.throwFeatMissing("specificity", "eu.project.ttc.types.TermOccAnnotation");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_specificity, v);}    
   
    
  //*--------------*
  //* Feature: category

  /** getter for category - gets 
   * @generated
   * @return value of the feature 
   */
  public String getCategory() {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_category == null)
      jcasType.jcas.throwFeatMissing("category", "eu.project.ttc.types.TermOccAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_category);}
    
  /** setter for category - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setCategory(String v) {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_category == null)
      jcasType.jcas.throwFeatMissing("category", "eu.project.ttc.types.TermOccAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_category, v);}    
   
    
  //*--------------*
  //* Feature: lemma

  /** getter for lemma - gets 
   * @generated
   * @return value of the feature 
   */
  public String getLemma() {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_lemma == null)
      jcasType.jcas.throwFeatMissing("lemma", "eu.project.ttc.types.TermOccAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_lemma);}
    
  /** setter for lemma - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLemma(String v) {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_lemma == null)
      jcasType.jcas.throwFeatMissing("lemma", "eu.project.ttc.types.TermOccAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_lemma, v);}    
   
    
  //*--------------*
  //* Feature: langset

  /** getter for langset - gets Langset id after the TermOccAnnotation has been saved.
   * @generated
   * @return value of the feature 
   */
  public String getLangset() {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_langset == null)
      jcasType.jcas.throwFeatMissing("langset", "eu.project.ttc.types.TermOccAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_langset);}
    
  /** setter for langset - sets Langset id after the TermOccAnnotation has been saved. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setLangset(String v) {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_langset == null)
      jcasType.jcas.throwFeatMissing("langset", "eu.project.ttc.types.TermOccAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_langset, v);}    
  }

    