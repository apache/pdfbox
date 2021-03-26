package org.apache.pdfbox.tools;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.File;
import java.io.IOException;

//This class will give users a method by which to draw lines on pages of a PDF document as stated in Req 3.0

public class DrawLine {
    private PDDocument documentToWrite;
    private PDPageContentStream writingStream;


//loading the document and initializing the content stream is done in this method to make testing easier and avoiding a single, dense method
    public PDDocument loadDocument(String imagePath, int page) throws IOException {
        File file = new File(imagePath);
        PDDocument doc = Loader.loadPDF(file);
        documentToWrite = doc;
        pageCheck(doc, page);
        PDPage writePage = doc.getPage(page-1);
        writingStream = new PDPageContentStream(doc, writePage, PDPageContentStream.AppendMode.APPEND, true, true);

        return doc;
    }
//draws the line on the previous loaded document using the previously created content stream
    public void draw(float x1, float y1, float x2, float y2, String outputName) throws IOException {
        writingStream.moveTo(x1, y1);
        writingStream.lineTo(x2, y2);
        writingStream.stroke();
        writingStream.close();
        documentToWrite.save(outputName);
    }

//pageCheck and addPage check if the page specified by the user exists or not and adds pages as needed. This fulfills Req 3.1
    protected void pageCheck(PDDocument doc, int page){
        int pageDifference = documentToWrite.getNumberOfPages()-page;
        if(pageDifference<0) {
            for (int i = 0; i < (pageDifference * -1); i++) {
                addPage();
            }

        }
    }

    protected void addPage() {
        PDPage newPage = new PDPage();
        documentToWrite.addPage(newPage);

    }

    public static void main (String args[]) throws IOException {
        if(args.length < 7) {
            System.out.println("Error: improper syntax");
            System.out.println("Expected: DrawLine PathToDocument x1 y1 x2 y2 SaveDestinationPath");
        } else {
            String docOrigin = args[0];
            int page = Integer.parseInt(args[1]);
            Float x1 = Float.parseFloat(args[2]);
            Float y1 = Float.parseFloat(args[3]);
            Float x2 = Float.parseFloat(args[4]);
            Float y2 = Float.parseFloat(args[5]);
            String docDest = args[6];

            DrawLine pencil = new DrawLine();
            pencil.loadDocument(docOrigin, page);
            pencil.draw(x1, y1, x2, y2, docDest);
        }
    }
}
