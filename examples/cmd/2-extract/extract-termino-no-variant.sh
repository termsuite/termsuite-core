#!/bin/sh
#
# ---
# title: Deactivating variant gathering
# excerpt: >
#  Variant gathering ca be deactivated with option `--disable-gathering`. this
#  should make the terminology extraction pipeline to terminate significantly more quickly.
# ---
#


java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --disable-gathering \
            --tsv $TSV_OUTPUT_FILE \
            --info
