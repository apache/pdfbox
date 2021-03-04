package org.apache.pdfbox.tools.imageio;
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
    String expected = "../../../../../app/src/main/appended-resources/examplePDFWithText.pdf";
    PDDocument actual = add.loadFileandInitializeStream(expected);
    int actualPages = actual.getNumberOfPages();
    assertEquals(2, actualPages);
}

@Test
public void testCreateImage() throws IOException {

    int expected = 750;
    AddImage add = new AddImage();
    add.loadFileandInitializeStream("../../../../../app/src/main/appended-resources/examplePDFWithText.pdf");
    String imageString = "../../../../../app/src/main/appended-resources/pamcamke.jpg";
    PDImageXObject actual = add.createImage(imageString);
    assertEquals(750, actual.getWidth());

    }

    public static void main( String[] args )
    {
        String[] arg = {AddImage.class.getName() };
        junit.textui.TestRunner.main( arg );
    }
}
