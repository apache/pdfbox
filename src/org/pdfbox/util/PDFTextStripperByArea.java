/**
 * Copyright (c) 2005-2006, www.pdfbox.org
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
package org.pdfbox.util;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.pdfbox.cos.COSStream;
import org.pdfbox.pdmodel.PDPage;
import org.pdfbox.pdmodel.common.PDStream;

/**
 * This will extract text from a specified region in the PDF.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.5 $
 */
public class PDFTextStripperByArea extends PDFTextStripper
{
    private List regions = new ArrayList();
    private Map regionArea = new HashMap();
    private Map regionCharacterList = new HashMap();
    private Map regionText = new HashMap();
    
    /**
     * Constructor.
     * @throws IOException If there is an error loading properties.
     */
    public PDFTextStripperByArea() throws IOException
    {
        super();
        setPageSeparator( "" );
    }
    
    /**
     * Add a new region to group text by.
     * 
     * @param regionName The name of the region.
     * @param rect The rectangle area to retrieve the text from.
     */
    public void addRegion( String regionName, Rectangle2D rect )
    {
        regions.add( regionName );
        regionArea.put( regionName, rect );
    }
    
    /**
     * Get the list of regions that have been setup.  
     * 
     * @return A list of java.lang.String objects to identify the region names.
     */
    public List getRegions()
    {
        return regions;
    }
    
    /**
     * Get the text for the region, this should be called after extractRegions().
     * 
     * @param regionName The name of the region to get the text from.
     * @return The text that was identified in that region.
     */
    public String getTextForRegion( String regionName )
    {
        StringWriter text = (StringWriter)regionText.get( regionName );
        return text.toString();
    }
    
    /**
     * Process the page to extract the region text.
     * 
     * @param page The page to extract the regions from.
     * @throws IOException If there is an error while extracting text.
     */
    public void extractRegions( PDPage page ) throws IOException
    {
        Iterator regionIter = regions.iterator();
        while( regionIter.hasNext() )
        {
            //reset the stored text for the region so this class
            //can be reused.
            String regionName = (String)regionIter.next();
            Vector regionCharactersByArticle = new Vector();
            regionCharactersByArticle.add( new ArrayList() );
            regionCharacterList.put( regionName, regionCharactersByArticle );
            regionText.put( regionName, new StringWriter() );
        }
        
        PDStream contentStream = page.getContents();
        if( contentStream != null )
        {
            COSStream contents = contentStream.getStream();
            processPage( page, contents );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected void showCharacter( TextPosition text )
    {
        Iterator regionIter = regionArea.keySet().iterator();
        while( regionIter.hasNext() )
        {
            String region = (String)regionIter.next();
            Rectangle2D rect = (Rectangle2D)regionArea.get( region );
            if( rect.contains( text.getX(), text.getY() ) )
            {
                charactersByArticle = (Vector)regionCharacterList.get( region );
                super.showCharacter( text );                
            }
        }
    }
    
    /**
     * This will print the text to the output stream.
     *
     * @throws IOException If there is an error writing the text.
     */
    protected void flushText() throws IOException
    {
        Iterator regionIter = regionArea.keySet().iterator();
        while( regionIter.hasNext() )
        {
            String region = (String)regionIter.next();
            charactersByArticle = (Vector)regionCharacterList.get( region );
            output = (StringWriter)regionText.get( region );
            super.flushText();
        }
    }
}
