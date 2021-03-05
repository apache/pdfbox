package org.apache.pdfbox.tools;

import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class PDFWithTimeNewRomanText {

    protected static void main(String[] args)throws IOException {


        // Create a document and add a page to it
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage( page );

        // Create a new font object selecting one of the PDF base fonts, this is the Time New Roman group.
        PDFont font = PDType1Font.HELVETICA_BOLD;

        // Start a new content stream which will "hold" the to be created content
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        // Define a text content stream using the selected font, and print the text
        contentStream.beginText();
        contentStream.setFont( font, 28 );
        contentStream.newLineAtOffset( 100, 700 );
        contentStream.showText( "Hello World !!!!" );
        contentStream.endText();

        System.out.println("Text Content is added in the PDF Document.");

        //  closed the content stream class.
        contentStream.close();

        // Save the results and ensure that the document is properly closed.
        document.save( "Hello World.pdf");
        document.close();
    }
}
