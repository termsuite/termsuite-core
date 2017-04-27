#!/bin/sh
#
# ---
# title: Exporting terminology to TSV
# excerpt: >
#  Extracts a terminology from a textual corpus, keeps top 100 terms by frequency,
#  keeps term variants of these top 100 terms, and export terminology to
#  TSV with customized TSV columns.
# ---
#
# List of all available options for TerminologyExtractorCLI:
#    http://termsuite.github.io/documentation/command-line-api/

java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --post-filter-property "freq" \
            --post-filter-top-n 100 \
            --post-filter-keep-variants \
            --tsv $TSV_OUTPUT_FILE \
            --tsv-properties "pattern,pilot,spec,freq,ind" \
            --info
