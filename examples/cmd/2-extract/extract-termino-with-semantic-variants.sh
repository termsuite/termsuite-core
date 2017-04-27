#!/bin/sh
#
# Extracts a terminology from a textual corpus with semantic variants (synonyms),
# keeps top 100 terms by frequency, and export terminology to TSV with
# customized TSV columns.
#
# List of available properties and documentation for tsv output:
#    http://termsuite.github.io/documentation/command-line-api/#TerminologyExtractorCLI-tsv-properties

java -Xms1g -Xmx8g -cp termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --enable-semantic-gathering \
            --tsv $TSV_OUTPUT_FILE \
            --tsv-properties "pilot,f,sp,semScore,isDico,distrib" \
            --json "mytermino.json" \
            --info
