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
package org.pdfbox.pdmodel.interactive.action.type;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;

/**
 * This represents a URI action that can be executed in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author Panagiotis Toumasis (ptoumasis@mail.gr)
 * @version $Revision: 1.3 $
 */
public class PDActionURI extends PDAction
{
    /**
     * This type of action this object represents.
     */
    public static final String SUB_TYPE = "URI";

    /**
     * Default constructor.
     */
    public PDActionURI()
    {
        action = new COSDictionary();
        setSubType( SUB_TYPE );
    }

    /**
     * Constructor.
     *
     * @param a The action dictionary.
     */
    public PDActionURI( COSDictionary a )
    {
        super( a );
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return action;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSDictionary getCOSDictionary()
    {
        return action;
    }

    /**
     * This will get the type of action that the actions dictionary describes.
     * It must be URI for a URI action.
     *
     * @return The S entry of the specific URI action dictionary.
     */
    public String getS()
    {
       return action.getNameAsString( "S" );
    }

    /**
     * This will set the type of action that the actions dictionary describes.
     * It must be URI for a URI action.
     *
     * @param s The URI action.
     */
    public void setS( String s )
    {
       action.setName( "S", s );
    }

    /**
     * This will get the uniform resource identifier to resolve, encoded in 7-bit ASCII.
     *
     * @return The URI entry of the specific URI action dictionary.
     */
    public String getURI()
    {
        return action.getString( "URI" );
    }

    /**
     * This will set the uniform resource identifier to resolve, encoded in 7-bit ASCII.
     *
     * @param uri The uniform resource identifier.
     */
    public void setURI( String uri )
    {
        action.setString( "URI", uri );
    }

    /**
     * This will specify whether to track the mouse position when the URI is resolved.
     * Default value: false.
     * This entry applies only to actions triggered by the user's clicking an annotation;
     * it is ignored for actions associated with outline items or with a document's OpenAction entry.
     *
     * @return A flag specifying whether to track the mouse position when the URI is resolved.
     */
    public boolean shouldTrackMousePosition()
    {
        return action.getBoolean( "MousePosition", true );
    }

    /**
     * This will specify whether to track the mouse position when the URI is resolved.
     *
     * @param value The flag value.
     */
    public void setTrackMousePosition( boolean value )
    {
        action.setBoolean( "MousePosition", value );
    }

    /**
     * This will get the base URI to be used in resolving relative URI references.
     * URI actions within the document may specify URIs in partial form, to be interpreted
     * relative to this base address. If no base URI is specified, such partial URIs
     * will be interpreted relative to the location of the document itself.
     * The use of this entry is parallel to that of the body element &lt;BASE&gt;, as described
     * in the HTML 4.01 Specification.
     *
     * @return The URI entry of the specific URI dictionary.
     */
    public String getBase()
    {
        return action.getString( "Base" );
    }

    /**
     * This will set the base URI to be used in resolving relative URI references.
     * URI actions within the document may specify URIs in partial form, to be interpreted
     * relative to this base address. If no base URI is specified, such partial URIs
     * will be interpreted relative to the location of the document itself.
     * The use of this entry is parallel to that of the body element &lt;BASE&gt;, as described
     * in the HTML 4.01 Specification.
     *
     * @param base The the base URI to be used.
     */
    public void setBase( String base )
    {
        action.setString( "Base", base );
    }
}