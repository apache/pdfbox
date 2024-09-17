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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.InvalidPathException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import picocli.CommandLine;


/**
 * Test suite for ExtractText. 
 */
class TestExtractText
{

    final PrintStream originalOut = System.out;
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream printStream = null;
    static final String TESTFILE1 = "src/test/resources/org/apache/pdfbox/testPDFPackage.pdf";
    static final String TESTFILE2 = "src/test/resources/org/apache/pdfbox/hello3.pdf";
    static final String TESTFILE3 = "src/test/resources/org/apache/pdfbox/AngledExample.pdf";
    static String filename1 = null;
    static String filename2 = null;

    @BeforeAll
    public static void setupFilenames()
    {
        // the filename representation is platform dependent
        filename1 = Paths.get(TESTFILE1).toString();
        filename2 = Paths.get(TESTFILE2).toString();
    }

    @BeforeEach
    public void setUpStreams()
    {
        out.reset();
        try
        {
            printStream = new PrintStream(out, true, "utf-8");
            System.setOut(printStream);
        }
        catch (UnsupportedEncodingException e)
        {
            // shouldn't happen at all
            e.printStackTrace();
        }
    }

    @AfterEach
    public void restoreStreams()
    {
        System.setOut(originalOut);
        if (printStream != null)
        {
            printStream.close();
        }
    }
    
    /**
     * Run the text extraction test using a pdf with embedded pdfs.
     * 
     * @throws Exception if something went wrong
     */
    @Test
    void testEmbeddedPDFs() throws Exception 
    {
        ExtractText app = new ExtractText();
        CommandLine cmd = new CommandLine(app);
        int exitCode = cmd.execute("-i", TESTFILE1, "-console");
        assertEquals(0, exitCode);

        String result = out.toString("UTF-8");
        assertTrue(result.contains("PDF1"));
        assertTrue(result.contains("PDF2"));
        assertFalse(result.contains("PDF file: " + filename1));
        assertFalse(result.contains("Hello"));
        assertFalse(result.contains("World."));
        assertFalse(result.contains("PDF file: " + filename2));
    }

    /**
     * Run the text extraction with -addFileName
     * 
     * @throws Exception if something went wrong
     */
    @Test
    void testAddFileName() throws Exception
    {
        ExtractText app = new ExtractText();
        CommandLine cmd = new CommandLine(app);
        int exitCode = cmd.execute("-i", TESTFILE1, "-console", "-addFileName");
        assertEquals(0, exitCode);

        String result = out.toString("UTF-8");
        assertTrue(result.contains("PDF1"));
        assertTrue(result.contains("PDF2"));
        assertTrue(result.contains("PDF file: " + filename1));
        assertFalse(result.contains("Hello"));
        assertFalse(result.contains("World."));
        assertFalse(result.contains("PDF file: " + filename2));
    }

    /**
     * Run the text extraction as a PDFBox repeatable subcommand
     * 
     * @throws Exception if something went wrong
     */
    @Test
    void testPDFBoxRepeatableSubcommand() throws Exception
    {
        PDFBox.main(new String[] { "export:text", "-i", TESTFILE1, "-console", //
                "export:text", "-i", TESTFILE2, "-console" });

        String result = out.toString("UTF-8");
        assertTrue(result.contains("PDF1"));
        assertTrue(result.contains("PDF2"));
        assertFalse(result.contains("PDF file: " + filename1));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("World."));
        assertFalse(result.contains("PDF file: " + filename2));
    }

    /**
     * Run the text extraction as a PDFBox repeatable subcommand with -addFileName
     * 
     * @throws Exception if something went wrong
     */
    @Test
    void testPDFBoxRepeatableSubcommandAddFileName() throws Exception
    {
        PDFBox.main(new String[] { "export:text", "-i", TESTFILE1, "-console", "-addFileName",
                "export:text", "-i", TESTFILE2, "-console", "-addFileName" });

        String result = out.toString("UTF-8");
        assertTrue(result.contains("PDF1"));
        assertTrue(result.contains("PDF2"));
        assertTrue(result.contains("PDF file: " + filename1));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("World."));
        assertTrue(result.contains("PDF file: " + filename2));
    }

    /**
     * Run the text extraction as a PDFBox repeatable subcommand with -addFileName, with -o <outfile> and without
     * -append
     * 
     * @throws Exception if something went wrong
     */
    @Test
    void testPDFBoxRepeatableSubcommandAddFileNameOutfile(@TempDir Path tempDir) throws Exception
    {
        Path path = null;
        try
        {
            path = tempDir.resolve("outfile.txt");
            Files.deleteIfExists(path);
        }
        catch (InvalidPathException ipe)
        {
            System.err.println(
                    "Error creating temporary test file in " + this.getClass().getSimpleName());
        }
        assertNotNull(path);

        PDFBox.main(new String[] { "export:text", "-i", TESTFILE1, "-encoding", "UTF-8",
                "-addFileName", "-o", path.toString(), //
                "export:text", "-i", TESTFILE2, "-encoding", "UTF-8", //
                "-addFileName", "-o", path.toString() });

        String result = new String(Files.readAllBytes(path), "UTF-8");
        assertFalse(result.contains("PDF1"));
        assertFalse(result.contains("PDF2"));
        assertFalse(result.contains("PDF file: " + filename1));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("World."));
        assertTrue(result.contains("PDF file: " + filename2));
    }

    /**
     * Run the text extraction as a PDFBox repeatable subcommand with -addFileName, -o <outfile> and -append
     * 
     * @throws Exception if something went wrong
     */
    @Test
    void testPDFBoxRepeatableSubcommandAddFileNameOutfileAppend(@TempDir Path tempDir)
            throws Exception
    {
        Path path = null;

        try 
        {
            path = tempDir.resolve("outfile.txt");
            Files.deleteIfExists(path);
        }
        catch (InvalidPathException ipe)
        {
            System.err.println(
                    "Error creating temporary test file in " + this.getClass().getSimpleName());
        }
        assertNotNull(path);

        PDFBox.main(new String[] { "export:text", "-i", TESTFILE1, "-encoding", "UTF-8",
                "-addFileName", "-o", path.toString(), //
                "export:text", "-i", TESTFILE2, "-encoding", "UTF-8",
                "-addFileName", "-o", path.toString(), "-append" });

        String result = new String(Files.readAllBytes(path), "UTF-8");
        assertTrue(result.contains("PDF1"));
        assertTrue(result.contains("PDF2"));
        assertTrue(result.contains("PDF file: " + filename1));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("World."));
        assertTrue(result.contains("PDF file: " + filename2));
    }

    /**
     * Simple test to check that the rotationMagic feature works.
     *
     * @param tempDir
     * @throws Exception 
     */
    @Test
    void testRotationMagic(@TempDir Path tempDir) throws Exception
    {
        Path path = null;

        try 
        {
            path = tempDir.resolve("outfile.txt");
            Files.deleteIfExists(path);
        }
        catch (InvalidPathException ipe)
        {
            System.err.println(
                    "Error creating temporary test file in " + this.getClass().getSimpleName());
        }
        assertNotNull(path);

        PDFBox.main(new String[] { "export:text", "-rotationMagic", "-i", TESTFILE3,
            "-o", path.toString() });

        String result = new String(Files.readAllBytes(path), "UTF-8");
        assertTrue(result.contains("Horizontal Text"));
        assertTrue(result.contains("Vertical Text"));
    }

}
