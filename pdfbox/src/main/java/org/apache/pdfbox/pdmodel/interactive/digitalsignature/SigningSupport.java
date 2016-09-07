package org.apache.pdfbox.pdmodel.interactive.digitalsignature;

import org.apache.pdfbox.pdfwriter.COSWriter;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class to be used when creating PDF signatures externally. COSWriter is used to obtain data to be
 * signed and set the resulted CMS signature.
 *
 */
public class SigningSupport implements ExternalSigningSupport, Closeable
{
    private COSWriter cosWriter;

    public SigningSupport(COSWriter cosWriter)
    {
        this.cosWriter = cosWriter;
    }

    @Override
    public InputStream getContent() throws IOException
    {
        return cosWriter.getDataToSign();
    }

    @Override
    public void setSignature(byte[] signature) throws IOException
    {
        cosWriter.writeExternalSignature(signature);
    }

    @Override
    public void close() throws IOException
    {
        if (cosWriter != null)
        {
            try
            {
                cosWriter.close();
            }
            finally
            {
                cosWriter = null;
            }
        }
    }
}
