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
package org.apache.pdfbox.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * Creates a PDF document from images.
 *
 */
public final class ImageToPDF
{
    private PDRectangle mediaBox = PDRectangle.LETTER;
    private boolean landscape = false;
    private boolean autoOrientation = false;
    private boolean resize = false;

    private ImageToPDF()
    {
    }

    public static void main(String[] args) throws IOException
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        ImageToPDF app = new ImageToPDF();

        if (args.length < 2)
        {
            app.usage();
        }

        List<String> imageFilenames = new ArrayList<String>();
        String pdfPath = args[args.length - 1];

        if (!pdfPath.endsWith(".pdf"))
        {
            System.err.println("Last argument must be the destination .pdf file");
            System.exit(1);
        }
        for (int i = 0; i < args.length - 1; i++)
        {
            if (args[i].startsWith("-"))
            {
                if ("-resize".equals(args[i]))
                {
                    // will be modified to something more flexible
                    app.resize = true;
                }
                else if ("-landscape".equals(args[i]))
                {
                    app.setLandscape(true);
                }
                else if ("-autoOrientation".equals(args[i]))
                {
                    app.setAutoOrientation(true);
                }
                else if ("-pageSize".equals(args[i]))
                {
                    i++;
                    PDRectangle rectangle = createRectangle(args[i]);
                    if (rectangle == null)
                    {
                        throw new IOException("Unknown argument: " + args[i]);
                    }
                    app.setMediaBox(rectangle);
                }
                else
                {
                    throw new IOException("Unknown argument: " + args[i]);
                }
            }
            else
            {
                imageFilenames.add(args[i]);
            }
        }

        PDDocument doc = new PDDocument();
        app.createPDFFromImages(doc, imageFilenames);
        doc.save(pdfPath);
        doc.close();
    }

    void createPDFFromImages(PDDocument doc, List<String> imageFilenames) throws IOException
    {
        for (String imageFileName : imageFilenames)
        {
            PDImageXObject pdImage = PDImageXObject.createFromFile(imageFileName, doc);

            PDRectangle actualMediaBox = mediaBox;
            if ((autoOrientation && pdImage.getWidth() > pdImage.getHeight()) || landscape)
            {
                actualMediaBox = new PDRectangle(mediaBox.getHeight(), mediaBox.getWidth());
            }
            PDPage page = new PDPage(actualMediaBox);
            doc.addPage(page);

            PDPageContentStream contents = new PDPageContentStream(doc, page);
            if (resize)
            {
                contents.drawImage(pdImage, 0, 0, actualMediaBox.getWidth(), actualMediaBox.getHeight());
            }
            else
            {
                contents.drawImage(pdImage, 0, 0, pdImage.getWidth(), pdImage.getHeight());
            }
            contents.close();
        }
    }

    private static PDRectangle createRectangle(String paperSize)
    {
        if ("letter".equalsIgnoreCase(paperSize))
        {
            return PDRectangle.LETTER;
        }
        else if ("legal".equalsIgnoreCase(paperSize))
        {
            return PDRectangle.LEGAL;
        }
        else if ("A0".equalsIgnoreCase(paperSize))
        {
            return PDRectangle.A0;
        }
        else if ("A1".equalsIgnoreCase(paperSize))
        {
            return PDRectangle.A1;
        }
        else if ("A2".equalsIgnoreCase(paperSize))
        {
            return PDRectangle.A2;
        }
        else if ("A3".equalsIgnoreCase(paperSize))
        {
            return PDRectangle.A3;
        }
        else if ("A4".equalsIgnoreCase(paperSize))
        {
            return PDRectangle.A4;
        }
        else if ("A5".equalsIgnoreCase(paperSize))
        {
            return PDRectangle.A5;
        }
        else if ("A6".equalsIgnoreCase(paperSize))
        {
            return PDRectangle.A6;
        }
        else
        {
            return null;
        }
    }

    /**
     * Sets page size of produced PDF.
     *
     * @return returns the page size (media box)
     */
    public PDRectangle getMediaBox()
    {
        return mediaBox;
    }

    /**
     * Sets page size of produced PDF.
     *
     * @param mediaBox
     */
    public void setMediaBox(PDRectangle mediaBox)
    {
        this.mediaBox = mediaBox;
    }

    /**
     * Tells the paper orientation.
     *
     * @return true for landscape orientation
     */
    public boolean isLandscape()
    {
        return landscape;
    }

    /**
     * Sets paper orientation.
     *
     * @param landscape
     */
    public void setLandscape(boolean landscape)
    {
        this.landscape = landscape;
    }

    /**
     * Gets whether page orientation (portrait / landscape) should be decided automatically for each
     * page depending on image proportion.
     *
     * @return true if auto, false if not.
     */
    public boolean isAutoOrientation()
    {
        return autoOrientation;
    }

    /**
     * Sets whether page orientation (portrait / landscape) should be decided automatically for each
     * page depending on image proportion.
     *
     * @param autoOrientation true if auto, false if not.
     */
    public void setAutoOrientation(boolean autoOrientation)
    {
        this.autoOrientation = autoOrientation;
    }

    /**
     * This will print out a message telling how to use this example.
     */
    private void usage()
    {
        StringBuilder message = new StringBuilder();
        message.append("Usage: jar -jar pdfbox-app-x.y.z.jar ImageToPDF [options] <image-file>..<image-file> <output-file>\n");
        message.append("\nOptions:\n");
        message.append("  -resize              : resize to page size\n");
        message.append("  -pageSize <pageSize> : Letter (default)\n");
        message.append("                         Legal\n");
        message.append("                         A0\n");
        message.append("                         A1\n");
        message.append("                         A2\n");
        message.append("                         A3\n");
        message.append("                         A4\n");
        message.append("                         A5\n");
        message.append("                         A6\n");
        message.append("  -landscape           : sets orientation to landscape\n");
        message.append("  -autoOrientation     : sets orientation depending of image proportion\n");

        System.err.println(message.toString());
        System.exit(1);
    }
}
