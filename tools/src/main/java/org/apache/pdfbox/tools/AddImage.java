package org.apache.pdfbox.tools;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import java.io.File;
import java.io.IOException;
//This class shall annotate a pdf with an image, fulfilling req 4.0
public class AddImage {

    private PDDocument documentToWrite;
    private PDPageContentStream writingStream;
    private PDImageXObject imageToWrite;

    public AddImage() {

    }
    //loading the document and initializing the content stream is done in this method to make testing easier and avoiding a single, dense method
    public PDDocument loadFileandInitializeStream(String path, int page) throws IOException {

        File file = new File(path);
        PDDocument doc = Loader.loadPDF(file);
        documentToWrite = doc;
        pageCheck(doc, page);
        PDPage writePage = doc.getPage(page-1);
        writingStream = new PDPageContentStream(doc, writePage, PDPageContentStream.AppendMode.APPEND, true, true);

        return doc;
    }

    public PDImageXObject createImage(String path) throws IOException {

        PDImageXObject image = PDImageXObject.createFromFile(path, documentToWrite);
        imageToWrite = image;

        return image;

    }

//draws the image onto the PDF
    public void writeImage(String fileName) throws IOException {
        writingStream.drawImage(imageToWrite, 50, 170, 500, 500);
        writingStream.close();
        documentToWrite.save(fileName);
        documentToWrite.close();
    }


    public static void main(String[] args) throws IOException {
        if (args[0] == null || args[1] == null || args.length < 1) {
            System.out.println("Invalid usage: syntax is AddImage PDFpath imagePath");
            System.exit(-1);
        }
        AddImage add = new AddImage();
        String PDFUrl = args[0];
        String imageUrl = args[1];
        int page = Integer.parseInt(args[2]);
        PDDocument doc = add.loadFileandInitializeStream(PDFUrl, page);
        add.createImage(imageUrl);
        add.writeImage(PDFUrl);
    }


    public PDDocument getDocumentToWrite() {
        return documentToWrite;
    }

    //this method along with addPage() check to see if the requested page is out of bounds for the document
    //and adds pages as needed.
    //This fulfills requirement 4.1
    protected void pageCheck(PDDocument doc, int page){
        int pageDifference = documentToWrite.getNumberOfPages()-page;
        if(pageDifference<0){
            for(int i = 0; i<(pageDifference*-1); i++) {
                addPage();
            }
        }
    }

    protected void addPage() {
        PDPage newPage = new PDPage();
        documentToWrite.addPage(newPage);

    }

}