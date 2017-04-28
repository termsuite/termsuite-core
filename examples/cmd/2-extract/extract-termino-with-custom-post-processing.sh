#!/bin/sh
# ---
# title: Customizing Terminology Post-Processing
# excerpt: >
#  Terminology Post-Processing step (after variant gathering)
#  can be customized with `--postproc-*` options. Option `--disable-post-processing`
#  would disable this step.
#  >
#  This launcher sets a threshold of 0.20 for term property `Independance`.
#  This means that for each term, at least 20% of its occurrences must not be
#  in the form of any of its variant.
#  >
#  See [default TermSuite config](https://github.com/termsuite/termsuite-core/blob/master/src/main/resources/fr/univnantes/termsuite/resources/en/english-extractor-config.json)
#  for the default english post-processing values.
# ---
#

java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --postproc-independance-th 0.20 \
            --tsv $TSV_OUTPUT_FILE \
            --tsv-properties "pilot,freq,spec,ind" \
            --info
