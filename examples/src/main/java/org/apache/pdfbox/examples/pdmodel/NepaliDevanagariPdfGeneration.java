package org.apache.pdfbox.examples.pdmodel;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.IOException;
import java.util.*;

/*
 * @author Amar Dura
 *
 */
public class NepaliDevanagariPdfGeneration {

    public static void main(String[] args) {
        PDDocument document;
        PDPageContentStream contentStream;
        String text = getStringText();
        // Load the font file
        Map<String,String> fonts = getFontMap();

        // Define initial position
        float startX = 50;
        float startY = 700;
        float fontSize = 12;
        float leading = 1.5f * fontSize;


        try{
            document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            List<String> fontNames = new ArrayList<>(fonts.keySet());
            for (int i = 0; i < fonts.size(); i++) {
                String fontName = fontNames.get(i);
                String fontPath = fonts.get(fontName);

                File fontFile = new File(fontPath);
                PDType0Font pdfFont = PDType0Font.load(document, fontFile);

                // Begin the contentStream
                contentStream.beginText();
                contentStream.newLineAtOffset(startX, startY);
                contentStream.setFont(pdfFont, fontSize);

                // Show font name
                contentStream.showText(fontName);
                // Move down for spacing (e.g., 2x the leading)
                contentStream.newLineAtOffset(0, -1 * leading);

                // Split and show the text
                List<String> wrappedText = wrapText(text, pdfFont, fontSize, page.getMediaBox().getWidth() - 2 * startX);
                for (String line : wrappedText) {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -leading);
                }

                contentStream.endText();

                // Update the startY position for the next font block
                startY -= wrappedText.size() * leading + 1.5 * leading; // Extra gap between font blocks
            }


            contentStream.close();
            document.save("examples/src/main/resources/org/apache/pdfbox/examples/nepali/nepali.pdf");
            System.out.println("PDF created successfully.");
            document.close();


        }
        catch (IOException e){
            System.out.println("PROBLEM: "+e.getMessage());
        }
    }

    private static Map<String, String> getFontMap() {
        String noto = "examples/src/main/resources/org/apache/pdfbox/resources/ttf/NotoSansDevanagariRegular.ttf";
        String noto_the_group = "examples/src/main/resources/org/apache/pdfbox/resources/ttf/NotoTheGroup.ttf";
        String kokila = "examples/src/main/resources/org/apache/pdfbox/resources/ttf/Kokila.ttf";
        String nirmala = "examples/src/main/resources/org/apache/pdfbox/resources/ttf/Nirmala.ttf";
        String nirmala_the_group = "examples/src/main/resources/org/apache/pdfbox/resources/ttf/NirmalaTheGroup.ttf";
        String mangal = "examples/src/main/resources/org/apache/pdfbox/resources/ttf/MangalRegular.ttf";
        String lohit = "examples/src/main/resources/org/apache/pdfbox/resources/ttf/LohitDevanagari.ttf";
        String tiro = "examples/src/main/resources/org/apache/pdfbox/resources/ttf/TiroDevanagariHindiRegular.ttf";
        String kalimati ="examples/src/main/resources/org/apache/pdfbox/resources/ttf/Kalimati.ttf";
        String kanjirowa ="examples/src/main/resources/org/apache/pdfbox/resources/ttf/Kanjirowa.ttf";


        Map<String, String> fontMap = new LinkedHashMap<>();
//        fontMap.put("नोटो सान्स देवनागरी", noto);
//        fontMap.put("नोटो द ग्रुप", noto_the_group);
        fontMap.put("मङ्गल", mangal);
        fontMap.put("कोकिल", kokila);
//        fontMap.put("निर्मला", nirmala);
//        fontMap.put("निर्मला द ग्रुप", nirmala_the_group);
        fontMap.put("लोहित",lohit);
        fontMap.put("टिरो",tiro);
        fontMap.put("कालिमाटी", kalimati);
        fontMap.put("काञ्जिरोवा", kanjirowa);

        return fontMap;
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

    private static String getStringText(){

        String loclText = "ख झ ५ ८ ९";
        String akhnText ="क्ष त्र ज्ञ";
        String rphfText = "र्य र्त्य र्त्स्य र्यि र्यी र्त्स्यि";
        String rkrfText = "क्र प्र श्र क्ष्र त्र ज्ञ्र";
        String blwfText = "ङ्र ट्र ठ्र ड्र ढ्र";
        String halfText = "क्य ख्य ग्य छ्य थ्य ष्ट न्थ्य क्र्क र्क्क";
        String cjctText = "ङ्क ङ्क्त ट्क ड्म द्ध द्म द्द द्द्र";
        String presText = "क्क क्त क्न ग्न च्च ष्ट्र ल्ल";
        String abvsText ="काँ किँ कीँ केँ कैँ कोँ कौँ र्काँ र्किँ र्कीँ र्केँ र्कैँ र्कोँ र्कौँ ";
        String blwsText = "रु रू ट्रु ट्रू ङ्कु";
        String pstsText ="की खी गी झी";
        String halnText = "द् ट् ढ् ड्";

        // Text to display
        //String text ="प्राकृतिक दृश्यले मन्त्रमुग्ध अन्नपूर्ण आनन्दित संस्कृतिलाई छन्।";
//        String text ="Dura नेपाली भाषाको लेखन प्रणाली देवनागरी लिपिमा आधारित छ।" +
//                "\"क्ष\", \"त्र\", \"ज्ञ\" जस्ता जटिल संयुक्ताक्षरहरू प्रचुर छन्।" +
//                "यो लिपि स्वर, व्यञ्जन, र संयुक्ताक्षरहरूको संयोजनमा समृद्ध छ।" +
//                "\"श्र\", \"द्य\", \"क्ष्म\" जस्ता अक्षरहरूले यसको जटिलता झल्काउँछन्।";
//        String text = " र्क र्का र्कि र्की र्के र्कै र्को र्कौ र्कँ र्न्थ्यि र्थ्यो";

//        String textOnPdf = "वर्त्स्य टर्कि गर्छन्";

        String textOnPdf = "वर्त्स्य टर्कि गर्छन्   राष्ट्रिय";

        return halnText;
    }
}