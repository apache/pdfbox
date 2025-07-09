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
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
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

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * This is the main program that simply parses the pdf document and transforms it
 * into text.
 *
 * @author Ben Litchfield
 * @author Tilman Hausherr
 */
@Command(name = "extracttext", header = "Extracts the text from a PDF document", versionProvider = Version.class, mixinStandardHelpOptions = true)
public final class ExtractText  implements Callable<Integer>
{
    private static final Logger LOG = LogManager.getLogger(ExtractText.class);

    private static final String STD_ENCODING = "UTF-8";

    // Expected for CLI app to write to System.out/System.err
    @SuppressWarnings("squid:S106")
    private final PrintStream SYSOUT;
    @SuppressWarnings("squid:S106")
    private final PrintStream SYSERR;

    @Option(names = "-alwaysNext", description = "Process next page (if applicable) despite IOException " + 
        "(ignored when -html)")
    private boolean alwaysNext = false;

    @Option(names = "-console", description = "Send text to console instead of file")
    private boolean toConsole = false;

    @Option(names = "-debug", description = "Enables debug output about the time consumption of every stage")
    private boolean debug = false;

    @Option(names = "-encoding", description = "UTF-8 or ISO-8859-1, UTF-16BE, UTF-16LE, etc. (default: ${DEFAULT-VALUE})")
    private String encoding = STD_ENCODING;

    @Option(names = "-endPage", description = "The last page to extract (1 based, inclusive)")
    private int endPage = Integer.MAX_VALUE;

    @Option(names = "-html", description = "Output in HTML format instead of raw text")
    private boolean toHTML = false;

    @Option(names = "-md", description = "Output in Markdown format instead of raw text")
    private boolean toMD = false;

    @Option(names = "-ignoreBeads", description = "Disables the separation by beads")
    private boolean ignoreBeads = false;

    @Option(names = "-password", description = "the password for the PDF or certificate in keystore.", arity = "0..1", interactive = true)    
    private String password = "";

    @Option(names = "-rotationMagic", description = "Analyze each page for rotated/skewed text, rotate to 0° " +
        "and extract separately (slower, and ignored when -html)" )
    private boolean rotationMagic = false;

    @Option(names = "-sort", description = "Sort the text before writing of every stage")
    private boolean sort = false;

    @Option(names = "-startPage", description = "The first page to start extraction (1 based)")
    private int startPage = 1;

    @Option(names = {"-i", "--input"}, description = "the PDF file", required = true)
    private File infile;

    @Option(names = {"-o", "--output"}, description = "the exported text file")
    private File outfile;

    @Option(names = "-addFileName", description = "Print PDF file name to the output text")
    private boolean addFileName = false;

    @Option(names = "-append", description = "Use append mode for output file")
    private boolean append = false;

    /**
     * Constructor.
     */
    public ExtractText()
    {
        SYSOUT = System.out;
        SYSERR = System.err;
    }

    /**
     * Infamous main method.
     *
     * @param args Command line arguments, should be one and a reference to a file.
     */
    public static void main( String[] args )
    {
        // suppress the Dock icon on OS X
        System.setProperty("apple.awt.UIElement", "true");

        int exitCode = new CommandLine(new ExtractText()).execute(args);
        System.exit(exitCode);
    }

    /**
     * Starts the text extraction.
     *  
     */
    public Integer call()
    {
        // set file extension
        if (toHTML && toMD)
        {
            SYSERR.println( "You can't set md and html at the same time");
            return 1;
        }
        String ext = toHTML ? ".html" : ".txt";
        ext = toMD ? ".md" : ext;

        if (outfile == null)
        {
            String outPath = FilenameUtils.removeExtension(infile.getAbsolutePath()) + ext;
            outfile = new File(outPath);
        }

        if (toHTML && !STD_ENCODING.equals(encoding))
        {
            encoding = STD_ENCODING;
            SYSOUT.println("The encoding parameter is ignored when writing html output.");
        }

        if (toConsole && encoding != null)
        {
            SYSOUT.println("The encoding parameter is ignored when writing to the console.");
        }

        try (PDDocument document = Loader.loadPDF(infile, password);
                Writer output = createOutputWriter())
        {
            long startTime = startProcessing("Loading PDF " + infile);

            AccessPermission ap = document.getCurrentAccessPermission();
            if( ! ap.canExtractContent() )
            {
                SYSERR.println( "You do not have permission to extract text");
                return 1;
            }
            
            stopProcessing("Time for loading: ", startTime);

            startTime = startProcessing("Starting text extraction");

            if (addFileName)
            {
                output.write("PDF file: " + infile);
                output.write(System.lineSeparator());
            }

            if (debug)
            {
                SYSERR.println("Writing to " + outfile.getAbsolutePath());
            }

            PDFTextStripper stripper;
            if(toHTML)
            {
                // HTML stripper can't work page by page because of startDocument() callback
                stripper = new PDFText2HTML();
                stripper.setSortByPosition(sort);
                stripper.setShouldSeparateByBeads(!ignoreBeads);
                stripper.setStartPage(startPage);
                stripper.setEndPage(endPage);

                // Extract text for main document:
                stripper.writeText(document, output);
            }
            else
            {
                if (toMD)
                {
                    if (rotationMagic)
                    {
                        stripper = new FilteredText2Markdown();
                    }
                    else
                    {
                        stripper = new PDFText2Markdown();
                    }
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
                }
                stripper.setSortByPosition(sort);
                stripper.setShouldSeparateByBeads(!ignoreBeads);

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
                                SYSERR.println("Processing embedded file " + ent.getKey() + ":");
                            }
                            PDComplexFileSpecification spec = ent.getValue();
                            PDEmbeddedFile file = spec.getEmbeddedFile();
                            if (file != null && "application/pdf".equals(file.getSubtype()))
                            {
                                if (debug)
                                {
                                    SYSERR.println("  is PDF (size=" + file.getSize() + ")");
                                }
                                try (PDDocument subDoc = Loader.loadPDF(RandomAccessReadBuffer
                                                .createBufferFromStream(file.createInputStream())))
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
            output.flush();
            stopProcessing("Time for extraction: ", startTime);
        }
        catch (IOException ioe)
        {
            SYSERR.println( "Error extracting text for document [" + ioe.getClass().getSimpleName() + "]: " + ioe.getMessage());
            return 4;
        }

        return 0;
    }

    private Writer createOutputWriter() throws IOException
    {
        if (toConsole)
        {
            return new PrintWriter(SYSOUT)
            {
                @Override
                public void close()
                {
                    // don't close the console
                }
            };
        }
        else
        {
            return new OutputStreamWriter(new FileOutputStream(outfile, append), encoding);
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
                        page.getCOSObject().getCOSArray(COSName.CONTENTS).remove(0);
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
            SYSERR.println(message);
        }
        return System.currentTimeMillis();
    }
    
    private void stopProcessing(String message, long startTime) 
    {
        if (debug)
        {
            long stopTime = System.currentTimeMillis();
            float elapsedTime = ((float)(stopTime - startTime))/1000;
            SYSERR.println(message + elapsedTime + " seconds");
        }
    }

    static int getAngle(TextPosition text)
    {
        // should this become a part of TextPosition?
        Matrix m = text.getTextMatrix().clone();
        m.concatenate(text.getFont().getFontMatrix());
        return (int) Math.round(Math.toDegrees(Math.atan2(m.getShearY(), m.getScaleY())));
    }
}

/**
 * Collect all angles while doing text extraction. Angles are in degrees and rounded to the closest
 * integer (to avoid slight differences from floating point arithmetic resulting in similarly
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
 * PDFText2Markdown that only processes glyphs that have angle 0.
 */
class FilteredText2Markdown extends PDFText2Markdown
{
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
