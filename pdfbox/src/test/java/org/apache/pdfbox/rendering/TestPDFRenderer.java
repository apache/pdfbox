package org.apache.pdfbox.rendering;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;

public class TestPDFRenderer {

    public static final String type4_shading_pdf = "minimal.pdf";

    @Test
    public void testGetTotalFilingSize() throws Exception {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        String filePath = classloader.getResource(type4_shading_pdf).getFile();
        File inputFile = new File(filePath);


        TestPDFToImage.doTestFile(inputFile, "src/test/resources/input/rendering", "target/test-output/rendering");

    }
}
