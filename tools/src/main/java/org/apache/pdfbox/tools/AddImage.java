package org.apache.pdfbox.tools;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import java.io.File;
import java.io.IOException;

public class AddImage {
    //syntax: AddImage PDF_URL Image_URL
private PDDocument documentToWrite;
private PDPageContentStream writingStream;
private PDImageXObject imageToWrite;

public AddImage(){

}

public PDDocument loadFileandInitializeStream(String path) throws IOException {

    File file = new File(path);
    PDDocument doc = Loader.loadPDF(file);
    documentToWrite = doc;
    PDPage lastPage = doc.getPage(doc.getNumberOfPages()-1);
    writingStream = new PDPageContentStream(doc, lastPage, PDPageContentStream.AppendMode.APPEND, true, true);

return doc;
}

public PDImageXObject createImage (String path) throws IOException {

    PDImageXObject image = PDImageXObject.createFromFile(path, documentToWrite);
    imageToWrite = image;

    return image;

}


public void writeImage(String fileName) throws IOException {

    writingStream.drawImage(imageToWrite, 50, 170, 500, 500);
    writingStream.close();
    documentToWrite.save(fileName);
    documentToWrite.close();
}


public static void main(String[] args) throws IOException {
    if(args[0]== null || args[1] == null || args.length < 1) {
        System.out.println("Invalid usage: syntax is AddImage PDFpath imagePath");
        System.exit(-1);
    }
    AddImage add = new AddImage();
    String PDFUrl = args[0];
    String imageUrl = args[1];
    PDDocument doc = add.loadFileandInitializeStream(PDFUrl);
    add.createImage(imageUrl);
    add.writeImage(PDFUrl);
}


public PDDocument getDocumentToWrite(){
    return documentToWrite;
}


}

