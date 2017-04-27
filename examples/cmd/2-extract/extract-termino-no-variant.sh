#!/bin/sh
#
# ---
# title: Deactivating variant gathering
# excerpt: >
#  Variant gathering ca be deactivated with option `--disable-gathering`. this
#  should make the terminology extraction pipeline to terminate significantly more quickly.
# ---
#
# List of all available options for TerminologyExtractorCLI:
#    http://termsuite.github.io/documentation/command-line-api/


java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --disable-gathering \
            --post-filter-property "spec" \
            --post-filter-top-n 100 \
            --tsv $TSV_OUTPUT_FILE \
            --info
