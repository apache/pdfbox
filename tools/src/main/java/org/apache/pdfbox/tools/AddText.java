package org.apache.pdfbox.tools;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;

public class AddText
{
    private PDDocument documentToWrite;
    private PDPageContentStream writingStream;
    

    public PDDocument loadFileandInitializeStream(String path) throws IOException {

        File file = new File(path);
        PDDocument doc = Loader.loadPDF(file);
        documentToWrite = doc;
        PDPage lastPage = doc.getPage(doc.getNumberOfPages()-1);
        writingStream = new PDPageContentStream(doc, lastPage, PDPageContentStream.AppendMode.APPEND, true, true);
    
        return doc;
    }

    public String writeText(String annotation) throws IOException
    {
        writingStream.beginText();

        PDPage page = documentToWrite.getPage(0);

        PDAnnotation annot = new PDAnnotationText();

        annot.setContents(annotation);

        List<PDAnnotation> annotation_list = page.getAnnotations();
        annotation_list.add(annot);

        page.setAnnotations(annotation_list);

        return page.getAnnotations().get(page.getAnnotations().size()-1).getContents();
    }
}
