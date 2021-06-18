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
package org.apache.pdfbox.examples.pdmodel;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Convert PDF documents to images.
 *
 * <p>
 * The example is very important to prevent OOM when parsing complex PDF files.
 * <pre>
 *     PDDocument.load(in, MemoryUsageSetting.setupTempFileOnly())
 *     renderer.setSubsamplingAllowed(true)
 * </pre>
 *
 * @author lanshiqin
 */
public final class LightlyPdfToImage {

    private static final int DPI = 100;
    private static final String FILE_SUFFIX = ".pdf";
    private static final int ARGS_LENGTH = 2;

    private LightlyPdfToImage() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != ARGS_LENGTH) {
            System.err.println("usage: " + LightlyPdfToImage.class.getName() + " <pdf-file> <output-path>");
            System.exit(1);
        }

        String pdfPath = args[0];
        String outputPath = args[1];

        if (!pdfPath.endsWith(FILE_SUFFIX)) {
            System.err.println("Last argument must be the destination .pdf file");
            System.exit(1);
        }

        InputStream in = new URL("file:///" + pdfPath).openStream();
        // Load document with temp file only
        // This is very important to prevent OOM when parsing complex PDF files
        PDDocument document = Loader.loadPDF(in, MemoryUsageSetting.setupTempFileOnly());
        try {
            // no use resource cache, Preventing large objects
            document.setResourceCache(null);
            PDFRenderer renderer = new PDFRenderer(document);
            // Indicates that the renderer is allowed to sub sample the image before drawing.
            // This is very important to prevent OOM when parsing complex PDF files
            renderer.setSubsamplingAllowed(true);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage bufferedImage = renderer.renderImageWithDPI(i, DPI);
                try {
                    ImageIOUtil.writeImage(bufferedImage, outputPath + i + ".png", DPI, -1);
                } finally {
                    bufferedImage.getGraphics().dispose();
                }
            }
        } finally {
            document.close();
            in.close();
        }
    }
}
