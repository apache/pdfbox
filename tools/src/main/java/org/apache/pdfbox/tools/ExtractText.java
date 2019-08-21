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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;

/**
 * This is the main program that simply parses the pdf document and transforms it
 * into text.
 *
 * @author Ben Litchfield
 * @author Tilman Hausherr
 */
public final class ExtractText
{
    private static final Log LOG = LogFactory.getLog(ExtractText.class);

    @SuppressWarnings({"squid:S2068"})
    private static final String PASSWORD = "-password";
    private static final String ENCODING = "-encoding";
    private static final String CONSOLE = "-console";
    private static final String START_PAGE = "-startPage";
    private static final String END_PAGE = "-endPage";
    private static final String SORT = "-sort";
    private static final String IGNORE_BEADS = "-ignoreBeads";
    private static final String DEBUG = "-debug";
    private static final String HTML = "-html";
    private static final String ALWAYSNEXT = "-alwaysNext";
    private static final String ROTATION_MAGIC = "-rotationMagic";
    private static final String STD_ENCODING = "UTF-8";

    /*
     * debug flag
     */
    private boolean debug = false;

    /**
     * private constructor.
    */
    private ExtractText()
    {
        //static class
    }

    /**
     * Infamous main method.
     *
     * @param args Command line arguments, should be one and a reference to a file.
     *
     * @throws IOException if there is an error reading the document or extracting the text.
     */
    public static void main( String[] args ) throws IOException
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        ExtractText extractor = new ExtractText();
        extractor.startExtraction(args);
    }
    /**
     * Starts the text extraction.
     *  
     * @param args the commandline arguments.
     * @throws IOException if there is an error reading the document or extracting the text.
     */
    public void startExtraction( String[] args ) throws IOException
    {
        boolean toConsole = false;
        boolean toHTML = false;
        boolean sort = false;
        boolean separateBeads = true;
        boolean alwaysNext = false;
        boolean rotationMagic = false;
        @SuppressWarnings({"squid:S2068"})
        String password = "";
        String encoding = STD_ENCODING;
        String pdfFile = null;
        String outputFile = null;
        // Defaults to text files
        String ext = ".txt";
        int startPage = 1;
        int endPage = Integer.MAX_VALUE;
        for( int i=0; i<args.length; i++ )
        {
            if( args[i].equals( PASSWORD ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                password = args[i];
            }
            else if( args[i].equals( ENCODING ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                encoding = args[i];
            }
            else if( args[i].equals( START_PAGE ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                startPage = Integer.parseInt( args[i] );
            }
            else if( args[i].equals( HTML ) )
            {
                toHTML = true;
                ext = ".html";
            }
            else if( args[i].equals( SORT ) )
            {
                sort = true;
            }
            else if( args[i].equals( IGNORE_BEADS ) )
            {
                separateBeads = false;
            }
            else if( args[i].equals( DEBUG ) )
            {
                debug = true;
            }
            else if (args[i].equals(ALWAYSNEXT))
            {
                alwaysNext = true;
            }
            else if (args[i].equals(ROTATION_MAGIC))
            {
                rotationMagic = true;
            }
            else if( args[i].equals( END_PAGE ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                endPage = Integer.parseInt( args[i] );
            }
            else if( args[i].equals( CONSOLE ) )
            {
                toConsole = true;
            }
            else
            {
                if( pdfFile == null )
                {
                    pdfFile = args[i];
                }
                else
                {
                    outputFile = args[i];
                }
            }
        }

        if( pdfFile == null )
        {
            usage();
        }
        else
        {

            Writer output = null;
            PDDocument document = null;
            try
            {
                long startTime = startProcessing("Loading PDF "+pdfFile);
                if( outputFile == null && pdfFile.length() >4 )
                {
                    outputFile = new File( pdfFile.substring( 0, pdfFile.length() -4 ) + ext ).getAbsolutePath();
                }
                document = PDDocument.load(new File( pdfFile ), password);
                
                AccessPermission ap = document.getCurrentAccessPermission();
                if( ! ap.canExtractContent() )
                {
                    throw new IOException( "You do not have permission to extract text" );
                }
                
                stopProcessing("Time for loading: ", startTime);

                if( toConsole )
                {
                    output = new OutputStreamWriter( System.out, encoding );
                }
                else
                {
                    if (toHTML && !STD_ENCODING.equals(encoding))
                    {
                        encoding = STD_ENCODING;
                        System.out.println("The encoding parameter is ignored when writing html output.");
                    }
                    output = new OutputStreamWriter( new FileOutputStream( outputFile ), encoding );
                }
                startTime = startProcessing("Starting text extraction");
                if (debug)
                {
                    System.err.println("Writing to " + outputFile);
                }

                PDFTextStripper stripper;
                if(toHTML)
                {
                    // HTML stripper can't work page by page because of startDocument() callback
                    stripper = new PDFText2HTML();
                    stripper.setSortByPosition(sort);
                    stripper.setShouldSeparateByBeads(separateBeads);
                    stripper.setStartPage(startPage);
                    stripper.setEndPage(endPage);

                    // Extract text for main document:
                    stripper.writeText(document, output);
                }
                else
                {
                    if (rotationMagic)
                    {
                        stripper = new FilteredTextStripper();
                    }
                    else
                    {
                        stripper = new PDFTextStripper();
                    }
                    stripper.setSortByPosition(sort);
                    stripper.setShouldSeparateByBeads(separateBeads);

                    // Extract text for main document:
                    extractPages(startPage, Math.min(endPage, document.getNumberOfPages()), 
                                 stripper, document, output, rotationMagic, alwaysNext);
                }

                // ... also for any embedded PDFs:
                PDDocumentCatalog catalog = document.getDocumentCatalog();
                PDDocumentNameDictionary names = catalog.getNames();    
                if (names != null)
                {
                    PDEmbeddedFilesNameTreeNode embeddedFiles = names.getEmbeddedFiles();
                    if (embeddedFiles != null)
                    {
                        Map<String, PDComplexFileSpecification> embeddedFileNames = embeddedFiles.getNames();
                        if (embeddedFileNames != null)
                        {
                            for (Map.Entry<String, PDComplexFileSpecification> ent : embeddedFileNames.entrySet()) 
                            {
                                if (debug)
                                {
                                    System.err.println("Processing embedded file " + ent.getKey() + ":");
                                }
                                PDComplexFileSpecification spec = ent.getValue();
                                PDEmbeddedFile file = spec.getEmbeddedFile();
                                if (file != null && "application/pdf".equals(file.getSubtype()))
                                {
                                    if (debug)
                                    {
                                        System.err.println("  is PDF (size=" + file.getSize() + ")");
                                    }
                                    try (InputStream fis = file.createInputStream();
                                        PDDocument subDoc = PDDocument.load(fis))
                                    {
                                        if (toHTML)
                                        {
                                            // will not really work because of HTML header + footer
                                            stripper.writeText( subDoc, output );
                                        }
                                        else
                                        {
                                            extractPages(1, subDoc.getNumberOfPages(),
                                                         stripper, subDoc, output, rotationMagic, alwaysNext);
                                        }
                                    } 
                                }
                            } 
                        }
                    }
                }
                stopProcessing("Time for extraction: ", startTime);
            }
            finally
            {
                IOUtils.closeQuietly(output);
                IOUtils.closeQuietly(document);
            }
        }
    }

    private void extractPages(int startPage, int endPage,
            PDFTextStripper stripper, PDDocument document, Writer output,
            boolean rotationMagic, boolean alwaysNext) throws IOException
    {
        for (int p = startPage; p <= endPage; ++p)
        {
            stripper.setStartPage(p);
            stripper.setEndPage(p);
            try
            {
                if (rotationMagic)
                {
                    PDPage page = document.getPage(p - 1);
                    int rotation = page.getRotation();
                    page.setRotation(0);
                    AngleCollector angleCollector = new AngleCollector();
                    angleCollector.setStartPage(p);
                    angleCollector.setEndPage(p);
                    angleCollector.writeText(document, new NullWriter());
                    // rotation magic
                    for (int angle : angleCollector.getAngles())
                    {
                        // prepend a transformation
                        // (we could skip these parts for angle 0, but it doesn't matter much)
                        try (PDPageContentStream cs = new PDPageContentStream(document, page, 
                                PDPageContentStream.AppendMode.PREPEND, false))
                        {
                            cs.transform(Matrix.getRotateInstance(-Math.toRadians(angle), 0, 0));
                        }

                        stripper.writeText(document, output);

                        // remove prepended transformation
                        ((COSArray) page.getCOSObject().getItem(COSName.CONTENTS)).remove(0);
                    }
                    page.setRotation(rotation);
                }
                else
                {
                    stripper.writeText(document, output);
                }
            }
            catch (IOException ex)
            {
                if (!alwaysNext)
                {
                    throw ex;
                }
                LOG.error("Failed to process page " + p, ex);
            }
        }
    }

    private long startProcessing(String message) 
    {
        if (debug) 
        {
            System.err.println(message);
        }
        return System.currentTimeMillis();
    }
    
    private void stopProcessing(String message, long startTime) 
    {
        if (debug)
        {
            long stopTime = System.currentTimeMillis();
            float elapsedTime = ((float)(stopTime - startTime))/1000;
            System.err.println(message + elapsedTime + " seconds");
        }
    }

    static int getAngle(TextPosition text)
    {
        // should this become a part of TextPosition?
        Matrix m = text.getTextMatrix().clone();
        m.concatenate(text.getFont().getFontMatrix());
        return (int) Math.round(Math.toDegrees(Math.atan2(m.getShearY(), m.getScaleY())));
    }

    /**
     * This will print the usage requirements and exit.
     */
    private static void usage()
    {
        String message = "Usage: java -jar pdfbox-app-x.y.z.jar ExtractText [options] <inputfile> [output-text-file]\n"
            + "\nOptions:\n"
            + "  -password <password>        : Password to decrypt document\n"
            + "  -encoding <output encoding> : UTF-8 (default) or ISO-8859-1, UTF-16BE,\n"
            + "                                UTF-16LE, etc.\n"
            + "  -console                    : Send text to console instead of file\n"
            + "  -html                       : Output in HTML format instead of raw text\n"
            + "  -sort                       : Sort the text before writing\n"
            + "  -ignoreBeads                : Disables the separation by beads\n"
            + "  -debug                      : Enables debug output about the time consumption\n"
            + "                                of every stage\n"
            + "  -alwaysNext                 : Process next page (if applicable) despite\n"
            + "                                IOException (ignored when -html)\n"
            + "  -rotationMagic              : Analyze each page for rotated/skewed text,\n"
            + "                                rotate to 0° and extract separately\n"
            + "                                (slower, and ignored when -html)\n"
            + "  -startPage <number>         : The first page to start extraction (1 based)\n"
            + "  -endPage <number>           : The last page to extract (1 based, inclusive)\n"
            + "  <inputfile>                 : The PDF document to use\n"
            + "  [output-text-file]          : The file to write the text to";
        
        System.err.println(message);
        System.exit( 1 );
    }
}

/**
 * Collect all angles while doing text extraction. Angles are in degrees and rounded to the closest
 * integer (to avoid slight differences from floating point arithmethic resulting in similarly
 * angled glyphs being treated separately). This class must be constructed for each page so that the
 * angle set is initialized.
 */
class AngleCollector extends PDFTextStripper
{
    private final Set<Integer> angles = new TreeSet<>();

    AngleCollector() throws IOException
    {
    }

    Set<Integer> getAngles()
    {
        return angles;
    }

    @Override
    protected void processTextPosition(TextPosition text)
    {
        int angle = ExtractText.getAngle(text);
        angle = (angle + 360) % 360;
        angles.add(angle);
    }
}

/**
 * TextStripper that only processes glyphs that have angle 0.
 */
class FilteredTextStripper extends PDFTextStripper
{
    FilteredTextStripper() throws IOException
    {
    }

    @Override
    protected void processTextPosition(TextPosition text)
    {
        int angle = ExtractText.getAngle(text);
        if (angle == 0)
        {
            super.processTextPosition(text);
        }
    }
}

/**
 * Dummy output.
 */
class NullWriter extends Writer
{
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException
    {
        // do nothing
    }

    @Override
    public void flush() throws IOException
    {
        // do nothing
    }

    @Override
    public void close() throws IOException
    {
        // do nothing
    }
}
