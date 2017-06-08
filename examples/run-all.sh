#!/bin/sh
#
# Run all TermSuite commandline examples
#



for example in cmd/**/*.sh
do
  echo "---"
  echo "Running $example $1"
  ruby example-runner.rb $1 $example
done
