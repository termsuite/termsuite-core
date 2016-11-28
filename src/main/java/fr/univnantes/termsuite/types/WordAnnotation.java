

/* First created by JCasGen Mon Nov 28 18:03:45 CET 2016 */
package fr.univnantes.termsuite.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Nov 28 18:03:45 CET 2016
 * XML source: /home/cram-d/git/termsuite-core/src/main/resources/TermSuite_TS.xml
 * @generated */
public class WordAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(WordAnnotation.class);
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
  protected WordAnnotation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public WordAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public WordAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public WordAnnotation(JCas jcas, int begin, int end) {
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
  //* Feature: category

  /** getter for category - gets 
   * @generated
   * @return value of the feature 
   */
  public String getCategory() {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_category == null)
      jcasType.jcas.throwFeatMissing("category", "fr.univnantes.termsuite.types.WordAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_category);}
    
  /** setter for category - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setCategory(String v) {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_category == null)
      jcasType.jcas.throwFeatMissing("category", "fr.univnantes.termsuite.types.WordAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_category, v);}    
   
    
  //*--------------*
  //* Feature: lemma

  /** getter for lemma - gets 
   * @generated
   * @return value of the feature 
   */
  public String getLemma() {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_lemma == null)
      jcasType.jcas.throwFeatMissing("lemma", "fr.univnantes.termsuite.types.WordAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_lemma);}
    
  /** setter for lemma - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLemma(String v) {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_lemma == null)
      jcasType.jcas.throwFeatMissing("lemma", "fr.univnantes.termsuite.types.WordAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_lemma, v);}    
   
    
  //*--------------*
  //* Feature: stem

  /** getter for stem - gets 
   * @generated
   * @return value of the feature 
   */
  public String getStem() {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_stem == null)
      jcasType.jcas.throwFeatMissing("stem", "fr.univnantes.termsuite.types.WordAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_stem);}
    
  /** setter for stem - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setStem(String v) {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_stem == null)
      jcasType.jcas.throwFeatMissing("stem", "fr.univnantes.termsuite.types.WordAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_stem, v);}    
   
    
  //*--------------*
  //* Feature: tag

  /** getter for tag - gets 
   * @generated
   * @return value of the feature 
   */
  public String getTag() {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_tag == null)
      jcasType.jcas.throwFeatMissing("tag", "fr.univnantes.termsuite.types.WordAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_tag);}
    
  /** setter for tag - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTag(String v) {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_tag == null)
      jcasType.jcas.throwFeatMissing("tag", "fr.univnantes.termsuite.types.WordAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_tag, v);}    
   
    
  //*--------------*
  //* Feature: subCategory

  /** getter for subCategory - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSubCategory() {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_subCategory == null)
      jcasType.jcas.throwFeatMissing("subCategory", "fr.univnantes.termsuite.types.WordAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_subCategory);}
    
  /** setter for subCategory - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSubCategory(String v) {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_subCategory == null)
      jcasType.jcas.throwFeatMissing("subCategory", "fr.univnantes.termsuite.types.WordAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_subCategory, v);}    
   
    
  //*--------------*
  //* Feature: regexLabel

  /** getter for regexLabel - gets 
   * @generated
   * @return value of the feature 
   */
  public String getRegexLabel() {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_regexLabel == null)
      jcasType.jcas.throwFeatMissing("regexLabel", "fr.univnantes.termsuite.types.WordAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_regexLabel);}
    
  /** setter for regexLabel - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setRegexLabel(String v) {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_regexLabel == null)
      jcasType.jcas.throwFeatMissing("regexLabel", "fr.univnantes.termsuite.types.WordAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_regexLabel, v);}    
   
    
  //*--------------*
  //* Feature: number

  /** getter for number - gets 
   * @generated
   * @return value of the feature 
   */
  public String getNumber() {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_number == null)
      jcasType.jcas.throwFeatMissing("number", "fr.univnantes.termsuite.types.WordAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_number);}
    
  /** setter for number - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setNumber(String v) {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_number == null)
      jcasType.jcas.throwFeatMissing("number", "fr.univnantes.termsuite.types.WordAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_number, v);}    
   
    
  //*--------------*
  //* Feature: gender

  /** getter for gender - gets 
   * @generated
   * @return value of the feature 
   */
  public String getGender() {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_gender == null)
      jcasType.jcas.throwFeatMissing("gender", "fr.univnantes.termsuite.types.WordAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_gender);}
    
  /** setter for gender - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setGender(String v) {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_gender == null)
      jcasType.jcas.throwFeatMissing("gender", "fr.univnantes.termsuite.types.WordAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_gender, v);}    
   
    
  //*--------------*
  //* Feature: case

  /** getter for case - gets 
   * @generated
   * @return value of the feature 
   */
  public String getCase() {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_case == null)
      jcasType.jcas.throwFeatMissing("case", "fr.univnantes.termsuite.types.WordAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_case);}
    
  /** setter for case - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setCase(String v) {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_case == null)
      jcasType.jcas.throwFeatMissing("case", "fr.univnantes.termsuite.types.WordAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_case, v);}    
   
    
  //*--------------*
  //* Feature: mood

  /** getter for mood - gets 
   * @generated
   * @return value of the feature 
   */
  public String getMood() {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_mood == null)
      jcasType.jcas.throwFeatMissing("mood", "fr.univnantes.termsuite.types.WordAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_mood);}
    
  /** setter for mood - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMood(String v) {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_mood == null)
      jcasType.jcas.throwFeatMissing("mood", "fr.univnantes.termsuite.types.WordAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_mood, v);}    
   
    
  //*--------------*
  //* Feature: tense

  /** getter for tense - gets 
   * @generated
   * @return value of the feature 
   */
  public String getTense() {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_tense == null)
      jcasType.jcas.throwFeatMissing("tense", "fr.univnantes.termsuite.types.WordAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_tense);}
    
  /** setter for tense - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTense(String v) {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_tense == null)
      jcasType.jcas.throwFeatMissing("tense", "fr.univnantes.termsuite.types.WordAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_tense, v);}    
   
    
  //*--------------*
  //* Feature: person

  /** getter for person - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPerson() {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_person == null)
      jcasType.jcas.throwFeatMissing("person", "fr.univnantes.termsuite.types.WordAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_person);}
    
  /** setter for person - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPerson(String v) {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_person == null)
      jcasType.jcas.throwFeatMissing("person", "fr.univnantes.termsuite.types.WordAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_person, v);}    
   
    
  //*--------------*
  //* Feature: possessor

  /** getter for possessor - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPossessor() {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_possessor == null)
      jcasType.jcas.throwFeatMissing("possessor", "fr.univnantes.termsuite.types.WordAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_possessor);}
    
  /** setter for possessor - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPossessor(String v) {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_possessor == null)
      jcasType.jcas.throwFeatMissing("possessor", "fr.univnantes.termsuite.types.WordAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_possessor, v);}    
   
    
  //*--------------*
  //* Feature: degree

  /** getter for degree - gets 
   * @generated
   * @return value of the feature 
   */
  public String getDegree() {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_degree == null)
      jcasType.jcas.throwFeatMissing("degree", "fr.univnantes.termsuite.types.WordAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_degree);}
    
  /** setter for degree - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDegree(String v) {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_degree == null)
      jcasType.jcas.throwFeatMissing("degree", "fr.univnantes.termsuite.types.WordAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_degree, v);}    
   
    
  //*--------------*
  //* Feature: formation

  /** getter for formation - gets 
   * @generated
   * @return value of the feature 
   */
  public String getFormation() {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_formation == null)
      jcasType.jcas.throwFeatMissing("formation", "fr.univnantes.termsuite.types.WordAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_formation);}
    
  /** setter for formation - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setFormation(String v) {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_formation == null)
      jcasType.jcas.throwFeatMissing("formation", "fr.univnantes.termsuite.types.WordAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_formation, v);}    
   
    
  //*--------------*
  //* Feature: labels

  /** getter for labels - gets 
   * @generated
   * @return value of the feature 
   */
  public String getLabels() {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_labels == null)
      jcasType.jcas.throwFeatMissing("labels", "fr.univnantes.termsuite.types.WordAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_labels);}
    
  /** setter for labels - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLabels(String v) {
    if (WordAnnotation_Type.featOkTst && ((WordAnnotation_Type)jcasType).casFeat_labels == null)
      jcasType.jcas.throwFeatMissing("labels", "fr.univnantes.termsuite.types.WordAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((WordAnnotation_Type)jcasType).casFeatCode_labels, v);}    
  }

    