#!/bin/sh
#
#
#
#

java -Xms1g -Xmx8g -cp termsuite-core-$TS_VERSION.jar \
      fr.univnantes.termsuite.tools.AlignerCLI \
      --source-termino mytermino-fr.json \
      --target-termino mytermino-en.json  \
      --dictionary $BILINGUAL_DICO_PATH \
       --term "énergie éolienne" \
       -n 5 \
       --info
