/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox.examples.pdmodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import static org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA;
import static org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.SYMBOL;
import static org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.ZAPF_DINGBATS;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;

/**
 * An example of using an embedded TrueType font with Unicode text.
 *
 * @author Keiji Suzuki
 * @author John Hewson
 */
public final class EmbeddedFonts
{
    private static final Log LOG = LogFactory.getLog(EmbeddedFonts.class);


    private EmbeddedFonts()
    {
    }
    
    public static void main(String[] args) throws IOException
    {
        try (PDDocument document = new PDDocument())
        {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            String dir = "../pdfbox/src/main/resources/org/apache/pdfbox/resources/ttf/";
            PDType0Font font = PDType0Font.load(document, new File(dir + "LiberationSans-Regular.ttf"));
            
            try (PDPageContentStream stream = new PDPageContentStream(document, page))
            {
                stream.beginText();
                stream.setFont(font, 12);
                stream.setLeading(12 * 1.2f);

                stream.newLineAtOffset(50, 600);
                stream.showText("PDFBox's Unicode with Embedded TrueType Font \u00AD - affection affiliate film float");
                stream.newLine();
                System.out.println("liberation sans done");
                
                stream.showText("Supports full Unicode text ☺");
                stream.newLine();
                
                stream.showText("English русский язык Tiếng Việt");
                stream.newLine();
                
                // ligature
                stream.showText("Ligatures: \uFB01lm \uFB02ood / generated: effective, affiliation, float, film, affluent");
                stream.newLine();
                
//                PDType0Font font3 = PDType0Font.load(document, new File("c:/windows/fonts/arabtype.ttf"));
//                stream.setFont(font3, 12);
//                stream.showText("نديم");
//                stream.newLine();
//                stream.showText(new StringBuilder("نديم").reverse().toString());
//                stream.newLine();
//                PDType0Font font2 = PDType0Font.load(document, new File("c:/windows/fonts/simhei.ttf"));
//                stream.setFont(font2, 12);
//                stream.showText("中国你好! simhei.ttf");
                
//            PDDocument doc2 = new PDDocument();
//            PDPage page2 = new PDPage();
//            PDPageContentStream cs2 = new PDPageContentStream(doc2, page2);
//            cs2.setFont(font2, 1);
//            cs2.close();
//            doc2.save(new ByteArrayOutputStream());
                
                PDType0Font font4 = PDType0Font.load(document, new FileInputStream(new File("c:/windows/fonts/arialuni.ttf")));
                stream.setFont(font4, 12);
                stream.newLine();
                stream.showText("α \uFF0C \u4E8B The quick brown fox jumps over the lazy dog äüöÄÜÖß 电信 arialuni.ttf effective affluent");
                
                stream.setFont(new PDType1Font(HELVETICA), 12);
                stream.newLine();
                stream.showText("\u2022 The\u00A0quick brown fox jumps over the lazy dog äüöÄÜÖß Helvetica type1");
                
                stream.setFont(new PDType1Font(ZAPF_DINGBATS), 12);
                stream.newLine();
                stream.showText("\u2714\u27a2    \u273F\u271D\u0020\u275E");
                
                PDFont font6 = new PDType1Font(SYMBOL);
                stream.setFont(font6, 12);
                stream.newLine();
                stream.setRenderingMode(RenderingMode.FILL_STROKE);
                stream.showText("\u2206ααα\u21D1\u21B5");
                stream.setRenderingMode(RenderingMode.FILL);
                
                PDFont font7 = PDType0Font.load(document, new File("c:/windows/fonts/webdings.ttf"));
                stream.setFont(font7, 12);
                stream.newLine();
                stream.showText("\uf061");
                
                PDFont font8 = PDType0Font.load(document, new File("c:/windows/fonts/arial.ttf"));
                //PDFont font8 = PDType0Font.load(document, new File("c:/windows/fonts/NotoMono-Regular.ttf"));
                //PDFont font8 = PDType0Font.load(document, new File("c:/users/tilman/downloads/dejavusans.ttf"));
                stream.setFont(font8, 12);
                stream.newLine();
                stream.showText("font8 Rupee ₹ \u062C  effective affluent");
                
                //TrueTypeFont ligttf = new TTFParser().parse(new RandomAccessReadBufferedFile("c:/windows/fonts/calibri.ttf"));
                stream.newLine();
                //PDFont ligFont = PDType0Font.load(document, ligttf, true);
                //PDFont ligFont = PDType0Font.load(document, new File("c:/windows/fonts/calibri.ttf"));
                PDFont ligFont = PDType0Font.load(document, new File("c:/users/tilman/downloads/dejavusans.ttf"));
                stream.setFont(ligFont, 12);
                stream.showText("Generated DejaVu ligatures: effective, affiliation, float, film, affluent");

                LOG.info("*** FIRA code test ***");
                stream.newLine();
                PDFont firaFont = PDType0Font.load(document, new File("c:/users/tilman/downloads/FiraCode-Regular.ttf"));
                stream.setFont(firaFont, 12);
                stream.showText("fira code = == => <= >= ==> === ");
                LOG.info("*** FIRA code test end ***");
                

                stream.endText();
            }
            
            document.save("C:\\Users\\Tilman\\Downloads\\example.pdf");
        }
    }
}
