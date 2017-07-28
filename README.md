[![Build Status](https://travis-ci.org/termsuite/termsuite-core.svg?branch=master)](https://travis-ci.org/termsuite/termsuite-core)

TermSuite is a Java UIMA-based toolbox for terminology extraction and multilingual term alignment. To install TermSuite and run terminology extraction, see the [Getting Started](http://termsuite.github.io/getting-started/) guide.


# Getting Started

Download, install and get TermSuite running on a short example without the [Getting started](http://termsuite.github.io/getting-started/) guide.

# Developers

### Building TermSuite from sources

Requirements:
 
 * git
 * gradle

~~~
$ git clone git@github.com:termsuite/termsuite-core.git
$ cd termsuite-core
$ gradle
~~~

The jar will then be found under ./build/libs.

### Use TermSuite as a Maven dependency

~~~

<dependency>
  <groupId>fr.univ-nantes.termsuite</groupId>
  <artifactId>termsuite-core</artifactId>
  <version>3.0.9</version>
</dependency>

~~~


 * Install TermSuite and run terminology extraction: [Getting started](http://termsuite.github.io/getting-started/) guide
 * User manual: [Documentation](http://termsuite.github.io/documentation/introduction/)
 * Official site: [TermSuite](http://termsuite.github.io/)


### Running TermSuite functional tests

 1. `cp src/test/resources/termsuite-test.properties.sample src/test/resources/termsuite-test.properties`,
 2. In `termsuite-test.properties`, set the path to your local TreeTagger installation,
 3. Run `gradle cucumberTest` from command line, or launch the `FunctionalTests` test suite from your IDE's JUnit launcher.
  

