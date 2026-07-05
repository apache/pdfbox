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
package org.apache.pdfbox.glyphlayout;

import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/*
 * Example of formatting for letters defined in:
 * DIN 91379: Characters and defined character sequences in Unicode for the electronic processing of names
 * and data exchange in Europe
 * <p>
 * Use of the glyph layout features of Java that are based on HarfBuzz
 *
 * @author Volker Kunert
 */

class GlyphLayoutDin91379FormTest
{
    static String LATIN_CHARS_DIN_91379 =
                    "DIN 91379: Characters in Unicode for the electronic processing of names "
                    + "and data exchange in Europe\n"
                    + "bll; Latin Letters (normative)\n"
                    + "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z a b c d e f g h i j k l m n o p q r s t u v w x y z "
                    + "À Á Â Ã Ä Å Æ Ç È É Ê Ë Ì Í Î Ï Ð Ñ Ò Ó Ô Õ Ö Ø Ù Ú Û Ü Ý Þ ß à á â ã ä å æ ç è é ê ë ì í î ï ð ñ ò ó "
                    + "ô õ ö ø ù ú û ü ý þ ÿ Ā ā Ă ă Ą ą Ć ć Ĉ ĉ Ċ ċ Č č Ď ď Đ đ Ē ē Ĕ ĕ Ė ė Ę ę Ě ě Ĝ ĝ Ğ ğ Ġ ġ Ģ ģ Ĥ ĥ Ħ "
                    + "ħ Ĩ ĩ Ī ī Ĭ ĭ Į į İ ı Ĳ ĳ Ĵ ĵ Ķ ķ ĸ Ĺ ĺ Ļ ļ Ľ ľ Ŀ ŀ Ł ł Ń ń Ņ ņ Ň ň ŉ Ŋ ŋ Ō ō Ŏ ŏ Ő ő Œ œ Ŕ ŕ Ŗ ŗ Ř ř Ś ś Ŝ ŝ Ş "
                    + "ş Š š Ţ ţ Ť ť Ŧ ŧ Ũ ũ Ū ū Ŭ ŭ Ů ů Ű ű Ų ų Ŵ ŵ Ŷ ŷ Ÿ Ź ź Ż ż Ž ž Ƈ ƈ Ə Ɨ Ơ ơ Ư ư Ʒ Ǎ ǎ Ǐ ǐ Ǒ ǒ Ǔ ǔ Ǖ ǖ "
                    + "Ǘ ǘ Ǚ ǚ Ǜ ǜ Ǟ ǟ Ǣ ǣ Ǥ ǥ Ǧ ǧ Ǩ ǩ Ǫ ǫ Ǭ ǭ Ǯ ǯ ǰ Ǵ ǵ Ǹ ǹ Ǻ ǻ Ǽ ǽ Ǿ ǿ Ȓ ȓ Ș ș Ț ț Ȟ ȟ ȧ Ȩ ȩ Ȫ ȫ Ȭ ȭ "
                    + "Ȯ ȯ Ȱ ȱ Ȳ ȳ ə ɨ ʒ Ḃ ḃ Ḇ ḇ Ḋ ḋ Ḍ ḍ Ḏ ḏ Ḑ ḑ ḗ Ḝ ḝ Ḟ ḟ Ḡ ḡ Ḣ ḣ Ḥ ḥ Ḧ ḧ Ḩ ḩ Ḫ ḫ ḯ Ḱ ḱ Ḳ ḳ Ḵ ḵ Ḷ ḷ Ḻ ḻ Ṁ "
                    + "ṁ Ṃ ṃ Ṅ ṅ Ṇ ṇ Ṉ ṉ Ṓ ṓ Ṕ ṕ Ṗ ṗ Ṙ ṙ Ṛ ṛ Ṟ ṟ Ṡ ṡ Ṣ ṣ Ṫ ṫ Ṭ ṭ Ṯ ṯ Ẁ ẁ Ẃ ẃ Ẅ ẅ Ẇ ẇ Ẍ ẍ Ẏ ẏ Ẑ ẑ Ẓ ẓ Ẕ ẕ "
                    + "ẖ ẗ ẞ Ạ ạ Ả ả Ấ ấ Ầ ầ Ẩ ẩ Ẫ ẫ Ậ ậ Ắ ắ Ằ ằ Ẳ ẳ Ẵ ẵ Ặ ặ Ẹ ẹ Ẻ ẻ Ẽ ẽ Ế ế Ề ề Ể ể Ễ ễ Ệ ệ Ỉ ỉ Ị ị Ọ ọ Ỏ ỏ Ố "
                    + "ố Ồ ồ Ổ ổ Ỗ ỗ Ộ ộ Ớ ớ Ờ ờ Ở ở Ỡ ỡ Ợ ợ Ụ ụ Ủ ủ Ứ ứ Ừ ừ Ử ử Ữ ữ Ự ự Ỳ ỳ Ỵ ỵ Ỷ ỷ Ỹ ỹ\n"
                    + "Sequences\n"
                    + "A̋ C̀ C̄ C̆ C̈ C̕ C̣ C̦ C̨̆ D̂ F̀ F̄ G̀ H̄ H̦ H̱ J́ J̌ K̀ K̂ K̄ K̇ K̕ K̛ K̦ K͟H "
                    + "K͟h L̂ L̥ L̥̄ L̦ M̀ M̂ M̆ M̐ N̂ N̄ N̆ N̦ P̀ P̄ P̕ P̣ R̆ R̥ R̥̄ S̀ S̄ S̛̄ S̱ T̀ T̄ "
                    + "T̈ T̕ T̛ U̇ Z̀ Z̄ Z̆ Z̈ Z̧ a̋ c̀ c̄ c̆ c̈ c̕ c̣ c̦ c̨̆ d̂ f̀ f̄ g̀ h̄ h̦ j́ k̀ "
                    + "k̂ k̄ k̇ k̕ k̛ k̦ k͟h l̂ l̥ l̥̄ l̦ m̀ m̂ m̆ m̐ n̂ n̄ n̆ n̦ p̀ p̄ p̕ p̣ r̆ r̥ r̥ "
                    + "s̀ s̄ s̛̄ s̱ t̀ t̄ t̕ t̛ u̇ z̀ z̄ z̆ z̈ z̧ Ç̆ Û̄ ç̆ û̄ ÿ́ Č̕ Č̣ č̕ č̣ ē̍ Ī́ ī́ "
                    + "ō̍ Ž̦ Ž̧ ž̦ ž̧ Ḳ̄ ḳ̄ Ṣ̄ ṣ̄ Ṭ̄ ṭ̄ Ạ̈ ạ̈ Ọ̈ ọ̈ Ụ̄ Ụ̈ ụ̄ ụ̈\n"
                    + "bnlreq; Non-Letters N1 (normative)\n"
                    + "  ' , - . ` ~ ¨ ´ · ʹ ʺ ʾ ʿ ˈ ˌ ’ ‡\n"
                    + "bnl; Non-Letters N2 (normative)\n"
                    + "! \" # $ % & ( ) * + / 0 1 2 3 4 5 6 7 8 9 : ; < = > "
                    + "? @ [ \\ ] ^ _ { | } ¡ ¢ £ ¥ § © ª « ¬ ® ¯ ° ± ² ³ µ "
                    + "¶ ¹ º » ¿ × ÷ € \n"
                    + "bnlopt; Non-Letters N3 (normative)\n"
                    + "¤ ¦ ¸ ¼ ½ ¾ \n"
                    + "gl; Greek Letters (extended)\n"
                    + "Ά Έ Ή Ί Ό Ύ Ώ ΐ Α Β Γ Δ Ε Ζ Η Θ Ι Κ Λ Μ Ν Ξ Ο Π Ρ Σ "
                    + "Τ Υ Φ Χ Ψ Ω Ϊ Ϋ ά έ ή ί ΰ α β γ δ ε ζ η θ ι κ λ μ ν "
                    + "ξ ο π ρ ς σ τ υ φ χ ψ ω ϊ ϋ ό ύ ώ \n"
                    + "cl; Cyrillic Letters (extended)\n"
                    + "Ѝ А Б В Г Д Е Ж З И Й К Л М Н О П Р С Т У Ф Х Ц Ч Ш "
                    + "Щ Ъ Ь Ю Я а б в г д е ж з и й к л м н о п р с т у ф "
                    + "х ц ч ш щ ъ ь ю я ѝ \n"
                    + "enl; Non-Letters E1 (extended)\n"
                    + "ƒ ʰ ʳ ˆ ˜ ˢ ᵈ ᵗ ‘ ‚ “ ” „ † … ‰ ′ ″ ‹ › ⁰ ⁴ ⁵ ⁶ ⁷ ⁸ "
                    + "⁹ ⁿ ₀ ₁ ₂ ₃ ₄ ₅ ₆ ₇ ₈ ₉ ™ ∞ ≤ ≥ \n"
                    + "Additional non-letters (not included in DIN 91379): – — •�";

    @Test
    void testGlyphLayoutDin91379Form() throws IOException, FontFormatException, URISyntaxException
    {
        GlyphLayoutProcessorAwt glyphLayoutProcessor = new GlyphLayoutProcessorAwt();

        String outputFilename = "target/GlyphLayoutDIN91379Form.pdf";

        String fontSizeString = "12";

        InputStream fontStream = GlyphLayoutDin91379FormTest.class.getResourceAsStream("/ttf/Arimo-Regular.ttf");
        File pdfForm = new File(GlyphLayoutDin91379FormTest.class.getResource("/pdf/PdfForm.pdf").toURI());

        try (PDDocument doc = Loader.loadPDF(pdfForm))
        {
            // embedSubset has to be set to false
            // last parameter is just for test coverage
            PDType0Font font = glyphLayoutProcessor.loadFont(doc, fontStream, 
                    false, new GlyphLayoutFontLoaderAwt.FontOptions());

            PDDocumentCatalog docCatalog = doc.getDocumentCatalog();
            PDAcroForm acroForm = docCatalog.getAcroForm();

            acroForm.setGlyphLayoutProcessor(glyphLayoutProcessor);

            PDResources resources = new PDResources();
            String resourceFontName = resources.add(font).getName();
            acroForm.setDefaultResources(resources);
            for (PDField field : acroForm.getFields())
            {
                if (field instanceof PDTextField)
                {
                    PDTextField textfield = (PDTextField) field;
                    textfield.setDefaultAppearance(
                            String.format("/%s %s Tf 0 g ", resourceFontName, fontSizeString));
                    textfield.setValue(LATIN_CHARS_DIN_91379);
                }
            }
            acroForm.refreshAppearances();
            acroForm.flatten();
            doc.save(outputFilename);
        }

        //TODO add rendering comparison
        try (PDDocument doc = Loader.loadPDF(new File(outputFilename)))
        {
            assertEquals(1, doc.getNumberOfPages());
        }
    }
}
