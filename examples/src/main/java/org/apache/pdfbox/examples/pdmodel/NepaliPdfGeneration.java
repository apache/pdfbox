package org.apache.pdfbox.examples.pdmodel;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/*
* @author Amar Dura
*
*/
public class NepaliPdfGeneration {

    public static void main(String[] args) {
        PDDocument document = null;
        PDPageContentStream contentStream = null;


        // Load the font file
        String noto = "examples/src/main/resources/org/apache/pdfbox/resources/ttf/NotoSansDevanagari.ttf";
        String kokila = "examples/src/main/resources/org/apache/pdfbox/resources/ttf/kokila.ttf";
        String nirmala = "examples/src/main/resources/org/apache/pdfbox/resources/ttf/Nirmala.ttf";

        String kalimati ="examples/src/main/resources/org/apache/pdfbox/resources/otf/KalimatiRegular.otf";
        String mangal = "examples/src/main/resources/org/apache/pdfbox/resources/otf/MangalRegular.otf";

        String adobeDevanagari = "examples/src/main/resources/org/apache/pdfbox/resources/otf/AdobeDevanagari-Regular.otf"; //CFF related problem with this font

        List<String> fonts = Arrays.asList(noto, kalimati, mangal, kokila, nirmala);
        List<String> fontNames = Arrays.asList("नोटो सान्स देवनागरी","कालिमाटी", "मङ्गल", "कोकिल", "निर्मला");

        // Text to display
        //String text ="प्राकृतिक दृश्यले मन्त्रमुग्ध अन्नपूर्ण आनन्दित संस्कृतिलाई छन्।";
        String text = "फिनल्याण्ड";

        // Define initial position
        float startX = 100;
        float startY = 750;
        float fontSize = 40;
        float leading = 1.5f * fontSize;


        try{
            document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);

            for(int i=0;i<fonts.size();i++){
                String font = fonts.get(i);
                String fontName = fontNames.get(i);

                File fontFile = new File(font);
                PDType0Font pdfFont = PDType0Font.load(document, fontFile);

                // Begin the contentStream
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.setFont(pdfFont,fontSize);
                contentStream.showText(fontName);
                contentStream.newLineAtOffset(0, -leading);
                contentStream.showText(text);

                contentStream.endText();

                // Update the startY position for the next iteration
                startY -= 3 * leading;
            }

            contentStream.close();
            document.save("examples/src/main/resources/org/apache/pdfbox/examples/nepali-pdf/nepali.pdf");
            document.close();


        }
        catch (IOException e){
            System.out.println("PROBLEM: "+e.getMessage());
        }
        }
    }

