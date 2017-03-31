#!/bin/sh
#
# Extracts a terminology from a textual corpus, keeps top 100 terms by frequency,
# keeps term variants of these top 100 terms, and export terminology to
# TSV with customized TSV columns.
#
# List of all available options for TerminologyExtractorCLI:
#    http://termsuite.github.io/documentation/command-line-api/


java -Xms1g -Xmx8g -cp termsuite-core-3.0.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t /home/me/apps/TreeTagger \
            -c /home/me/corpora/mycorpus/English/txt \
            -l en \
            --no-occurrence \
            --enable-semantic-gathering \
            --capped-size 500000 \
            --tsv "mytermino.tsv" \
            --tsv-properties "p,pilot,sp,f,ind" \
            --info
