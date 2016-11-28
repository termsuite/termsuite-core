
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
public class WordAnnotation_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (WordAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = WordAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new WordAnnotation(addr, WordAnnotation_Type.this);
  			   WordAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new WordAnnotation(addr, WordAnnotation_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = WordAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("fr.univnantes.termsuite.types.WordAnnotation");
 
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
      jcas.throwFeatMissing("category", "fr.univnantes.termsuite.types.WordAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_category);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setCategory(int addr, String v) {
        if (featOkTst && casFeat_category == null)
      jcas.throwFeatMissing("category", "fr.univnantes.termsuite.types.WordAnnotation");
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
      jcas.throwFeatMissing("lemma", "fr.univnantes.termsuite.types.WordAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_lemma);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setLemma(int addr, String v) {
        if (featOkTst && casFeat_lemma == null)
      jcas.throwFeatMissing("lemma", "fr.univnantes.termsuite.types.WordAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_lemma, v);}
    
  
 
  /** @generated */
  final Feature casFeat_stem;
  /** @generated */
  final int     casFeatCode_stem;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getStem(int addr) {
        if (featOkTst && casFeat_stem == null)
      jcas.throwFeatMissing("stem", "fr.univnantes.termsuite.types.WordAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_stem);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setStem(int addr, String v) {
        if (featOkTst && casFeat_stem == null)
      jcas.throwFeatMissing("stem", "fr.univnantes.termsuite.types.WordAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_stem, v);}
    
  
 
  /** @generated */
  final Feature casFeat_tag;
  /** @generated */
  final int     casFeatCode_tag;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getTag(int addr) {
        if (featOkTst && casFeat_tag == null)
      jcas.throwFeatMissing("tag", "fr.univnantes.termsuite.types.WordAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_tag);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTag(int addr, String v) {
        if (featOkTst && casFeat_tag == null)
      jcas.throwFeatMissing("tag", "fr.univnantes.termsuite.types.WordAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_tag, v);}
    
  
 
  /** @generated */
  final Feature casFeat_subCategory;
  /** @generated */
  final int     casFeatCode_subCategory;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getSubCategory(int addr) {
        if (featOkTst && casFeat_subCategory == null)
      jcas.throwFeatMissing("subCategory", "fr.univnantes.termsuite.types.WordAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_subCategory);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSubCategory(int addr, String v) {
        if (featOkTst && casFeat_subCategory == null)
      jcas.throwFeatMissing("subCategory", "fr.univnantes.termsuite.types.WordAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_subCategory, v);}
    
  
 
  /** @generated */
  final Feature casFeat_regexLabel;
  /** @generated */
  final int     casFeatCode_regexLabel;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getRegexLabel(int addr) {
        if (featOkTst && casFeat_regexLabel == null)
      jcas.throwFeatMissing("regexLabel", "fr.univnantes.termsuite.types.WordAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_regexLabel);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setRegexLabel(int addr, String v) {
        if (featOkTst && casFeat_regexLabel == null)
      jcas.throwFeatMissing("regexLabel", "fr.univnantes.termsuite.types.WordAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_regexLabel, v);}
    
  
 
  /** @generated */
  final Feature casFeat_number;
  /** @generated */
  final int     casFeatCode_number;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getNumber(int addr) {
        if (featOkTst && casFeat_number == null)
      jcas.throwFeatMissing("number", "fr.univnantes.termsuite.types.WordAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_number);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setNumber(int addr, String v) {
        if (featOkTst && casFeat_number == null)
      jcas.throwFeatMissing("number", "fr.univnantes.termsuite.types.WordAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_number, v);}
    
  
 
  /** @generated */
  final Feature casFeat_gender;
  /** @generated */
  final int     casFeatCode_gender;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getGender(int addr) {
        if (featOkTst && casFeat_gender == null)
      jcas.throwFeatMissing("gender", "fr.univnantes.termsuite.types.WordAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_gender);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setGender(int addr, String v) {
        if (featOkTst && casFeat_gender == null)
      jcas.throwFeatMissing("gender", "fr.univnantes.termsuite.types.WordAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_gender, v);}
    
  
 
  /** @generated */
  final Feature casFeat_case;
  /** @generated */
  final int     casFeatCode_case;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getCase(int addr) {
        if (featOkTst && casFeat_case == null)
      jcas.throwFeatMissing("case", "fr.univnantes.termsuite.types.WordAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_case);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setCase(int addr, String v) {
        if (featOkTst && casFeat_case == null)
      jcas.throwFeatMissing("case", "fr.univnantes.termsuite.types.WordAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_case, v);}
    
  
 
  /** @generated */
  final Feature casFeat_mood;
  /** @generated */
  final int     casFeatCode_mood;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getMood(int addr) {
        if (featOkTst && casFeat_mood == null)
      jcas.throwFeatMissing("mood", "fr.univnantes.termsuite.types.WordAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_mood);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setMood(int addr, String v) {
        if (featOkTst && casFeat_mood == null)
      jcas.throwFeatMissing("mood", "fr.univnantes.termsuite.types.WordAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_mood, v);}
    
  
 
  /** @generated */
  final Feature casFeat_tense;
  /** @generated */
  final int     casFeatCode_tense;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getTense(int addr) {
        if (featOkTst && casFeat_tense == null)
      jcas.throwFeatMissing("tense", "fr.univnantes.termsuite.types.WordAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_tense);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTense(int addr, String v) {
        if (featOkTst && casFeat_tense == null)
      jcas.throwFeatMissing("tense", "fr.univnantes.termsuite.types.WordAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_tense, v);}
    
  
 
  /** @generated */
  final Feature casFeat_person;
  /** @generated */
  final int     casFeatCode_person;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getPerson(int addr) {
        if (featOkTst && casFeat_person == null)
      jcas.throwFeatMissing("person", "fr.univnantes.termsuite.types.WordAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_person);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setPerson(int addr, String v) {
        if (featOkTst && casFeat_person == null)
      jcas.throwFeatMissing("person", "fr.univnantes.termsuite.types.WordAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_person, v);}
    
  
 
  /** @generated */
  final Feature casFeat_possessor;
  /** @generated */
  final int     casFeatCode_possessor;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getPossessor(int addr) {
        if (featOkTst && casFeat_possessor == null)
      jcas.throwFeatMissing("possessor", "fr.univnantes.termsuite.types.WordAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_possessor);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setPossessor(int addr, String v) {
        if (featOkTst && casFeat_possessor == null)
      jcas.throwFeatMissing("possessor", "fr.univnantes.termsuite.types.WordAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_possessor, v);}
    
  
 
  /** @generated */
  final Feature casFeat_degree;
  /** @generated */
  final int     casFeatCode_degree;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getDegree(int addr) {
        if (featOkTst && casFeat_degree == null)
      jcas.throwFeatMissing("degree", "fr.univnantes.termsuite.types.WordAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_degree);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setDegree(int addr, String v) {
        if (featOkTst && casFeat_degree == null)
      jcas.throwFeatMissing("degree", "fr.univnantes.termsuite.types.WordAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_degree, v);}
    
  
 
  /** @generated */
  final Feature casFeat_formation;
  /** @generated */
  final int     casFeatCode_formation;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getFormation(int addr) {
        if (featOkTst && casFeat_formation == null)
      jcas.throwFeatMissing("formation", "fr.univnantes.termsuite.types.WordAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_formation);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setFormation(int addr, String v) {
        if (featOkTst && casFeat_formation == null)
      jcas.throwFeatMissing("formation", "fr.univnantes.termsuite.types.WordAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_formation, v);}
    
  
 
  /** @generated */
  final Feature casFeat_labels;
  /** @generated */
  final int     casFeatCode_labels;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getLabels(int addr) {
        if (featOkTst && casFeat_labels == null)
      jcas.throwFeatMissing("labels", "fr.univnantes.termsuite.types.WordAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_labels);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setLabels(int addr, String v) {
        if (featOkTst && casFeat_labels == null)
      jcas.throwFeatMissing("labels", "fr.univnantes.termsuite.types.WordAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_labels, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public WordAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_category = jcas.getRequiredFeatureDE(casType, "category", "uima.cas.String", featOkTst);
    casFeatCode_category  = (null == casFeat_category) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_category).getCode();

 
    casFeat_lemma = jcas.getRequiredFeatureDE(casType, "lemma", "uima.cas.String", featOkTst);
    casFeatCode_lemma  = (null == casFeat_lemma) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_lemma).getCode();

 
    casFeat_stem = jcas.getRequiredFeatureDE(casType, "stem", "uima.cas.String", featOkTst);
    casFeatCode_stem  = (null == casFeat_stem) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_stem).getCode();

 
    casFeat_tag = jcas.getRequiredFeatureDE(casType, "tag", "uima.cas.String", featOkTst);
    casFeatCode_tag  = (null == casFeat_tag) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_tag).getCode();

 
    casFeat_subCategory = jcas.getRequiredFeatureDE(casType, "subCategory", "uima.cas.String", featOkTst);
    casFeatCode_subCategory  = (null == casFeat_subCategory) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_subCategory).getCode();

 
    casFeat_regexLabel = jcas.getRequiredFeatureDE(casType, "regexLabel", "uima.cas.String", featOkTst);
    casFeatCode_regexLabel  = (null == casFeat_regexLabel) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_regexLabel).getCode();

 
    casFeat_number = jcas.getRequiredFeatureDE(casType, "number", "uima.cas.String", featOkTst);
    casFeatCode_number  = (null == casFeat_number) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_number).getCode();

 
    casFeat_gender = jcas.getRequiredFeatureDE(casType, "gender", "uima.cas.String", featOkTst);
    casFeatCode_gender  = (null == casFeat_gender) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_gender).getCode();

 
    casFeat_case = jcas.getRequiredFeatureDE(casType, "case", "uima.cas.String", featOkTst);
    casFeatCode_case  = (null == casFeat_case) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_case).getCode();

 
    casFeat_mood = jcas.getRequiredFeatureDE(casType, "mood", "uima.cas.String", featOkTst);
    casFeatCode_mood  = (null == casFeat_mood) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_mood).getCode();

 
    casFeat_tense = jcas.getRequiredFeatureDE(casType, "tense", "uima.cas.String", featOkTst);
    casFeatCode_tense  = (null == casFeat_tense) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_tense).getCode();

 
    casFeat_person = jcas.getRequiredFeatureDE(casType, "person", "uima.cas.String", featOkTst);
    casFeatCode_person  = (null == casFeat_person) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_person).getCode();

 
    casFeat_possessor = jcas.getRequiredFeatureDE(casType, "possessor", "uima.cas.String", featOkTst);
    casFeatCode_possessor  = (null == casFeat_possessor) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_possessor).getCode();

 
    casFeat_degree = jcas.getRequiredFeatureDE(casType, "degree", "uima.cas.String", featOkTst);
    casFeatCode_degree  = (null == casFeat_degree) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_degree).getCode();

 
    casFeat_formation = jcas.getRequiredFeatureDE(casType, "formation", "uima.cas.String", featOkTst);
    casFeatCode_formation  = (null == casFeat_formation) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_formation).getCode();

 
    casFeat_labels = jcas.getRequiredFeatureDE(casType, "labels", "uima.cas.String", featOkTst);
    casFeatCode_labels  = (null == casFeat_labels) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_labels).getCode();

  }
}



    