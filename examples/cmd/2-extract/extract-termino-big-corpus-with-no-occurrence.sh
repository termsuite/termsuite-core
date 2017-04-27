#!/bin/sh
#
# ---
# title: Extracting a terminology from a very large corpus
# excerpt: >
#  Extracts a terminology from a very large corpus by deactivating
#  occurrence indexing and setting a capped terminology size.
#  The capped size is the maximum number of terms allowed to be kept in memory.
#  Every time this number goes over the capped size, TermSuite filters the
#  on-going termino by frequency.
# ---
#
# List of all available options for TerminologyExtractorCLI:
#    http://termsuite.github.io/documentation/command-line-api/


java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --no-occurrence \
            --capped-size 500000 \
            --tsv $TSV_OUTPUT_FILE \
            --info
