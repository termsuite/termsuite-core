#!/bin/sh
# ---
# title: Preprocessing a ``*.txt` corpus and exporting it to a `*.json` preprocessed corpus
# excerpt: >
#  This example shows how to execute the preprocessing pipeline on a textual
#  corpus and export the preprocessed documents to their `*.json` format for
#  later reuse (e.g. by
#  [TerminoExtractorCLI](http://termsuite.github.io/documentation/terminology-extractor-cli/)).
#  <br>
#  **Note:** The prepared corpus in `*.json` format (i.e. one output directory containing
#  one `file-i.json` annotation file per input `file-i.txt` input text document)
#  is different from the prepared corpus in imported terminology `imported-termino.json`
#  format (one signle `json` file for the whole input document).
# ---
#

java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.PreprocessorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --json-anno $PREPARED_CORPUS_PATH_JSON \
            --info
