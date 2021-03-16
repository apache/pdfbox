package org.apache.pdfbox.tools;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.tools.AddImage;
import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class TestAddText extends TestCase
{
    
    @Test
    public void testLoadFileAndInitializeStream() throws IOException
    {
        AddText add = new AddText();
        
        String expected = "../tools/src/main/java/org/apache/pdfbox/tools/AddImageResources/examplePDFWithText.pdf";

        PDDocument actual = add.loadFileandInitializeStream(expected);
        int actualPages = actual.getNumberOfPages();
        assertEquals(1, actualPages);
    }

    @Test
    public void testWriteText()
    {
        AddText add = new AddText();
        
        String expected = "../tools/src/main/java/org/apache/pdfbox/tools/AddImageResources/examplePDFWithText.pdf";


        try
        {
            PDDocument actual = add.loadFileandInitializeStream(expected);
            String str = add.writeText("annotation");
            
            actual.getPage(actual.getNumberOfPages()-1);

            
            assertEquals(actual.getPage(0).getAnnotations().get(actual.getPage(0).getAnnotations().size()-1).getContents(), str);
        }
        catch (IOException e)
        {
            assertEquals(1, 0);
        }

    }
}
