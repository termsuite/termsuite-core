#!/bin/sh
#
# ---
# title: Extracting a terminology from preprocessed corpus
# excerpt: >
#  It is not mandatory to start a terminology extraction pipeline on textual
#  corpus (with option `-c`). You may want to do your preprocessings once and
#  start a extraction pipelines from an already preprocessed corpus with option
#  `--from-prepared-corpus` instead.
#  <br>
#  Note that since the corpus is prepared (including POS tags), there is no more
#  need for setting options `-t`. TermSuite will complain if both options are
#  set together.
#  <br>
#  A valid preprocessed corpus is a directory path containing as many `*.xmi`
#  files as there are `*.txt` file in input corpus. The `file-i.xmi` file being the
#  list of preprocessed annotation of file `file-i.txt`.
#  <br>
#  Another valid preprocessed corpus type is a TermSuite `.json` terminology
#  in which all spotted terms have been imported but on which no further terminology
#  extraction processings have been operated.
#  <br>
#  See [TermSuite `Preprocessor`](http://termsuite.github.io/documentation/preprocessor-cli/)
#  on how to produce a valid preprocessed corpus in any of these format.
# ---
#

java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            --from-prepared-corpus $PREPARED_CORPUS_PATH \
            -l en \
            --tsv $TSV_OUTPUT_FILE \
            --info
