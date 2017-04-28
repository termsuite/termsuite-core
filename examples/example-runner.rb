# This file is intended to run given examples and ensure that
# example markdown front matters are valid before being published.
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

def header_lines
  hlines = []
  for line in IO.readlines(EXAMPLE).map(&:chomp)
    next if line.strip.empty?
    if line.start_with?("#")
      l = line.gsub /^\s*#\s/, ""
      hlines << l
    else
      break
    end
  end
  hlines
end

def check_markdown_front_matter
  startf = header_lines.index "---"
  abort("no mardown front matter found") if startf.nil?
  endf = startf + header_lines[(startf+1)..-1].index("---")
  abort("no mardown front matter found") if startf.nil?
  fmatter = header_lines[startf..endf].join("\n")
  config = YAML.load(fmatter)
  %w{title excerpt}.each do |key|
    if(!config[key] || config[key].empty?)
      abort("No key \"#{key}\" found in front matter")
    end
  end
end

def to_dest_path
  example_path = Pathname.new File.expand_path(EXAMPLE)
  root_path = Pathname.new File.expand_path(File.dirname(__FILE__))
  relative_path = example_path.relative_path_from(root_path).to_s
  dest_path = File.join(CFG["output_dir"], relative_path)
end

def to_output_dir name
  dest_path = to_dest_path
  dest_path = dest_path.gsub /\.sh$/, ""
  dest_dir = File.join(dest_path, name)
  FileUtils.mkdir_p(File.dirname(dest_dir)) unless Dir.exist?(dest_dir)
  dest_dir
end

def to_output_file extension
  dest_path = to_dest_path
  dest_path = dest_path.gsub /\.sh$/, ".#{extension}"
  dest_dir = File.dirname(dest_path)
  FileUtils.mkdir_p(dest_dir) unless Dir.exist?(dest_dir)
  dest_path
end

check_markdown_front_matter()

%x{TS_VERSION=#{CFG["termsuite"]["version"]} \
TS_HOME=#{CFG["termsuite"]["home"]} \
TREETAGGER_HOME=/opt/treetagger \
SYNONYMS_DICO=#{CFG["termsuite"]["resources"]} \
PREPARED_CORPUS_PATH=#{CFG["corpora"]["prepared"]} \
TSV_OUTPUT_FILE=#{to_output_file("tsv")} \
CUSTOM_RESOURCE_DIR=#{CFG["resource_dir"]} \
PREPARED_CORPUS_AS_TERMINOLOGY_PATH=#{to_output_file("prepared.json")} \
PREPARED_CORPUS_PATH_JSON=#{to_output_dir("prepared_json")} \
PREPARED_CORPUS_PATH_XMI=#{to_output_dir("prepared_xmi")} \
TBX_OUTPUT_FILE=#{to_output_file("tbx")} \
CORPUS_PATH=#{CFG["corpora"][CORPUS]} \
sh #{EXAMPLE} > #{to_output_file("log")}}
