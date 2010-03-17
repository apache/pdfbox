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
package org.apache.pdfbox.pdmodel.fdf;

import java.io.IOException;
import java.io.Writer;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;

import org.apache.pdfbox.pdmodel.common.COSObjectable;

import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;

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
