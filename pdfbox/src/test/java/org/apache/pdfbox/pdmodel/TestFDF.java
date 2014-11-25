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
package org.apache.pdfbox.pdmodel;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.fdf.FDFDocument;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceEntry;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDFieldTreeNode;
import org.apache.pdfbox.pdmodel.interactive.form.PDRadioButton;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

/**
 * This will test the FDF algorithms in PDFBox.
 *
 * @author Ben Litchfield
 * 
 */
public class TestFDF extends TestCase
{

    private static final String PDF_FDEB = "target/test-input-ext/fdeb.pdf";
    private static final String PDF_LOTSOFFIELDS = "target/test-input-ext/pdf_with_lots_of_fields.pdf";
    private static final String PDF_FREEDOM = "target/test-input-ext/FreedomExpressions.pdf";
    private static final String FDF_FREEDOM = "target/test-input-ext/FreedomExpressions.fdf";

    /**
     * Constructor.
     *
     * @param name The name of the test to run.
     */
    public TestFDF( String name )
    {
        super( name );
    }

    /**
     * This will get the suite of test that this class holds.
     *
     * @return All of the tests that this class holds.
     */
    public static Test suite()
    {
        return new TestSuite( TestFDF.class );
    }

    /**
     * infamous main method.
     *
     * @param args The command line arguments.
     */
    public static void main( String[] args )
    {
        String[] arg = {TestFDF.class.getName() };
        junit.textui.TestRunner.main( arg );
    }

    /**
     * This will test some simple field setting.
     *
     * @throws Exception If there is an exception while encrypting.
     */
    public void testFDFfdeb() throws Exception
    {
        
        File filePDF = new File(PDF_FDEB); 
        if ( filePDF.exists() )
        {
            PDDocument fdeb = null;
            try
            {
                fdeb = PDDocument.load( filePDF );
                PDAcroForm form = fdeb.getDocumentCatalog().getAcroForm();
                PDTextField field = (PDTextField)form.getField( "f67_1" );
                field.setValue( "2" );
    
                String expected =
                    "/Tx BMC " +
                    "BT " +
                    "/Helv 9 Tf " +
                    " 0 g " +
                    " 2 1.985585 Td " +
                    "2.07698 0 Td " +
                    "(2) Tj " +
                    "ET " +
                    "EMC";
    
                testContentStreams( fdeb, field, expected );
            }
            finally
            {
                if( fdeb != null )
                {
                    fdeb.close();
                }
            }
        }
    }

    /**
     * This will test a pdf with lots of fields.
     *
     * @throws Exception If there is an exception while encrypting.
     */
    public void testFDFPDFWithLotsOfFields() throws Exception
    {
        File filePDF = new File(PDF_LOTSOFFIELDS); 
        if ( filePDF.exists() )
        {
            PDDocument fdeb = null;
            try
            {
                fdeb = PDDocument.load( filePDF );
                PDAcroForm form = fdeb.getDocumentCatalog().getAcroForm();
                PDTextField feld2 = (PDTextField)form.getField( "Feld.2" );
                feld2.setValue( "Benjamin" );
    
                String expected =
                "1 1 0.8000000119 rg " +
                " 0 0 127.5 19.8299999237 re " +
                " f " +
                " 0 0 0 RG " +
                " 1 w " +
                " 0.5 0.5 126.5 18.8299999237 re " +
                " S " +
                " 0.5 g " +
                " 1 1 m " +
                " 1 18.8299999237 l " +
                " 126.5 18.8299999237 l " +
                " 125.5 17.8299999237 l " +
                " 2 17.8299999237 l " +
                " 2 2 l " +
                " 1 1 l " +
                " f " +
                " 0.75 g " +
                " 1 1 m " +
                " 126.5 1 l " +
                " 126.5 18.8299999237 l " +
                " 125.5 17.8299999237 l " +
                " 125.5 2 l " +
                " 2 2 l " +
                " 1 1 l " +
                " f " +
                " /Tx BMC  " +
                "BT " +
                "/Helv 14 Tf " +
                " 0 0 0 rg " +
                " 4 4.721 Td " +
                "(Benjamin) Tj " +
                "ET " +
                "EMC";
    
                testContentStreams( fdeb, feld2, expected );
    
                PDRadioButton feld3 = (PDRadioButton)form.getField( "Feld.3" );
                feld3.setValue(COSName.getPDFName("RB1"));
                assertEquals( "RB1", feld3.getValue().getName() );
            }
            finally
            {
                if( fdeb != null )
                {
                    fdeb.close();
                }
            }
        }
    }

    /**
     * This will test the Freedom pdf.
     *
     * @throws Exception If there is an error while testing.
     */
    public void testFDFFreedomExpressions() throws Exception
    {
        File filePDF = new File(PDF_FREEDOM); 
        File fileFDF = new File(FDF_FREEDOM); 
        if (filePDF.exists() && fileFDF.exists())
        {
            PDDocument freedom = null;
            FDFDocument fdf = null;
            try
            {
                freedom = PDDocument.load( filePDF );
                fdf = FDFDocument.load( fileFDF );
                PDAcroForm form = freedom.getDocumentCatalog().getAcroForm();
                form.importFDF( fdf );
                PDTextField feld2 = (PDTextField)form.getField( "eeFirstName" );
                List<COSObjectable> kids = feld2.getKids();
                PDFieldTreeNode firstKid = (PDFieldTreeNode)kids.get( 0 );
                PDFieldTreeNode secondKid = (PDFieldTreeNode)kids.get( 1 );
                testContentStreamContains( freedom, firstKid, "Steve" );
                testContentStreamContains( freedom, secondKid, "Steve" );
    
                //the appearance stream is suppose to be null because there
                //is an F action in the AA dictionary that populates that field.
                PDFieldTreeNode totalAmt = form.getField( "eeSuppTotalAmt" );
                assertTrue( totalAmt.getDictionary().getDictionaryObject( COSName.AP ) == null );
    
            }
            finally
            {
                if( freedom != null )
                {
                    freedom.close();
                }
                if( fdf != null )
                {
                    fdf.close();
                }
            }
        }
    }

    private void testContentStreamContains( PDDocument doc, PDFieldTreeNode field, String expected ) throws Exception
    {
        PDAnnotationWidget widget = field.getWidget();
        PDAppearanceEntry normalAppearance = widget.getAppearance().getNormalAppearance();
        PDAppearanceStream appearanceStream = normalAppearance.getAppearanceStream();
        COSStream actual = appearanceStream.getCOSStream();

        List<Object> actualTokens = getStreamTokens( doc, actual );
        assertTrue( actualTokens.contains( new COSString( expected ) ) );
    }

    private void testContentStreams( PDDocument doc, PDFieldTreeNode field, String expected ) throws Exception
    {
        PDAnnotationWidget widget = field.getWidget();
        PDAppearanceEntry normalAppearance = widget.getAppearance().getNormalAppearance();
        PDAppearanceStream appearanceStream = normalAppearance.getAppearanceStream();
        COSStream actual = appearanceStream.getCOSStream();

        List<Object> actualTokens = getStreamTokens( doc, actual );
        List<Object> expectedTokens = getStreamTokens( doc, expected );
        assertEquals( actualTokens.size(), expectedTokens.size() );
        for( int i=0; i<actualTokens.size(); i++ )
        {
            Object actualToken = actualTokens.get( i );
            Object expectedToken = expectedTokens.get( i );
            assertEquals( actualToken, expectedToken );
        }
    }

    private List<Object> getStreamTokens( PDDocument doc, String string ) throws IOException
    {
        PDFStreamParser parser;

        List<Object> tokens = null;
        if( string != null )
        {
            ByteArrayInputStream stream = new ByteArrayInputStream( string.getBytes() );
            parser = new PDFStreamParser( stream );
            parser.parse();
            tokens = parser.getTokens();
        }
        return tokens;
    }

    private List<Object> getStreamTokens( PDDocument doc, COSStream stream ) throws IOException
    {
        PDFStreamParser parser;

        List<Object> tokens = null;
        if( stream != null )
        {
            parser = new PDFStreamParser( stream );
            parser.parse();
            tokens = parser.getTokens();
        }
        return tokens;
    }
}
