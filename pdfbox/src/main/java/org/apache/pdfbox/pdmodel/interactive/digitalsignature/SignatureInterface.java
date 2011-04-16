package org.apache.pdfbox.pdmodel.interactive.digitalsignature;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.exceptions.SignatureException;

/**
 * Providing an interface for accessing necessary functions for signing a pdf document.
 * 
 * @author <a href="mailto:mail@thomas-chojecki.de">Thomas Chojecki</a>
 * @version $
 */
public interface SignatureInterface
{

  /**
   * Creates a cms signature for the given content
   * 
   * @param content is the content as a (Filter)InputStream
   * @return signature as a byte array
   */
  public byte[] sign (InputStream content) throws SignatureException, IOException;
  
}
