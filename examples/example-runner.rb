# This file is intended to run given examples
#
# Usage:
#   ruby start-cmd.rb CORPUS_NAME EXAMPLE_PATH

require "fileutils"
require "pathname"
require "yaml"

CORPUS, EXAMPLE= ARGV
puts "Running example #{EXAMPLE} on corpus #{CORPUS}"
CORPUS || abort("Arg corpus missing")
EXAMPLE || abort("example script missing")
CFG = File.open("example-env.yaml") { |file| YAML.load(file) }
CFG["corpora"][CORPUS] || abort("No such corpus: #{CORPUS}")
File.exist?(EXAMPLE) || abort("No such script: #{EXAMPLE}")

def to_output_file extension
  example_path = Pathname.new File.expand_path(EXAMPLE)
  root_path = Pathname.new File.expand_path(File.dirname(__FILE__))
  relative_path = example_path.relative_path_from(root_path).to_s
  dest_path = File.join(CFG["output_dir"], relative_path)
  dest_path = dest_path.gsub /\.sh$/, ".#{extension}"
  dest_dir = File.dirname(dest_path)
  FileUtils.mkdir_p(dest_dir) unless Dir.exist?(dest_dir)
  dest_path
end

%x{TS_VERSION=#{CFG["termsuite"]["version"]} \
TS_HOME=#{CFG["termsuite"]["home"]} \
TREETAGGER_HOME=/opt/treetagger \
TSV_OUTPUT_FILE=#{to_output_file("tsv")} \
JSON_OUTPUT_FILE=#{to_output_file("json")} \
TBX_OUTPUT_FILE=#{to_output_file("tbx")} \
CORPUS_PATH=#{CFG["corpora"][CORPUS]} \
sh #{EXAMPLE} > #{to_output_file("log")}}
