package org.lehmi.linuxmagazin;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.util.*;
import java.io.*;

public class ExportText
{

    public static void main(String[] args) throws Exception
    {
        PDDocument document = null;
        Writer output = null;
        try {
            document = PDDocument.load(args[0]);
            output = new OutputStreamWriter(System.out);
            PDFTextStripper stripper = new PDFTextStripper("UTF-8");
            stripper.setSortByPosition( true );
            stripper.setStartPage( 1 );
            stripper.setEndPage( document.getNumberOfPages() );
            stripper.writeText( document, output );
        }
        catch (IOException exception) {
            
        }
        finally {
            if (output != null)
                output.close();
            if (document != null)
                document.close();
        }
    }
}
