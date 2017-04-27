#!/bin/sh
#
#
# ---
# title: Semantic variant detection without idstributional variants (dico only)
# excerpt: >
#  Semantic variants found distributionally (with property `isDistrib=true`) generally are of worse
#  quality and slower to find compared to variants found on dictionary. You can tell TermSuite to deactivate distributional gathering
#  with option `--semantic-dico-only`.
# ---
#
# List of available properties and documentation for tsv output:
#    http://termsuite.github.io/documentation/command-line-api/#TerminologyExtractorCLI-tsv-properties

java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --enable-semantic-gathering \
            --semantic-dico-only \
            --tsv $TSV_OUTPUT_FILE \
            --tsv-properties "pilot,freq,spec,semScore,isDico,isDistrib" \
            --info
