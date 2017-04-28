#!/bin/sh
#
# ---
# title: Activating semantic variant detection
# excerpt: >
#  You can activate semantic variant detection with option `--enable-semantic-gathering`.
#  Semantic variant gathering can be significantly slower. In TSV output,
#  semantic variants are flagged as `V[h]`. TSV properties `isDico` and `isDistrib`
#  tell if the variant has been detected with a synonymic dictionary or with distributional alignments.
# ---
#
#

java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --enable-semantic-gathering \
            --tsv $TSV_OUTPUT_FILE \
            --tsv-properties "pilot,freq,spec,semScore,isDico,isDistrib" \
            --info
