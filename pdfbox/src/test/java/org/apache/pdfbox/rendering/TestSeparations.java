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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
import org.junit.Test;

public class TestSeparations {
    private static final String INPUT_DIR = "/input/rendering";
    private static String OUTPUT_DIR = "src/test/resources/output/rendering";

    @Test
    public void test() throws IOException, URISyntaxException {
        float maxSize = 2048;
        float maxScale = 300 / 72f;
        String filename = "FANTASTICCMYK.ai";
        Path filePath = Paths.get(getClass().getResource(INPUT_DIR + "/" + filename).toURI());
        File file = filePath.toFile();
        PDDocument document = PDDocument.load(file); 
        PDRectangle cropBox = document.getPage(0).getCropBox();        
        float wScale = maxSize / cropBox.getWidth();
        float hScale = maxSize / cropBox.getHeight();
        float scale = wScale < hScale ? wScale : hScale;        

        if (scale > maxScale) {
            scale = maxScale;
        }

        PDPage page = document.getPage(0);
        PDFRenderer renderer = new PDFRenderer(document);

        //renderSeparation(scale, filename, document, renderer, "PANTONE Orange 021 C");
        
        renderComposite(page, filename, renderer, scale);

        renderCMYKSeparations(page, filename, renderer, scale);

        renderSpotColorSeparations(page, filename, renderer, scale);

        document.close();
    }

	private void renderSeparation(float scale, String filename, PDDocument document, PDFRenderer renderer, String colorant) throws IOException {
		PDResources pageResources = document.getPage(0).getResources();

        for (COSName csName : pageResources.getColorSpaceNames()) {
            PDColorSpace colorSpace = pageResources.getColorSpace(csName);

            if (colorSpace instanceof PDSeparation) {
                PDSeparation separation = (PDSeparation)colorSpace;

                String colorantName = separation.getColorantName();

                if (colorantName.equals(colorant)) {
                    BufferedImage image = renderer.renderImage(0, scale, separation, -1);

                    writeImage(image, OUTPUT_DIR + "/" + filename + "." + colorantName);
                }
            }
        }
    }

    private void renderSpotColorSeparations(PDPage page, String filename, PDFRenderer renderer, float scale) throws IOException {
        PDSeparation[] separations =  new PDSeparation[renderer.getSeparations().size()];
        
        separations = renderer.getSeparations().toArray(separations);

        for (PDSeparation separation : separations) {
            String colorant = separation.getColorantName();

            if (colorant.equals("All")) {
                continue;
            }

            BufferedImage image = renderer.renderImage(page, scale, separation);

            writeImage(image, OUTPUT_DIR + "/" + filename + "." + colorant, "png");
        }
    }

    private void renderComposite(PDPage page, String filename, PDFRenderer renderer, float scale) throws IOException {
        BufferedImage image = renderer.renderImage(page, scale);

        writeImage(image, OUTPUT_DIR + "/" + filename, "png");
    }

    private void renderCMYKSeparations(PDPage page, String filename, PDFRenderer renderer, float scale) throws IOException {
		String[] processColors = new String[] { "Cyan", "Magenta", "Yellow", "Black" };

        for (int i = 0; i < processColors.length; i++) {
            BufferedImage image = renderer.renderImage(page, scale, PDDeviceCMYK.INSTANCE, i);

            writeImage(image, OUTPUT_DIR + "/" + filename + "." + processColors[i], "png");
        }
    }

    private void writeImage(BufferedImage image, String filename, String extension) throws IOException {
        FileOutputStream outputFile = new FileOutputStream(filename + "." + extension);

        try {
            ImageIO.write(image, extension, outputFile);
        }
        finally {
            outputFile.close();
        }
    }

    private void writeImage(BufferedImage image, String filename) throws IOException {
        writeImage(image, filename, "jpg");
    }
}