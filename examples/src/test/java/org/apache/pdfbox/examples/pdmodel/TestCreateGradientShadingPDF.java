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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Tilman Hausherr
 */
public class TestCreateGradientShadingPDF
{
    @Test
    public void testCreateGradientShading() throws IOException
    {
        String filename = "target/GradientShading.pdf";

        CreateGradientShadingPDF creator = new CreateGradientShadingPDF();
        creator.create(filename);

        PDDocument document = PDDocument.load(new File(filename));
        Set<Color> set = new HashSet<Color>();
        BufferedImage bim = new PDFRenderer(document).renderImage(0);
        for (int x = 0; x < bim.getWidth(); ++x)
        {
            for (int y = 0; y < bim.getHeight(); ++y)
            {
                set.add(new Color(bim.getRGB(x, y)));
            }
        }
        Assert.assertTrue(set.size() > 10000); // 10258 different colors on windows 10
        document.close();
    }
}
