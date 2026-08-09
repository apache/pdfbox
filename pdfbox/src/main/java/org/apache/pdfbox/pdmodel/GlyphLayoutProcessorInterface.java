diff --git a/pdfbox/src/main/java/org/apache/pdfbox/pdmodel/GlyphLayoutProcessorInterface.java b/pdfbox/src/main/java/org/apache/pdfbox/pdmodel/GlyphLayoutProcessorInterface.java
index e69de29..4570509 100644
--- a/pdfbox/src/main/java/org/apache/pdfbox/pdmodel/GlyphLayoutProcessorInterface.java
+++ b/pdfbox/src/main/java/org/apache/pdfbox/pdmodel/GlyphLayoutProcessorInterface.java
@@ -35,6 +35,16 @@ public interface GlyphLayoutProcessorInterface
     boolean supportsFont(PDFont font);
 
     /**
+     * Compute the width for a text
+     * @param font to be used
+     * @param fontSize font size
+     * @param text text
+     * @return string width
+     */
+    float getStringWidth(PDType0Font font, float fontSize, String text) throws IOException;
+
+
     /**
      * Shows a text using glyph positioning (if needed)
      *
