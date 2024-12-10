package org.apache.pdfbox.examples.pdmodel;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
* @author Amar Dura
*
*/
public class NepaliPdfGeneration {

    public static void main(String[] args) {
        PDDocument document;
        PDPageContentStream contentStream;


        // Load the font file
        List<String> fonts = getStrings();

        List<String> fontNames = Arrays.asList("नोटो सान्स देवनागरी","कालिमाटी", "मङ्गल", "कोकिल", "निर्मला");
//        List<String> fontNames = Arrays.asList("कोकिल");

        // Text to display
        //String text ="प्राकृतिक दृश्यले मन्त्रमुग्ध अन्नपूर्ण आनन्दित संस्कृतिलाई छन्।";
//        String text ="Dura नेपाली भाषाको लेखन प्रणाली देवनागरी लिपिमा आधारित छ।" +
//                "\"क्ष\", \"त्र\", \"ज्ञ\" जस्ता जटिल संयुक्ताक्षरहरू प्रचुर छन्।" +
//                "यो लिपि स्वर, व्यञ्जन, र संयुक्ताक्षरहरूको संयोजनमा समृद्ध छ।" +
//                "\"श्र\", \"द्य\", \"क्ष्म\" जस्ता अक्षरहरूले यसको जटिलता झल्काउँछन्।";
//        String text = " र्क र्का र्कि र्की र्के र्कै र्को र्कौ र्कँ र्न्थ्यि र्थ्यो";
String text ="र्थ्यो सङ्क्षिप्त ड्कि छन्";
//        String text ="छन्";
        // Define initial position
        float startX = 50;
        float startY = 700;
        float fontSize = 24;
        float leading = 1.5f * fontSize;


        try{
            document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);

            for (int i = 0; i < fonts.size(); i++) {
                String font = fonts.get(i);
                String fontName = fontNames.get(i);

                File fontFile = new File(font);
                PDType0Font pdfFont = PDType0Font.load(document, fontFile);

                // Begin the contentStream
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.setFont(pdfFont, fontSize);

                // Show font name
                contentStream.showText(fontName);
                // Move down for spacing (e.g., 2x the leading)
                contentStream.newLineAtOffset(0, -2 * leading);

                // Split and show the text
                List<String> wrappedText = wrapText(text, pdfFont, fontSize, page.getMediaBox().getWidth() - 2 * startX);
                for (String line : wrappedText) {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -leading);
                }

                contentStream.endText();

                // Update the startY position for the next font block
                startY -= wrappedText.size() * leading + 3 * leading; // Extra gap between font blocks
            }


            contentStream.close();
            document.save("examples/src/main/resources/org/apache/pdfbox/examples/nepali-pdf/nepali.pdf");
            System.out.println("PDF Created Successfully.");
            document.close();


        }
        catch (IOException e){
            System.out.println("PROBLEM: "+e.getMessage());
        }
        }

    private static List<String> getStrings() {
        String noto = "examples/src/main/resources/org/apache/pdfbox/resources/ttf/NotoSansDevanagari.ttf";
        String kokila = "examples/src/main/resources/org/apache/pdfbox/resources/ttf/kokila.ttf";
        String nirmala = "examples/src/main/resources/org/apache/pdfbox/resources/ttf/Nirmala.ttf";

        String kalimati ="examples/src/main/resources/org/apache/pdfbox/resources/otf/KalimatiRegular.otf";
        String mangal = "examples/src/main/resources/org/apache/pdfbox/resources/otf/MangalRegular.otf";

        String adobeDevanagari = "examples/src/main/resources/org/apache/pdfbox/resources/otf/AdobeDevanagari-Regular.otf"; //CFF related problem with this font

        return Arrays.asList(noto, kalimati, mangal, kokila, nirmala);
//        return Arrays.asList(kokila);

    }

    private static List<String> wrapText(String text, PDType0Font font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            float textWidth = font.getStringWidth(testLine) / 1000 * fontSize;

            if (textWidth > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine.append(currentLine.length() == 0 ? word : " " + word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
}

