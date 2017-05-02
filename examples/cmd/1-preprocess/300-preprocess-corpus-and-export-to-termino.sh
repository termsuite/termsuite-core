#!/bin/sh
# ---
# title: Preprocessing a ``*.txt` corpus and exporting it as a `json` terminology
# excerpt: >
#  This example shows how to execute the preprocessing pipeline on a textual
#  corpus and export the preprocessed corpus as a terminology in its native format (one single output `.json`) for
#  later reuse (e.g. by
#  [TerminoExtractorCLI](http://termsuite.github.io/documentation/terminology-extractor-cli/)).
#  >
#  **Note:** The prepared corpus in `*.json` format (i.e. one output directory containing
#  one `file-i.json` annotation file per input `file-i.txt` input text document)
#  is different from the prepared corpus in imported terminology `terminology.json`
#  format (one signle `json` file for the whole input document).
# ---
#

java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.PreprocessorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --json $PREPARED_CORPUS_AS_TERMINOLOGY_PATH \
            --info
