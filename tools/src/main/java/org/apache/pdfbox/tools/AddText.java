package org.apache.pdfbox.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.apache.pdfbox.text.PDFTextStripper;

public class AddText
{
    private PDDocument documentToWrite;
    private String path;
    

    public PDDocument loadFileandInitializeStream(String path) throws IOException {

        File file = new File(path);
        PDDocument doc = Loader.loadPDF(file);
        documentToWrite = doc;

        this.path = path;
    
        return doc;
    }

    public String writeText(String annotation) throws IOException
    {
        COSDocument doc = documentToWrite.getDocument();

        String key = "";

        if (!doc.isEncrypted()) {
            
            PDFTextStripper stripper = new PDFTextStripper();
            key = stripper.getText(documentToWrite);

            key += annotation;

            key = key.replace("\n", "").replace("\r", "");

            PDPage page = documentToWrite.getPage(0);

            PDPageContentStream content_stream = new PDPageContentStream(documentToWrite, page, PDPageContentStream.AppendMode.OVERWRITE, true);
            
            content_stream.beginText();

            content_stream.setFont(PDType1Font.HELVETICA, 12);

            content_stream.newLineAtOffset( 100, 700 );

            content_stream.showText(key);

            content_stream.endText();

            content_stream.close();
        }

        documentToWrite.save(this.path);

        return key;
    }
}
