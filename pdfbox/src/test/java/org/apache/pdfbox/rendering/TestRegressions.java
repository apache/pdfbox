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

import org.apache.pdfbox.ParallelParameterized;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * todo: JavaDoc
 *
 * @author John Hewson
 */
@RunWith(ParallelParameterized.class)  // todo: disable this test by default? (for now?)
//@Ignore
public class TestRegressions
{
    private static final String PDF_DIR = "../../regression/pdf";
    private static final String PNG_DIR = "../../regression/png";
    private static final String OUT_DIR = "../../regression/out";
    private static final String DIFF_DIR = "../../regression/diff";

    @Parameters(name = "{0}")
    public static Collection<Object[]> data()
    {
        // todo: sanity check for compatible JDK?

        // populate the input parameters
        File[] files = new File(PDF_DIR).listFiles();
        List<Object[]> params = new ArrayList<Object[]>();
        if (files != null)
        {
            for (File file : files)
            {
                if (file.getName().endsWith(".pdf"))
                {
                    params.add(new Object[] { file.getName() });
                }
            }
        }
        return params;
    }

    private final String filename;
    private File inDir, outDir, diffDir;

    public TestRegressions(String fileName)
    {
        this.filename = fileName;
    }

    @Before
    public void init()
    {
        File file = new File(PDF_DIR, filename);

        // create output dir
        String dirName =  file.getName().substring(0,  file.getName().lastIndexOf('.'));
        outDir = new File(OUT_DIR, dirName);
        outDir.mkdirs();

        // input png dirs
        inDir = new File(PNG_DIR, dirName);

        // diff dir (if any)
        diffDir = new File(DIFF_DIR, dirName);
        if (diffDir.exists())
        {
            // clean the diff dir
            for(File png: diffDir.listFiles())
            {
                png.delete();
            }
            diffDir.delete();
        }

        // clean the output dir
        for(File png: outDir.listFiles())
        {
            png.delete();
        }
    }

    @Test
    public void render() throws IOException
    {
        File file = new File(PDF_DIR, filename);
        PDDocument document = PDDocument.load(file);
        try
        {
            boolean noPNG = false;
            boolean isDifferent = false;

            PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0, size = document.getNumberOfPages(); i < size; i++)
            {
                BufferedImage imageTest = renderer.renderImageWithDPI(i, 72);

                // write to output file
                File outFile = new File(outDir, (i+1) + ".png");
                ImageIO.write(imageTest, "PNG", outFile);

                // load expected png
                File inFile = new File(inDir, (i+1) + ".png");
                if (inFile.exists())
                {
                    BufferedImage imageGood = ImageIO.read(inFile);

                    // compare
                    compare: for (int y = 0; y < imageGood.getHeight(); y++)
                    {
                        for (int x = 0; x < imageGood.getWidth(); x++)
                        {
                            if (imageGood.getRGB(x, y) != imageTest.getRGB(x, y))
                            {
                                // save diff to file
                                BufferedImage imageDiff = diff(imageGood, imageTest);
                                diffDir.mkdirs();
                                File diffFile = new File(diffDir, (i+1) + ".png");
                                ImageIO.write(imageDiff, "PNG", diffFile);

                                // keep rendering all pages
                                isDifferent = true;
                                break compare;
                            }
                        }
                    }
                }
                else
                {
                    // no expected png, probably the first time this file has been used, so we
                    // continue to render all pages, but we'll fail the test at the end
                    noPNG = true;
                }
            }

            if (noPNG)
            {
                // if this is the first time that this PDF file has been rendered then the test
                // will fail, but the "out" directory will contain the result. If it is good then
                // it can be manually added to the "png" directory and committed to SVN
                Assert.fail("No PNG found for '" + file.getName() +
                            "', perhaps this is a new file?");
            }
            else if (isDifferent)
            {
                // fail after all pages have been rendered
                Assert.fail("Rendering differs in '" + file.getName() + "'");
            }
        }
        finally
        {
            document.close();
        }
    }

    private BufferedImage diff(BufferedImage imageGood, BufferedImage imageTest)
    {
        BufferedImage diff = new BufferedImage(imageGood.getWidth(), imageGood.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        // convert good image to grayscale
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(cs, null);
        BufferedImage imageGray = op.filter(imageGood, null);

        // draw good image as grayscale background
        Graphics2D graphics = diff.createGraphics();
        graphics.setBackground(Color.WHITE);
        graphics.clearRect(0, 0, imageGood.getWidth(), imageGood.getHeight());
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        graphics.drawImage(imageGray, null, null);
        graphics.dispose();

        // draw differing pixels in red
        for (int y = 0; y < imageGood.getHeight(); y++)
        {
            for (int x = 0; x < imageGood.getWidth(); x++)
            {
                if (imageGood.getRGB(x, y) != imageTest.getRGB(x, y))
                {
                    int rgb = new Color(255, 0, 0).getRGB();
                    diff.setRGB(x, y, rgb);
                }
            }
        }
        return diff;
    }
}
