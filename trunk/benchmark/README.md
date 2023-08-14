<!---
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
--->

# Benchmark module

This module contains benchmarks written using [JMH](https://openjdk.java.net/projects/code-tools/jmh/) from OpenJDK.

## Preparation using the Ghent PDF Output Suite V50

As the Ghent PDF Output Suite V50 can not be downloaded automatically as a license has to be agreed on the following steps need to be done prior to running `Rendering.renderGhentCMYK`:

- select [Download](https://gwg.org/download/ghentpdfoutputsuitev50/) on the download page
- unpack the ZIP file in the directory `target/pdfs` so that the directory `Ghent_PDF_Output_Suite_V50_Full` is on top
- keep only `Ghent_PDF_Output_Suite_V50_Full/Categories/1-CMYK/Test pages/Ghent_PDF-Output-Test-V50_CMYK_X4.pdf`

## Running benchmarks

The default behavior is to run all benchmarks:

    java -jar target/benchmarks.jar

List all available benchmarks:

    java -jar target/benchmarks.jar -l

Pass a pattern or name after the command to select the benchmarks:

    java -jar target/benchmarks.jar LoadAndSave.loadMediumFile

Check which benchmarks match the provided pattern:

    java -jar target/benchmarks.jar -l LoadAndSave

Run a specific test and override the number of forks, iterations and warm-up iteration to `2`:

    java -jar target/benchmarks.jar  -f 2 -i 2 -wi 2 LoadAndSave.loadMediumFile

Get a list of available profilers:

    java -jar target/benchmarks.jar -lprof

The following sections cover async profiler and GC profilers in more detail.

## Using JMH with async-profiler

JMH includes [async-profiler](https://github.com/jvm-profiling-tools/async-profiler). After download run 
JMH using the async-profiler:

    java -jar target/benchmarks.jar -prof async:libPath=/path/to/libasyncProfiler.so

With flame graph output (the semicolon is escaped to ensure it is not treated as a command separator):

    java -jar target/benchmarks.jar -prof async:libPath=/path/to/libasyncProfiler.so\;output=flamegraph

To get help on options to be used for the async-profiler use the following command:

    java -jar target/benchmarks.jar -prof async:help

## Using JMH with GC profiler

To measure the allocation rate run the benchmark with `-prof gc`:

    java -jar target/benchmarks.jar -prof gc

For profiling the `norm` alloc rates are important as this which measure the allocations per operation rather than allocations per second which can increase/decrease with faster/slower code.

## Writing benchmarks

Examples for writing JMH tests are available from the projects [samples](https://github.com/openjdk/jmh/tree/master/jmh-samples) provided by the JMH project.

Tutorials are available at 

  - http://tutorials.jenkov.com/java-performance/jmh.html
  - https://www.baeldung.com/java-microbenchmark-harness
  - https://mkyong.com/java/java-jmh-benchmark-tutorial/