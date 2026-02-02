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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.Callable;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Create a PDF document from images.
 */
@Command(name = "imagetopdf", header = "Creates a PDF document from images", versionProvider = Version.class, mixinStandardHelpOptions = true)
public final class ImageToPDF implements Callable<Integer>
{
    private PDRectangle mediaBox = PDRectangle.LETTER;

    // Expected for CLI app to write to System.out/System.err
    @SuppressWarnings("squid:S106")
    private final PrintStream SYSERR;

    @Option(names = "-autoOrientation", description = "set orientation depending of image proportion")
    private boolean autoOrientation = false;

    @Option(names = "-landscape", description = "set orientation to landscape")
    private boolean landscape = false;

    @Option(names = "-pageSize", description = "the page size to use: Letter, Legal, A0, A1, A2, A3, A4, A5, A6 (default: ${DEFAULT-VALUE})")
    private String pageSize = "Letter";

    @Option(names = "-resize", description = "resize to page size")
    private boolean resize = false;

    @Option(names = {"-i", "--input"}, description = "the image files to convert", paramLabel="image-file", required = true)
    private File[] infiles;

    @Option(names = {"-o", "--output"}, description = "the generated PDF file", required = true)
    private File outfile;

    /**
     * Constructor.
     */
    public ImageToPDF()
    {
        SYSERR = System.err;
    }

    public static void main(String[] args)
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        int exitCode = new CommandLine(new ImageToPDF()).execute(args);
        System.exit(exitCode);
    }

    public Integer call()
    {
        setMediaBox(createRectangle(pageSize));

        try (PDDocument doc = new PDDocument())
        {
            for (File imageFile : infiles)
            {
                PDImageXObject pdImage = PDImageXObject.createFromFile(imageFile.getAbsolutePath(), doc);

                PDRectangle actualMediaBox = mediaBox;
                if ((autoOrientation && pdImage.getWidth() > pdImage.getHeight()) || landscape)
                {
                    actualMediaBox = new PDRectangle(mediaBox.getHeight(), mediaBox.getWidth());
                }
                PDPage page = new PDPage(actualMediaBox);
                doc.addPage(page);

                try (PDPageContentStream contents = new PDPageContentStream(doc, page))
                {
                    if (resize)
                    {
                        contents.drawImage(pdImage, 0, 0, actualMediaBox.getWidth(), actualMediaBox.getHeight());
                    }
                    else
                    {
                        contents.drawImage(pdImage, 0, 0, pdImage.getWidth(), pdImage.getHeight());
                    }
                }
            }
            doc.save(outfile);
        }
        catch (IOException ioe)
        {
            SYSERR.println( "Error converting image to PDF [" + ioe.getClass().getSimpleName() + "]: " + ioe.getMessage());
            return 4;
        }
        return 0;
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
            // return default if wron size was specified
            return PDRectangle.LETTER;
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
     * @param mediaBox the media box of the PDF document.
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
     * @param landscape use landscape orientation.
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
}
