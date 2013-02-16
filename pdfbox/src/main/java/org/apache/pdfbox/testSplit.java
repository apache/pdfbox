package org.apache.pdfbox;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.Splitter;

public class testSplit
{

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException, COSVisitorException
    {
        String pdfFile = "/home/lehmi/workspace/pdfs/splitter/HugePagesWhenSplit.pdf";
        PDDocument document = PDDocument.load(pdfFile);
        List<PDDocument> documents =  new Splitter().split( document );
        for( int i=0; i<documents.size(); i++ )
        {
            PDDocument doc = documents.get( i );
            String fileName = pdfFile.substring(0, pdfFile.length()-4 ) + "-" + i + ".pdf";
            writeDocument( doc, fileName );
            System.out.println(fileName);
            doc.close();
        }

    }

    private static void writeDocument(PDDocument doc, String onePage) throws IOException, COSVisitorException
    {
        final FileOutputStream output = new FileOutputStream(onePage);
        final COSWriter writer = new COSWriter(output);
        writer.write(doc);
        output.close();
        writer.close();
    }
}
