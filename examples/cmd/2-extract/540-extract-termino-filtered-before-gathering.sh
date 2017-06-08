#!/bin/sh
# ---
# title: "Filtering: cleaning terminology before gathering step (`--pre-filter-*`)"
# excerpt: >
#  Filtering terminology after the variant gathering step (`--post-filter-*`) is
#  often the default requirement for users. Sometimes, it is clever to filter the terminology
#  before the variant gathering with `--pre-filter-property` option. One of the
#  main use case for pre-filtering is to reduce the terminology size in amount of
#  term variant gathering, thus accelerating the whole variant gathering step.
#  <br>
#  Any numeric property can be used when for pre-propcessing filtering. See all [TermSuite properties](http://termsuite.github.io/documentation/properties/).
#  <br>
#  For example, this launcher filters all term having `freq < 2` (hapaxes)
#  before variant gathering. As a consequence, that will never be possible to
#  detect term variants occurring exactly once. There can not be any
#  `*-keep-variants` option with `--pre-filter` as there is `--post-filter-keep-variants`.
# ---
#


java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --pre-filter-property freq \
            --pre-filter-th 2 \
            --tsv $TSV_OUTPUT_FILE \
            --tsv-properties "pattern,pilot,spec,freq,dFreq" \
            --info
