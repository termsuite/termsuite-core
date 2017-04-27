#!/bin/sh
#
# Extracts a terminology from a textual corpus, keeps top 100 terms by frequency,
# keeps term variants of these top 100 terms, and export terminology to
# TSV with customized TSV columns.
#
# List of all available options for TerminologyExtractorCLI:
#    http://termsuite.github.io/documentation/command-line-api/


java -Xms1g -Xmx8g -cp termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --no-occurrence \
            --enable-semantic-gathering \
            --capped-size 500000 \
            --tsv $TSV_OUTPUT_FILE \
            --tsv-properties "p,pilot,sp,f,ind" \
            --info
