#!/bin/sh
# ---
# title: "Filtering: keeping only top n terms"
# excerpt: >
#  Terminology can be filtered on any property after variant gathering with
#  `--post-filter-property` option. When given together with option `--post-filter-top-n`,
#  TermSuite will only keep top n values sorted by given property (desc).
#  >
#  Any numeric property can be used when for post-propcessing filtering. See all [TermSuite properties](http://termsuite.github.io/documentation/properties/).
#  >
#  For example, this launcher keeps only top 500 most specific terms.
# ---
#

java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --post-filter-property spec \
            --post-filter-top-n 500 \
            --tsv $TSV_OUTPUT_FILE \
            --tsv-properties "pattern,pilot,spec,freq,dFreq" \
            --info
