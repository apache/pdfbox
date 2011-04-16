package org.apache.pdfbox.pdmodel.interactive.digitalsignature;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.VisualSignatureParser;


public class SignatureOptions
{
  private COSDocument visualSignature;

  private int pageNo;
  
  public void setPage(int pageNo)
  {
    this.pageNo = pageNo;
  }
  
  public int getPage() {
    return pageNo;
  }
  
  public void setVisualSignature(InputStream is) throws IOException
  {
    VisualSignatureParser visParser = new VisualSignatureParser(is);
    visParser.parse();
    visualSignature = visParser.getDocument();
  }

  public COSDocument getVisualSignature()
  {
    return visualSignature;
  }
}
