#!/bin/sh
# ---
# title: Preprocessing a `*.txt` corpus and exporting it to a `*.xmi` preprocessed corpus
# excerpt: >
#  This example shows how to execute the preprocessing pipeline on a textual
#  corpus and export the preprocessed documents to their `*.xmi` format for
#  later reuse (e.g. by
#  [TerminoExtractorCLI](http://termsuite.github.io/documentation/terminology-extractor-cli/)).
# ---
#

java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.PreprocessorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --xmi-anno $PREPARED_CORPUS_PATH_XMI \
            --info
