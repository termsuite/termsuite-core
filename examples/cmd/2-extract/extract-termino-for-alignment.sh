#!/bin/sh
#
# Extracts a terminology that is reusable by TermSuite's aligner, i.e.
# with  from a textual corpus .
#

java -Xms1g -Xmx8g -cp termsuite-core-3.0.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t /home/me/apps/TreeTagger \
            -c /home/me/corpora/mycorpus/English/txt \
            -l en \
            --contextualize \
            --json "mytermino-en.json" \
            --info
