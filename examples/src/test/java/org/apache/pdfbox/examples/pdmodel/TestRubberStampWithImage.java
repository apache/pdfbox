package org.apache.pdfbox.examples.pdmodel;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

/**
 * Test for RubberStampWithImage
 */
public class TestRubberStampWithImage extends TestCase
{
    public void test() throws IOException
    {
        // TODO get class package automatically?

        String documentFile = "src/test/resources/org.apache.pdfbox.examples.pdmodel/document.pdf";
        String stampFile = "src/test/resources/org.apache.pdfbox.examples.pdmodel/stamp.jpg";
        String outFile = "target/test-output/TestRubberStampWithImage.pdf";

        new File("target/test-output").mkdirs();    // TODO auto?

        String[] args = new String[] { documentFile, outFile, stampFile };
        RubberStampWithImage rubberStamp = new RubberStampWithImage();
        rubberStamp.doIt(args);
    }
}
