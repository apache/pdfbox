package org.lehmi.linuxmagazin;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.util.*;
import java.awt.Rectangle;
import java.io.*;
import java.util.List;

public class ExportTextByArea
{

    public static void main(String[] args) throws Exception
    {
        PDDocument document = null;
        try {
            document = PDDocument.load(args[0]);
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition( true );
            Rectangle region = new Rectangle( 10, 280, 275, 60 );
            stripper.addRegion( "region1", region );
            List<PDPage> allPages = document.getDocumentCatalog().getAllPages();
            stripper.extractRegions( allPages.get( 0 ) );
            System.out.println(stripper.getTextForRegion( "region1" ));
        }
        catch (IOException exception) {
            
        }
        finally {
            if (document != null)
                document.close();
        }
    }
}
