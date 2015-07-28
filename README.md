# Welcome to TermSuite

TermSuite is a Java UIMA-based toolbox for multilingual and efficient terminology extraction an multilingual term alignment.

# Features

* Terminology extraction
* Efficient multiword term detection
* Term syntactic variants detection
* Term graphic variants detection
* Term semantic variants detection (to come)
* Term morphology extraction
* Term morphosyntactic variants detection
* Term multilingual alignment

# Supported languages

You need a TermSuite resource pack (a jar file) to be able to use TermSuite.

| Language | Qualitity of resource pack (and other comments)|
|----------|:-------------:|
| french |  Excellent |
| english |  Excellent |
| russian |  Excellent (TreeTagger is slow) |
| german |  Good |
| spanish |  Good |
| danish |  Poor |
| chinese |  Poor |
| latvian |  Good, but no POS tagger supported natively |

# Prerequesites

* [Java](https://www.java.com/fr/download/) (> 1.7),
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
  .wordTokenizer()
  .setTreeTaggerHome("/path/to/treetagger")
  .treeTagger()
  .treeTaggerLemmaFixer()
  .ttNormalizer()
  .urlFilter()
  .stemmer()
  .regexSpotter()
  .specificityComputer()
  .compostSplitter() // morphological analysis
  .syntacticVariantGatherer()
  .graphicalVariantGatherer()
  .termClassifier(TermProperty.FREQUENCY)
  .setTsvExportProperties(
      TermProperty.PILOT,
      TermProperty.FREQUENCY,
      TermProperty.WR
    )
  .tsvExporter("terminology.tsv")
  .jsonExporter("terminology.json")
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

# TermSuite Graphical Interface

(to come)

# Corpora (Collections)

In order to use TermSuite for terminology extraction and other NLP pipeplines, you need to provide a corpus path (method `TermSuitePipeline.setCollection(...)`). TermSuite currently supports two types of corpus: `tei` and `txt`.

A valid TermSuite corpus is a directory having the following directory structure :
 * first level : the corpus's root directory (the corpus name),
 * second level : the language-specific directory (full language name, with first letter capitalized),
 * third level : the collection type (currently `tei` or `txt`).
 * fourth level : the corpus's documents (the files), each having the right extension (`tei` or `txt`).

Example with the corpus `MyCorpus`:

```
MyCorpus/
  French/
    txt/
      file1.txt
      file2.txt
      file3.txt
  German/
    txt/
    file1.txt
    file2.txt
  English/
    tei/
      file1.tei
      file2.tei
      file3.tei
      file4.tei
```

# Documentation

See the [full documentation](path to website) of TermSuite for advanced uses.
