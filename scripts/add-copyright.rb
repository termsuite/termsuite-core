require 'tempfile'
require 'fileutils'

COPYRIGHT=%Q(
/*******************************************************************************
 * Copyright 2015-2016 - CNRS (Centre National de Recherche Scientifique)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *******************************************************************************/

)

SPOT_EXISTING_COPYRIGHT=/Copyright\s+\d{4}(?:-\d{4})?\s+-\s+CNRS/
REPLACEMENT="Copyright 2015-#{Time.now.strftime("%Y")} - CNRS"
root_dir=ARGV[0] || '.'

Dir.glob("#{root_dir}/src/{**/}*.java") do |path|
  tempfile = Tempfile.new('with_copyright')
  txt = IO.read(path)
  if(SPOT_EXISTING_COPYRIGHT =~ txt)
    puts ">>> Updating header for #{path} ..."
    new_text = txt.sub(SPOT_EXISTING_COPYRIGHT, REPLACEMENT)
  else
    puts ">>> Prepending header for #{path} ..."
    new_text = COPYRIGHT + txt
  end
  tempfile.write(new_text)
  tempfile.flush
  tempfile.close
  #puts "-"*80
  #puts new_text
  #next if STDIN.gets == '\n'
  FileUtils.cp(tempfile.path,path)
end
