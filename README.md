# Welcome to TermSuite

TermSuite is a Java UIMA-based toolbox for multilingual and efficient terminology extraction and multilingual term alignment.

# Features

* Word tokenization,
* POS Tagging (3rd party: with TreeTagger or Mate),
* Lemmatization (3rd party: with TreeTagger or Mate),
* Stemming (Snowball),
* Terminology extraction,
* Efficient multiword term detection,
* Term syntactic variants detection,
* Term graphic variants detection,
* Term semantic variants detection (to come),
* Term morphology extraction,
* Term morphosyntactic variants detection,
* Term specificity (Weirdness Ratio) computing and other term measures: WR log, Z-score, term frequency, etc,
* Term alignment (distributional and compositional, multilingual and monolingual),
* Terminology export in multiple formats: `json`, `tsv`, `tbx`.

# Supported languages and TermSuite resources

You need a TermSuite resource pack (a jar file or a directory containing resource files) to be able to use TermSuite.

| Language | Quality of resource pack (and other comments)|
|----------|-------------|
| french |  Excellent |
| english |  Excellent |
| russian |  Excellent (TreeTagger is slow) |
| german |  Good |
| spanish |  Good |
| danish |  Poor |
| chinese |  Poor |
| latvian |  Good, but no POS tagger supported natively |

See [here](https://github.com/termsuite/termsuite-resources) for details and instructions about using resource pack in TermSuite.

# Requirements

1. Install [Java](https://www.java.com/fr/download/) (at least 1.7)
2. Install a POS Tagger and prepare it for TermSuite. (See [this guide](InstallingPOSTagger))
3. Prepare your document corpus into a valid [TermSuite collection](TermSuiteCollection).

*  (> 1.7),
* A POS Tagger ([TreeTagger](http://www.cis.uni-muenchen.de/~schmid/tools/TreeTagger/) advised, but [Mate](https://code.google.com/p/mate-tools/) is supported too),
* A TermSuite resource pack.


# Getting Started

There are three ways to get started easily with TermSuite : Git, Maven and Jar.

**Option 1 - From source code (Git + Gradle)**

Best solution if you need to run last up-to-date version of TermSuite. You first need to install Gradle. See this [installation guide](https://docs.gradle.org/current/userguide/installation.html).

```
$ git clone git@github.com:termsuite/termsuite-core.git
$ cd termsuite-core
$ gradle
```

The jar will then be found under `./build/libs`.

**Option 2 - From jar**

# Developing against TermSuite API

**Maven**

TermSuite can be referenced as a maven artifact: `fr.univnantes.lina:termsuite-core:1.0`.

**Jar**


# TermSuite Java API

The main entry point to TermSuite is the class `TermSuitePipeline`.

```java
// Configure and run the pipeline
TermSuitePipeline pipeline = TermSuitePipeline.create("en")
  .setResourcePath("/path/to/resource/pack")
  .setCollection(
      TermSuiteCollection.TXT,
      "/path/to/corpus",
      "UTF-8")
  .aeWordTokenizer()
  .setTreeTaggerHome("/path/to/treetagger")
  .aeTreeTagger()
  .aeUrlFilter()
  .aeStemmer()
  .aeRegexSpotter()
  .aeSpecificityComputer()
  .aeCompostSplitter() // morphological analysis
  .aeSyntacticVariantGatherer()
  .aeGraphicalVariantGatherer()
  .aeTermClassifier(TermProperty.FREQUENCY)
  .setTsvExportProperties(
      TermProperty.PILOT,
      TermProperty.FREQUENCY,
      TermProperty.WR
    )
  .haeTsvExporter("terminology.tsv")
  .haeJsonExporter("terminology.json")
  .run();

// Retrieve the terminology (i.e. the term index)
TermIndex termIndex = pipeline.getTermIndex();

//Now you can interact with terms and TermSuite's data model
Term t = termIndex.getTermByGroupingKey("n: windmill");
System.out.format("Frequency of %s: \n", t.getFrequency());
System.out.format("Specificity of %s: \n", t.getWR());
for(SyntacticVariation v:t.getSyntacticVariants())
  System.out.format("%s is a variant of %s (rule: %s)\n",
      v.getTarget().getLemma(),
      t.getLemma(),
      v.getVariationRule()
  );
```

See the full [Java API documentation](path to documentation).

# TermSuite Command Line

TermSuite also comes with a command line interface.

```

```
See the full [command line API documentation]().


# Documentation

See the [full documentation](path to website) of TermSuite for advanced uses.
