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
import java.util.ArrayList;
import java.util.List;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSStream;
import org.pdfbox.cos.COSString;

import org.pdfbox.pdmodel.common.COSObjectable;
import org.pdfbox.pdmodel.common.COSArrayList;
import org.pdfbox.pdmodel.common.filespecification.PDFileSpecification;
import org.pdfbox.pdmodel.common.filespecification.PDSimpleFileSpecification;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This represents an FDF dictionary that is part of the FDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.9 $
 */
public class FDFDictionary implements COSObjectable
{
    private COSDictionary fdf;

    /**
     * Default constructor.
     */
    public FDFDictionary()
    {
        fdf = new COSDictionary();
    }

    /**
     * Constructor.
     *
     * @param fdfDictionary The FDF documents catalog.
     */
    public FDFDictionary( COSDictionary fdfDictionary )
    {
        fdf = fdfDictionary;
    }
    
    /**
     * This will create an FDF dictionary from an XFDF XML document.
     * 
     * @param fdfXML The XML document that contains the XFDF data.
     * @throws IOException If there is an error reading from the dom.
     */
    public FDFDictionary( Element fdfXML ) throws IOException
    {
        this();
        NodeList nodeList = fdfXML.getChildNodes();
        for( int i=0; i<nodeList.getLength(); i++ )
        {
            Node node = nodeList.item( i );
            if( node instanceof Element )
            {
                Element child = (Element)node;
                if( child.getTagName().equals( "f" ) )
                {
                    PDSimpleFileSpecification fs = new PDSimpleFileSpecification();
                    fs.setFile( child.getAttribute( "href" ) );
                    setFile(fs);
                
                }
                else if( child.getTagName().equals( "ids" ) )
                {
                    COSArray ids = new COSArray();
                    String original = child.getAttribute( "original" );
                    String modified = child.getAttribute( "modified" );
                    ids.add( COSString.createFromHexString( original ) );
                    ids.add( COSString.createFromHexString( modified ) );
                    setID( ids );
                }
                else if( child.getTagName().equals( "fields" ) )
                {
                    NodeList fields = child.getElementsByTagName( "field" );
                    List fieldList = new ArrayList();
                    for( int f=0; f<fields.getLength(); f++ )
                    {
                        fieldList.add( new FDFField( (Element)fields.item( f ) ) );
                    }
                    setFields( fieldList );
                }
                else if( child.getTagName().equals( "annots" ) )
                {
                    NodeList annots = child.getChildNodes();
                    List annotList = new ArrayList();
                    for( int j=0; j<annots.getLength(); j++ )
                    {
                        Node annotNode = annots.item( i );
                        if( annotNode instanceof Element )
                        {
                            Element annot = (Element)annotNode;
                            if( annot.getNodeName().equals( "text" ) )
                            {
                                annotList.add( new FDFAnnotationText( annot ) );
                            }
                            else
                            {
                                throw new IOException( "Error: Unknown annotation type '" + annot.getNodeName() );
                            }
                        }
                    }
                    setAnnotations(annotList);
                }
            }
        }
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
        PDFileSpecification fs = this.getFile();
        if( fs != null )
        {
            output.write( "<f href=\"" + fs.getFile() + "\" />\n" );
        }
        COSArray ids = this.getID();
        if( ids != null )
        {
            COSString original = (COSString)ids.getObject( 0 );
            COSString modified = (COSString)ids.getObject( 1 );
            output.write( "<ids original=\"" + original.getHexString() + "\" " );
            output.write( "modified=\"" + modified.getHexString() + "\" />\n");
        }
        List fields = getFields();
        if( fields != null && fields.size() > 0 )
        {
            output.write( "<fields>\n" );
            for( int i=0; i<fields.size(); i++ )
            {
                ((FDFField)fields.get( i )).writeXML( output );
            }
            output.write( "</fields>\n" );
        }
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return fdf;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSDictionary getCOSDictionary()
    {
        return fdf;
    }

    /**
     * The source file or target file: the PDF document file that
     * this FDF file was exported from or is intended to be imported into.
     *
     * @return The F entry of the FDF dictionary.
     * 
     * @throws IOException If there is an error creating the file spec.
     */
    public PDFileSpecification getFile() throws IOException
    {
        return PDFileSpecification.createFS( fdf.getDictionaryObject( "F" ) );
    }

    /**
     * This will set the file specification.
     *
     * @param fs The file specification.
     */
    public void setFile( PDFileSpecification fs )
    {
        fdf.setItem( "F", fs );
    }

    /**
     * This is the FDF id.
     *
     * @return The FDF ID.
     */
    public COSArray getID()
    {
        return (COSArray)fdf.getDictionaryObject( "ID" );
    }

    /**
     * This will set the FDF id.
     *
     * @param id The new id for the FDF.
     */
    public void setID( COSArray id )
    {
        fdf.setItem( "ID", id );
    }

    /**
     * This will get the list of FDF Fields.  This will return a list of FDFField
     * objects.
     *
     * @return A list of FDF fields.
     */
    public List getFields()
    {
        List retval = null;
        COSArray fieldArray = (COSArray)fdf.getDictionaryObject( "Fields" );
        if( fieldArray != null )
        {
            List fields = new ArrayList();
            for( int i=0; i<fieldArray.size(); i++ )
            {
                fields.add( new FDFField( (COSDictionary)fieldArray.getObject( i ) ) );
            }
            retval = new COSArrayList( fields, fieldArray );
        }
        return retval;
    }

    /**
     * This will set the list of fields.  This should be a list of FDFField objects.
     *
     * @param fields The list of fields.
     */
    public void setFields( List fields )
    {
        fdf.setItem( "Fields", COSArrayList.converterToCOSArray( fields ) );
    }

    /**
     * This will get the status string to be displayed as the result of an
     * action.
     *
     * @return The status.
     */
    public String getStatus()
    {
        return fdf.getString( "Status" );
    }

    /**
     * This will set the status string.
     *
     * @param status The new status string.
     */
    public void setStatus( String status )
    {
        fdf.setString( "Status", status );
    }

    /**
     * This will get the list of FDF Pages.  This will return a list of FDFPage objects.
     *
     * @return A list of FDF pages.
     */
    public List getPages()
    {
        List retval = null;
        COSArray pageArray = (COSArray)fdf.getDictionaryObject( "Pages" );
        if( pageArray != null )
        {
            List pages = new ArrayList();
            for( int i=0; i<pageArray.size(); i++ )
            {
                pages.add( new FDFPage( (COSDictionary)pageArray.get( i ) ) );
            }
            retval = new COSArrayList( pages, pageArray );
        }
        return retval;
    }

    /**
     * This will set the list of pages.  This should be a list of FDFPage objects.
     *
     *
     * @param pages The list of pages.
     */
    public void setPages( List pages )
    {
        fdf.setItem( "Pages", COSArrayList.converterToCOSArray( pages ) );
    }

    /**
     * The encoding to be used for a FDF field.  The default is PDFDocEncoding
     * and this method will never return null.
     *
     * @return The encoding value.
     */
    public String getEncoding()
    {
        String encoding = fdf.getNameAsString( "Encoding" );
        if( encoding == null )
        {
            encoding = "PDFDocEncoding";
        }
        return encoding;

    }

    /**
     * This will set the encoding.
     *
     * @param encoding The new encoding.
     */
    public void setEncoding( String encoding )
    {
        fdf.setName( "Encoding", encoding );
    }

    /**
     * This will get the list of FDF Annotations.  This will return a list of FDFAnnotation objects
     * or null if the entry is not set.
     *
     * @return A list of FDF annotations.
     * 
     * @throws IOException If there is an error creating the annotation list.
     */
    public List getAnnotations() throws IOException
    {
        List retval = null;
        COSArray annotArray = (COSArray)fdf.getDictionaryObject( "Annots" );
        if( annotArray != null )
        {
            List annots = new ArrayList();
            for( int i=0; i<annotArray.size(); i++ )
            {
                annots.add( FDFAnnotation.create( (COSDictionary)annotArray.getObject( i ) ) );
            }
            retval = new COSArrayList( annots, annotArray );
        }
        return retval;
    }

    /**
     * This will set the list of annotations.  This should be a list of FDFAnnotation objects.
     *
     *
     * @param annots The list of annotations.
     */
    public void setAnnotations( List annots )
    {
        fdf.setItem( "Annots", COSArrayList.converterToCOSArray( annots ) );
    }

    /**
     * This will get the incremental updates since the PDF was last opened.
     *
     * @return The differences entry of the FDF dictionary.
     */
    public COSStream getDifferences()
    {
        return (COSStream)fdf.getDictionaryObject( "Differences" );
    }

    /**
     * This will set the differences stream.
     *
     * @param diff The new differences stream.
     */
    public void setDifferences( COSStream diff )
    {
        fdf.setItem( "Differences", diff );
    }

    /**
     * This will get the target frame in the browser to open this document.
     *
     * @return The target frame.
     */
    public String getTarget()
    {
        return fdf.getString( "Target" );
    }

    /**
     * This will set the target frame in the browser to open this document.
     *
     * @param target The new target frame.
     */
    public void setTarget( String target )
    {
        fdf.setString( "Target", target );
    }

    /**
     * This will get the list of embedded FDF entries, or null if the entry is null.
     * This will return a list of PDFileSpecification objects.
     *
     * @return A list of embedded FDF files.
     * 
     * @throws IOException If there is an error creating the file spec.
     */
    public List getEmbeddedFDFs() throws IOException
    {
        List retval = null;
        COSArray embeddedArray = (COSArray)fdf.getDictionaryObject( "EmbeddedFDFs" );
        if( embeddedArray != null )
        {
            List embedded = new ArrayList();
            for( int i=0; i<embeddedArray.size(); i++ )
            {
                embedded.add( PDFileSpecification.createFS( embeddedArray.get( i ) ) );
            }
            retval = new COSArrayList( embedded, embeddedArray );
        }
        return retval;
    }

    /**
     * This will set the list of embedded FDFs.  This should be a list of
     * PDFileSpecification objects.
     *
     *
     * @param embedded The list of embedded FDFs.
     */
    public void setEmbeddedFDFs( List embedded )
    {
        fdf.setItem( "EmbeddedFDFs", COSArrayList.converterToCOSArray( embedded ) );
    }

    /**
     * This will get the java script entry.
     *
     * @return The java script entry describing javascript commands.
     */
    public FDFJavaScript getJavaScript()
    {
        FDFJavaScript fs = null;
        COSDictionary dic = (COSDictionary)fdf.getDictionaryObject( "JavaScript" );
        if( dic != null )
        {
            fs = new FDFJavaScript( dic );
        }
        return fs;
    }

    /**
     * This will set the JavaScript entry.
     *
     * @param js The javascript entries.
     */
    public void setJavaScript( FDFJavaScript js )
    {
        fdf.setItem( "JavaScript", js );
    }

}