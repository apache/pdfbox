package org.apache.xmpbox.parser;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class InlineXmpTest
{

    private static final String EXAMPLE = "src/test/resources/validxmp/inline-definition.xml";
    @Test
    public void testCanParseValidSchema() throws IOException, XmpParsingException, BadFieldValueException
    {
        byte[] xmpData = Files.readAllBytes(Paths.get(EXAMPLE));
        final DomXmpParser xmpParser = new DomXmpParser();
        final XMPMetadata metadata = xmpParser.parse(xmpData);
        checkForPDFAIdentifiers(metadata);
    }

    private void checkForPDFAIdentifiers(final XMPMetadata xmp) throws BadFieldValueException
    {
        assertNotNull(xmp, "XMPSchema nicht vorhanden");
        final PDFAIdentificationSchema pdfaIdSchema = xmp.getPDFAIdentificationSchema();
        assertNotNull(pdfaIdSchema, "PDFAIdentificationSchema nicht vorhanden");
        final int partValue = pdfaIdSchema.getPart();
        assertTrue(partValue == 1 || partValue == 2,
                "Das PDF-Dokument entspricht nicht dem geforderten Standard");
        final String dataValue = xmp.getSchema("http://ns.example.org/default/1.0/").getUnqualifiedTextPropertyValue("Data");
        assertEquals("Example", dataValue, "Falscher Wert in Data-Field");
    }

}
