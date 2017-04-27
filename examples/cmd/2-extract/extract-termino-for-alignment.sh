#!/bin/sh
#
# Extracts a terminology that is reusable by TermSuite's aligner, i.e.
# with  from a textual corpus .
#

java -Xms1g -Xmx8g -cp termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --contextualize \
            --json "mytermino-en.json" \
            --info
