/**
 * Copyright (c) 2004, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.pdmodel.fdf;

import java.io.IOException;
import java.io.Writer;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;

import org.pdfbox.pdmodel.common.COSObjectable;

import org.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;

import org.w3c.dom.Element;

/**
 * This represents an FDF catalog that is part of the FDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class FDFCatalog implements COSObjectable
{
    private COSDictionary catalog;

    /**
     * Default constructor.
     */
    public FDFCatalog()
    {
        catalog = new COSDictionary();
    }

    /**
     * Constructor.
     *
     * @param cat The FDF documents catalog.
     */
    public FDFCatalog( COSDictionary cat )
    {
        catalog = cat;
    }
    
    /**
     * This will create an FDF catalog from an XFDF XML document.
     * 
     * @param element The XML document that contains the XFDF data.
     * @throws IOException If there is an error reading from the dom.
     */
    public FDFCatalog( Element element ) throws IOException
    {
        this();
        FDFDictionary fdfDict = new FDFDictionary( element );
        setFDF( fdfDict );
    }
    
    /**
     * This will write this element as an XML document.
     * 
     * @param output The stream to write the xml to.
     * 
     * @throws IOException If there is an error writing the XML.
     */
    public void writeXML( Writer output ) throws IOException
    {
        FDFDictionary fdf = getFDF();
        fdf.writeXML( output );
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return catalog;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSDictionary getCOSDictionary()
    {
        return catalog;
    }

    /**
     * This will get the version that was specified in the catalog dictionary.
     *
     * @return The FDF version.
     */
    public String getVersion()
    {
        return catalog.getNameAsString( "Version" );
    }

    /**
     * This will set the version of the FDF document.
     *
     * @param version The new version for the FDF document.
     */
    public void setVersion( String version )
    {
        catalog.setName( "Version", version );
    }

    /**
     * This will get the FDF dictionary.
     *
     * @return The FDF dictionary.
     */
    public FDFDictionary getFDF()
    {
        COSDictionary fdf = (COSDictionary)catalog.getDictionaryObject( "FDF" );
        FDFDictionary retval = null;
        if( fdf != null )
        {
            retval = new FDFDictionary( fdf );
        }
        else
        {
            retval = new FDFDictionary();
            setFDF( retval );
        }
        return retval;
    }

    /**
     * This will set the FDF document.
     *
     * @param fdf The new FDF dictionary.
     */
    public void setFDF( FDFDictionary fdf )
    {
        catalog.setItem( "FDF", fdf );
    }

    /**
     * This will get the signature or null if there is none.
     *
     * @return The signature.
     */
    public PDSignature getSignature()
    {
        PDSignature signature = null;
        COSDictionary sig = (COSDictionary)catalog.getDictionaryObject( "Sig" );
        if( sig != null )
        {
            signature = new PDSignature( sig );
        }
        return signature;
    }

    /**
     * This will set the signature that is associated with this catalog.
     *
     * @param sig The new signature.
     */
    public void setSignature( PDSignature sig )
    {
        catalog.setItem( "Sig", sig );
    }
}