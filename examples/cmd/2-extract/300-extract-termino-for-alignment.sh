#!/bin/sh
# ---
# title: Extract a terminology ready for alignment
# excerpt: >
#  To extract a terminology that is reusable by TermSuite's aligner, you only
#  need to set the `--contextualize` option and export terminology to its native
#  format (JSON) with option `--json`.
# ---
#

java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --contextualize \
            --json $JSON_OUTPUT_FILE \
            --info
