#!/bin/sh
# ---
# title: "Filtering: cleaning by threshold value on any property"
# excerpt: >
#  Terminology can be filtered on any property after variant gathering with
#  `--post-filter-property` option. When given together with option `--post-filter-th`,
#  TermSuite will filter by threshold on given property.
#  >
#  Any numeric property can be used when for post-propcessing filtering. See all [TermSuite properties](http://termsuite.github.io/documentation/properties/).
#  >
#  For example, this launcher filters all term having `dFreq < 3`, i.e. appearing in
#  less than 3 different document in the source corpus..
# ---
#

java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --post-filter-property dFreq \
            --post-filter-th 3 \
            --tsv $TSV_OUTPUT_FILE \
            --tsv-properties "pattern,pilot,spec,freq,dFreq" \
            --info
