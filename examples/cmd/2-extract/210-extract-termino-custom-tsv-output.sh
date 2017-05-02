#!/bin/sh
#
# ---
# title: Exporting terminology to TSV
# excerpt: >
#  Export extracted terminology to TSV simply by setting option `--tsv`.
#  >
#  You can select term and variant properties to appear as column
#  in TSV with option `--tsv-properties`. Given value must be a `,`-separated list of
#  valid property names. See all [available properties](http://termsuite.github.io/documentation/properties/).
# ---
#

java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --tsv $TSV_OUTPUT_FILE \
            --tsv-properties "key,pattern,pilot,spec,freq,ind" \
            --info
