#!/bin/sh
#
# Extracts a terminology from a textual corpus without term variations, keeps
# top 500 terms by specificity, and export terminology to TSV.
#

java -Xms1g -Xmx8g -cp termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --disable-gathering \
            --post-filter-property "sp" \
            --post-filter-top-n 100 \
            --tsv $TSV_OUTPUT_FILE \
            --info
