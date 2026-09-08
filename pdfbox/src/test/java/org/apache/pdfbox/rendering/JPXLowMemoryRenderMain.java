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
package org.apache.pdfbox.rendering;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * Renders the first page of a PDF at half scale, the same way as reported in PDFBOX-5876. Run in
 * its own JVM with a constrained heap by {@link TestQuality#testPDFBox5876()}, since the heap size
 * of the JVM already running the test suite can't be changed after the fact.
 */
public final class JPXLowMemoryRenderMain
{
    private JPXLowMemoryRenderMain()
    {
    }

    public static void main(String[] args) throws Exception
    {
        File file = new File(args[0]);
        try (PDDocument doc = Loader.loadPDF(file, IOUtils.createTempFileOnlyStreamCache()))
        {
            PDFRenderer renderer = new PDFRenderer(doc);
            renderer.setSubsamplingAllowed(true);
            BufferedImage bim = renderer.renderImage(0, 0.5f);
            File outputFile = new File("target/test-output", file.getName() + "-p1.png");
            boolean ok = ImageIO.write(bim, "png", outputFile);
            if (!ok)
            {
                System.exit(1);
            }
        }
    }
}
