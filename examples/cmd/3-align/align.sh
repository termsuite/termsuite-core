#!/bin/sh
#
#
#
#

java -Xms1g -Xmx8g -cp termsuite-core-3.0.jar \
      fr.univnantes.termsuite.tools.AlignerCLI \
      --source-termino mytermino-fr.json \
      --target-termino mytermino-en.json  \
      --dictionary ~/NLP/Ressources/dictionaries-not-free/FR-EN.txt \
       --term "énergie éolienne" \
       -n 5 \
       --info
