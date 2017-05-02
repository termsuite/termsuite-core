#!/bin/sh
#
# ---
# title: Translating french terms to english (and display top 5 candidates)
# excerpt: >
#  You can translate domain-specific terms, i.e. terms that cannot be found in
#  any general language bilingual dictionary, from one source language to another
#  target language, based on:
#   * a contextualized terminology extracted from a comparable multilingual corpus for the **source** language (`--source-termino`)
#   * a contextualized terminology extracted from **the same** comparable multilingual corpus for the **target** language (`--target-termino`)
#   * a bilingual `source language`-to-`target-language` dictionary (`--dictionary`)
#   * the source term to translate (`--source-term`) within double quotes `"` when multi-worded.
#  <br>
#  See how to produce an alignment-ready (i.e. contextualized) terminology with TermSuite
#  with [TerminologyExtractorCLI](http://termsuite.github.io/documentation/terminology-extractor-cli/).
# ---
#

echo $SOURCE_TERMINO_JSON
echo $TARGET_TERMINO_JSON
echo $BILINGUAL_DICO_PATH
echo $SOURCE_TERM

java -Xms1g -Xmx8g -cp $TS_HOME/termsuite-core-$TS_VERSION.jar \
      fr.univnantes.termsuite.tools.AlignerCLI \
      --source-termino $SOURCE_TERMINO_JSON \
      --target-termino $TARGET_TERMINO_JSON \
      --dictionary $BILINGUAL_DICO_PATH \
       --term $SOURCE_TERM \
       -n 5 \
       --info
