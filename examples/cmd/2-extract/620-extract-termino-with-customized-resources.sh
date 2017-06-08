#!/bin/sh
#
# ---
# title: Using customized linguistic resources (Advanced users)
# excerpt: >
#  TermSuite terminology extraction is based on multiple linguistic resources
#  of many types and formats (multi-word term spotting patterns, variation gathering rules,
#  term frequencies in general language, morphology rules, etc). The main use case
#  for this option is to elaborate linguistic ressources for a new language. Use customized
#  linguistic resources with options `--resource-dir` or `--resource-jar`.
#  <br>
#  See [TermSuite resources](http://termsuite.github.io/documentation/resources/) documentation
#  and see how to easily produce your own custom resource directory or jar.
# ---


java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar fr.univnantes.termsuite.tools.TerminologyExtractorCLI \
            -t $TREETAGGER_HOME \
            -c $CORPUS_PATH \
            -l en \
            --resource-dir $CUSTOM_RESOURCE_DIR \
            --tsv $TSV_OUTPUT_FILE \
            --info
