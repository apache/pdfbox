/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.benchmark;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.infra.Blackhole;

public class LoadAndSave {

    static final String MEDIUM_SIZE_TEST_FILE = "target/pdfs/849-42-94772-1-10-20210818.pdf";
    static final String LARGE_SIZE_TEST_FILE = "target/pdfs/506-42-86246-2-10-20190822.pdf";

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void loadMediumFile(Blackhole blackhole) throws IOException {
        PDDocument pdf = Loader.loadPDF(new File(MEDIUM_SIZE_TEST_FILE));
        blackhole.consume(pdf);
        pdf.close();
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void saveMediumFile(Blackhole blackhole) throws IOException {
        PDDocument pdf = Loader.loadPDF(new File(MEDIUM_SIZE_TEST_FILE));
        pdf.save(OutputStream.nullOutputStream());
        pdf.close();
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void saveIncrementalMediumFile(Blackhole blackhole) throws IOException {
        PDDocument pdf = Loader.loadPDF(new File(MEDIUM_SIZE_TEST_FILE));
        pdf.saveIncremental(OutputStream.nullOutputStream());
        pdf.close();
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void saveNoCompressionMediumFile(Blackhole blackhole) throws IOException {
        PDDocument pdf = Loader.loadPDF(new File(MEDIUM_SIZE_TEST_FILE));
        pdf.save(OutputStream.nullOutputStream(),CompressParameters.NO_COMPRESSION);
        pdf.close();
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void loadLargeFile(Blackhole blackhole) throws IOException {
        PDDocument pdf = Loader.loadPDF(new File(LARGE_SIZE_TEST_FILE));
        blackhole.consume(pdf);
        pdf.close();
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void saveLargeFile(Blackhole blackhole) throws IOException {
        PDDocument pdf = Loader.loadPDF(new File(LARGE_SIZE_TEST_FILE));
        pdf.save(OutputStream.nullOutputStream());
        pdf.close();
    }


    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void saveIncrementalLargeFile(Blackhole blackhole) throws IOException {
        PDDocument pdf = Loader.loadPDF(new File(LARGE_SIZE_TEST_FILE));
        pdf.saveIncremental(OutputStream.nullOutputStream());
        pdf.close();
    }
    
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void saveNoCompressionLargeFile(Blackhole blackhole) throws IOException {
        PDDocument pdf = Loader.loadPDF(new File(LARGE_SIZE_TEST_FILE));
        pdf.save(OutputStream.nullOutputStream(),CompressParameters.NO_COMPRESSION);
        pdf.close();
    }
}