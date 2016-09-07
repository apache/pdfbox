package org.apache.pdfbox.pdmodel.interactive.digitalsignature;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for external signature creation scenarios. It contains method for retrieving PDF data
 * to be sign and setting created CMS signature to the PDF.
 *
 */
public interface ExternalSigningSupport
{
    /**
     * Get PDF content to be signed. Obtained InputStream must be closed after use.
     *
     * @return content stream
     *
     * @throws java.io.IOException
     */
    InputStream getContent() throws IOException;

    /**
     * Set CMS signature bytes to PDF.
     *
     * @param signature CMS signature as byte array
     *
     * @throws IOException if exception occured during PDF writing
     */
    void setSignature(byte[] signature) throws IOException;
}
