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

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.infra.Blackhole;

public class Rendering {

    static final String ALTONA_TEST_SUITE = "target/pdfs/eci_altona-test-suite-v2_technical2_x4.pdf";
    static final String GHENT_CMYK_X4 = "target/pdfs/Ghent_PDF_Output_Suite_V50_Full/Categories/1-CMYK/Test pages/Ghent_PDF-Output-Test-V50_CMYK_X4.pdf";
    static final String PDF32000_2008 = "target/pdfs/PDF32000_2008.pdf";
    static final String RENDER_OUTPUT_DIR = "target/renditions";

    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.OFF);
        java.util.logging.Logger.getLogger("org.apache").setLevel(java.util.logging.Level.OFF);
        Path path = Paths.get(RENDER_OUTPUT_DIR);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            // this shouldn't fail and if it does as the
            // test should be run manually don't care atm 
        }
    }
    
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void renderGhentCMYKNoOutput(Blackhole blackhole) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(new File(GHENT_CMYK_X4)))
        {
            PDFRenderer renderer = new PDFRenderer(pdf);
            int numPages = pdf.getNumberOfPages();
            for (int i = 0; i< numPages; i++)
            {
                blackhole.consume(renderer.renderImageWithDPI(i, 600));
            }
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void renderGhentCMYK(Blackhole blackhole) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(new File(GHENT_CMYK_X4)))
        {
            PDFRenderer renderer = new PDFRenderer(pdf);
            int numPages = pdf.getNumberOfPages();
            for (int i = 0; i< numPages; i++)
            {
                BufferedImage bi = renderer.renderImageWithDPI(i, 600);
                ImageIO.write(bi, "PNG", new File(RENDER_OUTPUT_DIR, "ghent-" + i + ".png"));
            }
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void renderAltonaNoOutput(Blackhole blackhole) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(new File(ALTONA_TEST_SUITE)))
        {
            PDFRenderer renderer = new PDFRenderer(pdf);
            int numPages = pdf.getNumberOfPages();
            for (int i = 0; i< numPages; i++)
            {
                blackhole.consume(renderer.renderImageWithDPI(i, 600));
            }
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void renderAltona(Blackhole blackhole) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(new File(ALTONA_TEST_SUITE)))
        {
            PDFRenderer renderer = new PDFRenderer(pdf);
            int numPages = pdf.getNumberOfPages();
            for (int i = 0; i< numPages; i++)
            {
                BufferedImage bi = renderer.renderImageWithDPI(i, 600);
                ImageIO.write(bi, "PNG", new File(RENDER_OUTPUT_DIR, "altona-" + i + ".png"));
            }
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void renderPDFSpecNoOutput(Blackhole blackhole) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(new File(PDF32000_2008)))
        {
            PDFRenderer renderer = new PDFRenderer(pdf);
            int numPages = pdf.getNumberOfPages();
            for (int i = 0; i< numPages; i++)
            {
                blackhole.consume(renderer.renderImageWithDPI(i, 150));
            }
        }
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @BenchmarkMode(Mode.AverageTime)
    public void renderPDFSpec(Blackhole blackhole) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(new File(PDF32000_2008)))
        {
            PDFRenderer renderer = new PDFRenderer(pdf);
            int numPages = pdf.getNumberOfPages();
            for (int i = 0; i< numPages; i++)
            {
                BufferedImage bi = renderer.renderImageWithDPI(i, 150);
                ImageIO.write(bi, "PNG", new BufferedOutputStream(new FileOutputStream(new File (RENDER_OUTPUT_DIR, "pdf32000_2008-" + i + ".png"))));
            }
        }
    }
}
