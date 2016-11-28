

/* First created by JCasGen Mon Nov 28 18:03:45 CET 2016 */
package fr.univnantes.termsuite.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Nov 28 18:03:45 CET 2016
 * XML source: /home/cram-d/git/termsuite-core/src/main/resources/TermSuite_TS.xml
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
      jcasType.jcas.throwFeatMissing("words", "fr.univnantes.termsuite.types.TermOccAnnotation");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_words)));}
    
  /** setter for words - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setWords(FSArray v) {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_words == null)
      jcasType.jcas.throwFeatMissing("words", "fr.univnantes.termsuite.types.TermOccAnnotation");
    jcasType.ll_cas.ll_setRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_words, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for words - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public WordAnnotation getWords(int i) {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_words == null)
      jcasType.jcas.throwFeatMissing("words", "fr.univnantes.termsuite.types.TermOccAnnotation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_words), i);
    return (WordAnnotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_words), i)));}

  /** indexed setter for words - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setWords(int i, WordAnnotation v) { 
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_words == null)
      jcasType.jcas.throwFeatMissing("words", "fr.univnantes.termsuite.types.TermOccAnnotation");
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
      jcasType.jcas.throwFeatMissing("pattern", "fr.univnantes.termsuite.types.TermOccAnnotation");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_pattern)));}
    
  /** setter for pattern - sets The list of regex matcher labels (e.g. [N, A, A, C, A] ) 
   * @generated
   * @param v value to set into the feature 
   */
  public void setPattern(StringArray v) {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_pattern == null)
      jcasType.jcas.throwFeatMissing("pattern", "fr.univnantes.termsuite.types.TermOccAnnotation");
    jcasType.ll_cas.ll_setRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_pattern, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for pattern - gets an indexed value - The list of regex matcher labels (e.g. [N, A, A, C, A] )
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getPattern(int i) {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_pattern == null)
      jcasType.jcas.throwFeatMissing("pattern", "fr.univnantes.termsuite.types.TermOccAnnotation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_pattern), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_pattern), i);}

  /** indexed setter for pattern - sets an indexed value - The list of regex matcher labels (e.g. [N, A, A, C, A] )
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setPattern(int i, String v) { 
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_pattern == null)
      jcasType.jcas.throwFeatMissing("pattern", "fr.univnantes.termsuite.types.TermOccAnnotation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_pattern), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_pattern), i, v);}
   
    
  //*--------------*
  //* Feature: spottingRuleName

  /** getter for spottingRuleName - gets The unique name of the spotting rule matched
   * @generated
   * @return value of the feature 
   */
  public String getSpottingRuleName() {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_spottingRuleName == null)
      jcasType.jcas.throwFeatMissing("spottingRuleName", "fr.univnantes.termsuite.types.TermOccAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_spottingRuleName);}
    
  /** setter for spottingRuleName - sets The unique name of the spotting rule matched 
   * @generated
   * @param v value to set into the feature 
   */
  public void setSpottingRuleName(String v) {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_spottingRuleName == null)
      jcasType.jcas.throwFeatMissing("spottingRuleName", "fr.univnantes.termsuite.types.TermOccAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_spottingRuleName, v);}    
   
    
  //*--------------*
  //* Feature: termKey

  /** getter for termKey - gets 
   * @generated
   * @return value of the feature 
   */
  public String getTermKey() {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_termKey == null)
      jcasType.jcas.throwFeatMissing("termKey", "fr.univnantes.termsuite.types.TermOccAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_termKey);}
    
  /** setter for termKey - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTermKey(String v) {
    if (TermOccAnnotation_Type.featOkTst && ((TermOccAnnotation_Type)jcasType).casFeat_termKey == null)
      jcasType.jcas.throwFeatMissing("termKey", "fr.univnantes.termsuite.types.TermOccAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((TermOccAnnotation_Type)jcasType).casFeatCode_termKey, v);}    
  }

    