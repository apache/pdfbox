package org.apache.pdfbox.multipdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Splitter.
 *
 * @author Valery bokov
 */
public class SplitterTest
{
    private int expectedPageNumber = 1;

    static PDDocument createPDFWith2Pages() throws IOException
    {
        PDDocument document = new PDDocument();

        PDPage page = new PDPage();
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        /////
        String text = "some text";
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(100,750);
        contentStream.showText(text);
        contentStream.endText();

        contentStream.close();

        /////

        document.addPage(page);

        page = new PDPage();
        contentStream = new PDPageContentStream(document, page);

        /////
        String text2 = "another text";
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ITALIC), 16);
        contentStream.beginText();
        contentStream.newLineAtOffset(200,750);
        contentStream.showText(text2);
        contentStream.endText();

        contentStream.close();

        /////

        document.addPage(page);

        return document;
    }

    @Test
    void splitDocumentUsing_ObjIntConsumer() throws IOException
    {
        try(PDDocument doc = createPDFWith2Pages())
        {
            Splitter sp = new Splitter();
            sp.split(doc,
                    (child, pagenumber) ->
                    {
                        try
                        {
                            child.close();
                        } catch (IOException e)
                        {
                            fail();
                        }

                        if (expectedPageNumber != pagenumber)
                            System.out.println("splitDocumentUsing_ObjIntConsumer page numbers. Expected: " + expectedPageNumber + ". Current: " + pagenumber);

                        assertEquals(expectedPageNumber, pagenumber);
                        ++expectedPageNumber;
                    });
        }
    }
}
