#!/bin/sh
#
# Extracts a terminology from a textual corpus without term variations, keeps
# top 500 terms by specificity, and export terminology to TSV.
#

java -Xms1g -Xmx8g -cp termsuite-core-3.0.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t /home/me/apps/TreeTagger \
            -c /home/me/corpora/mycorpus/English/txt \
            -l en \
            --disable-gathering \
            --post-filter-property "sp" \
            --post-filter-top-n 100 \
            --tsv "mytermino.tsv" \
            --info
