#!/bin/sh
# ---
# title: "Filtering: keeping term variants"
# excerpt: >
#  Filtering terminology will often result in having few term variants detected
#  in output terminology, because most term variants have low frequencies. You
#  can configure TermSuite to keep all variants of a term even if they are
#  to be filtered out by the filter selector.
#  <br>
#  Any numeric property can be used when for post-propcessing filtering. See all [TermSuite properties](http://termsuite.github.io/documentation/properties/).
#  <br>
#  This launcher script keeps top 500 term by specificity, plus all 1-degree
#  variants of these 500 terms.
# ---
#

java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --post-filter-property freq \
            --post-filter-top-n 500 \
            --post-filter-keep-variants \
            --tsv $TSV_OUTPUT_FILE \
            --tsv-properties "pattern,pilot,spec,freq,dFreq" \
            --info
