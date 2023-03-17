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

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Convert a PDF document to an image.
 *
 * @author Ben Litchfield
 */
@Command(name = "pdftoimage", header = "Converts a PDF document to image(s)", versionProvider = Version.class, mixinStandardHelpOptions = true)
public final class PDFToImage implements Callable<Integer>
{
    // Expected for CLI app to write to System.out/System.err
    @SuppressWarnings("squid:S106")
    private static final PrintStream SYSERR = System.err;

    @Option(names = "-password", description = "the password to decrypt the document", arity = "0..1", interactive = true)
    private String password;

    @Option(names = {"-format"}, description = "the image file format (default: ${DEFAULT-VALUE})")    
    private String imageFormat = "jpg";

    @Option(names = {"-prefix", "-outputPrefix"}, description = "the filename prefix for image files")    
    private String outputPrefix;

    @Option(names = "-page", description = "the only page to extract (1-based)")    
    private int page = -1;

    @Option(names = "-startPage", description = "the first page to start extraction (1-based)")    
    private int startPage = 1;

    @Option(names = "-endPage", description = "the last page to extract (inclusive)")    
    private int endPage = Integer.MAX_VALUE;

    @Option(names = "-color", description = "the color depth (valid: ${COMPLETION-CANDIDATES}) (default: ${DEFAULT-VALUE})")    
    private ImageType imageType = ImageType.RGB;

    @Option(names = {"-dpi", "-resolution"}, description = "the DPI of the output image, default: screen resolution or 96 if unknown")
    private int dpi;

    @Option(names = "-quality", description = "the quality to be used when compressing the image (0 <= quality <= 1) " +
        "(default: 0 for PNG and 1 for the other formats)")
    private float quality = -1;

    @Option(names = "-cropbox", arity="4", description = "the page area to export")
    private int[] cropbox;

    @Option(names = "-time", description = "print timing information to stdout")
    private boolean showTime;

    @Option(names = "-subsampling", description = "activate subsampling (for PDFs with huge images)")
    private boolean subsampling;

    @Option(names = {"-i", "--input"}, description = "the PDF files to convert.", required = true)
    private File infile;

    /**
     * Infamous main method.
     *
     * @param args Command line arguments, should be one and a reference to a file.
     *
     */
    public static void main( String[] args )
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");
        int exitCode = new CommandLine(new PDFToImage()).execute(args);
        System.exit(exitCode);
    }

    public Integer call()
    {
        if (outputPrefix == null)
        {
            outputPrefix = FilenameUtils.removeExtension(infile.getAbsolutePath());
        }

        if (!getImageFormats().contains(imageFormat))
        {
            SYSERR.println( "Error: Invalid image format " + imageFormat + " - supported are: " + getImageFormats());
            return 2;
        }

        if (quality < 0)
        {
            quality = "png".equals(imageFormat) ? 0f : 1f;
        }

        if (dpi == 0)
        {
            try
            {
                dpi = Toolkit.getDefaultToolkit().getScreenResolution();
            }
            catch (HeadlessException e)
            {
                dpi = 96;
            }
        }

        try (PDDocument document = Loader.loadPDF(infile, password))
        {
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            if (acroForm != null && acroForm.getNeedAppearances())
            {
                acroForm.refreshAppearances();
            }

            if (cropbox != null)
            {
                changeCropBox(document, cropbox[0], cropbox[1], cropbox[2], cropbox[3]);
            }

            long startTime = System.nanoTime();

            // render the pages
            boolean success = true;
            endPage = Math.min(endPage, document.getNumberOfPages());
            PDFRenderer renderer = new PDFRenderer(document);
            renderer.setSubsamplingAllowed(subsampling);
            for (int i = startPage - 1; i < endPage; i++)
            {
                BufferedImage image = renderer.renderImageWithDPI(i, dpi, imageType);
                String fileName = outputPrefix + "-" + (i + 1) + "." + imageFormat;
                success &= ImageIOUtil.writeImage(image, fileName, dpi, quality);
            }

            // performance stats
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            int count = 1 + endPage - startPage;
            if (showTime)
            {
                SYSERR.printf("Rendered %d page%s in %dms%n", count, count == 1 ? "" : "s",
                                  duration / 1000000);
            }

            if (!success)
            {
                SYSERR.println( "Error: no writer found for image format '" + imageFormat + "'" );
                return 1;
            }
        }
        catch (IOException ioe)
        {
            SYSERR.println( "Error converting document [" + ioe.getClass().getSimpleName() + "]: " + ioe.getMessage());
            return 4;
        }
        return 0;
    }

    private static String getImageFormats()
    {
        StringBuilder retval = new StringBuilder();
        String[] formats = ImageIO.getWriterFormatNames();
        for( int i = 0; i < formats.length; i++ )
        {
           if (formats[i].equalsIgnoreCase(formats[i]))
           {
               retval.append( formats[i] );
               if( i + 1 < formats.length )
               {
                   retval.append( ", " );
               }
           }
        }
        return retval.toString();
    }

    private static void changeCropBox(PDDocument document, float a, float b, float c, float d)
    {
        for (PDPage page : document.getPages())
        {
            PDRectangle rectangle = new PDRectangle();
            rectangle.setLowerLeftX(a);
            rectangle.setLowerLeftY(b);
            rectangle.setUpperRightX(c);
            rectangle.setUpperRightY(d);
            page.setCropBox(rectangle);
        }
    }
}
