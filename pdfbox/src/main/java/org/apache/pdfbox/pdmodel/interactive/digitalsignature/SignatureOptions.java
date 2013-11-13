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
package org.apache.pdfbox.pdmodel.interactive.digitalsignature;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.VisualSignatureParser;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigProperties;


public class SignatureOptions
{
    private COSDocument visualSignature;

    private int preferedSignatureSize;
    
    private int pageNo;
  
    /**
     * Set the page number.
     * 
     * @param pageNo the page number
     * 
     */
    public void setPage(int pageNo)
    {
        this.pageNo = pageNo;
    }
  
    /**
     * Get the page number.
     * 
     * @return the page number
     */
    public int getPage() 
    {
        return pageNo;
    }
  
    /**
     * Reads the visual signature from the given input stream.
     *  
     * @param is the input stream containing the visual signature
     * 
     * @throws IOException when something went wrong during parsing 
     */
    public void setVisualSignature(InputStream is) throws IOException
    { 
        VisualSignatureParser visParser = new VisualSignatureParser(is);
        visParser.parse();
        visualSignature = visParser.getDocument();
    }
    
    /**
     * Reads the visual signature from the given visual signature properties
     *  
     * @param is the <code>PDVisibleSigProperties</code> object containing the visual signature
     * 
     * @throws IOException when something went wrong during parsing
     * 
     * @since 1.8.3
     */
    public void setVisualSignature(PDVisibleSigProperties visSignatureProperties) throws IOException
    { 
        setVisualSignature(visSignatureProperties.getVisibleSignature());
    }

    /**
     * Get the visual signature.
     * 
     * @return the visual signature
     */
    public COSDocument getVisualSignature()
    {
        return visualSignature;
    }
  
    /**
     * Get the preferred size of the signature.
     * 
     * @return the preferred size
     */
    public int getPreferedSignatureSize()
    {
      return preferedSignatureSize;
    }
  
    /**
     * Set the preferred size of the signature.
     * 
     * @param size the size of the signature
     */
    public void setPreferedSignatureSize(int size)
    {
        if (size > 0)
        {
            preferedSignatureSize = size;
        }
    } 
}
