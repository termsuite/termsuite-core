#!/bin/sh
#
#
# ---
# title: Semantic variant detection with custom dictionary
# excerpt: >
#  Extracting semantic variants requires a synonyms dictionary. Due to licensing
#  issues, the one packaged with TermSuite can be very poor for some languages. You
#  might prefer to use your own dictionary with option `--synonyms-dico`. See TermSuite
#  packaged [english dictionary](https://github.com/termsuite/termsuite-core/raw/master/src/main/resources/fr/univnantes/termsuite/resources/en/english-synonyms.txt)
#  for an example of dictionary format.
# ---
#

java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --enable-semantic-gathering \
            --synonyms-dico $SYNONYMS_DICO \
            --tsv $TSV_OUTPUT_FILE \
            --tsv-properties "pilot,freq,spec,semScore,isDico,isDistrib" \
            --info
