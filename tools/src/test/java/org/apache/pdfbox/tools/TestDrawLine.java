package org.apache.pdfbox.tools;
import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
//Test class for the DrawLine class
public class TestDrawLine {
@Test
    public void testLoadDocumentAndPageCheck() throws IOException {
    DrawLine draw = new DrawLine();
    PDDocument actual = draw.loadDocument("../tools/src/main/java/org/apache/pdfbox/tools/DrawLineResources/examplePDFWithText.pdf", 2);
    assertEquals(2, actual.getNumberOfPages()); //document should have 2 pages now
    //this also tests that the pageCheck properly adds pages
}

@Test
    public void testDrawLine() throws IOException {
    DrawLine draw = new DrawLine();
    PDDocument actual = draw.loadDocument("../tools/src/main/java/org/apache/pdfbox/tools/DrawLineResources/examplePDFWithText.pdf", 2);
    draw.draw(50, 50,100,1000,"../tools/src/main/java/org/apache/pdfbox/tools/DrawLineResources/examplePDFWithTextAndLine.pdf");
    File output = new File("../tools/src/main/java/org/apache/pdfbox/tools/DrawLineResources/examplePDFWithTextAndLine.pdf");
    assertTrue(output.exists()); //must manually check output file for line
}

}
