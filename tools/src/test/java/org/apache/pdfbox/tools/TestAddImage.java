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

public class TestAddImage extends TestCase {

@Test
public void testLoadFileAndInitializeStream() throws IOException {

    AddImage add = new AddImage();
    String expected = "../tools/src/main/java/org/apache/pdfbox/tools/AddImageResources/examplePDFWithText.pdf";
    PDDocument actual = add.loadFileandInitializeStream(expected);
    int actualPages = actual.getNumberOfPages();
    assertEquals(1, actualPages);
}

@Test
public void testCreateImage() throws IOException {

    int expected = 750;
    AddImage add = new AddImage();
    add.loadFileandInitializeStream("../tools/src/main/java/org/apache/pdfbox/tools/AddImageResources/examplePDFWithText.pdf");
    String imageString = "../tools/src/main/java/org/apache/pdfbox/tools/AddImageResources/pamcamke.jpg";
    PDImageXObject actual = add.createImage(imageString);
    assertEquals(750, actual.getWidth());

    }

    @Test
    public void testMain() throws IOException {
        AddImage add = new AddImage();
        add.loadFileandInitializeStream("../tools/src/main/java/org/apache/pdfbox/tools/AddImageResources/examplePDFWithText.pdf");
        String imageString = "../tools/src/main/java/org/apache/pdfbox/tools/AddImageResources/pamcamke.jpg";
        PDImageXObject actual = add.createImage(imageString);
        add.writeImage("../tools/src/main/java/org/apache/pdfbox/tools/AddImageResources/examplePDFWithTextAndImage.pdf");
        File madeFile = new File("../tools/src/main/java/org/apache/pdfbox/tools/AddImageResources/examplePDFWithTextAndImage.pdf");
        assertTrue(madeFile.exists());
    }


    public static void main( String[] args )
    {
        String[] arg = {AddImage.class.getName() };
        junit.textui.TestRunner.main( arg );
    }
}
