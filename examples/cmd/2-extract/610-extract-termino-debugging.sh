#!/bin/sh
#
# ---
# title: Debugging terminology extraction
# excerpt: >
#  You can activate logging to console with either `--info` or `--debug` (more verbose).
#  <br>
#  There is also the possibility to get term-centered information with option
#  `--watch`. The following launcher will print all pipeline information about term
#  "offshore wind energy", even if it has been filtered. This is a very convenient tool
#  when you do not get what you expected of a term (missing variants, missing term, etc).
# ---
#

java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --debug \
            --tsv $TSV_OUTPUT_FILE \
            --watch "offshore wind energy"
