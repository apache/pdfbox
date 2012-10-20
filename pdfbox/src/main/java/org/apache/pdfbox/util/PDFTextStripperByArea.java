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
package org.apache.pdfbox.util;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * This will extract text from a specified region in the PDF.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.5 $
 */
public class PDFTextStripperByArea extends PDFTextStripper
{
    private List<String> regions = new ArrayList<String>();
    private Map<String,Rectangle2D> regionArea = new HashMap<String,Rectangle2D>();
    private Map<String,Vector<ArrayList<TextPosition>>> regionCharacterList = 
        new HashMap<String,Vector<ArrayList<TextPosition>>>();
    private Map<String,StringWriter> regionText = new HashMap<String,StringWriter>();

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
     * Instantiate a new PDFTextStripperArea object. Loading all of the operator
     * mappings from the properties object that is passed in. Does not convert
     * the text to more encoding-specific output.
     * 
     * @param props
     *            The properties containing the mapping of operators to
     *            PDFOperator classes.
     * 
     * @throws IOException
     *             If there is an error reading the properties.
     */
    public PDFTextStripperByArea(Properties props) throws IOException
    {
        super(props);
        setPageSeparator("");
    }

    /**
     * Instantiate a new PDFTextStripperArea object. This object will load
     * properties from PDFTextStripper.properties and will apply
     * encoding-specific conversions to the output text.
     * 
     * @param encoding
     *            The encoding that the output will be written in.
     * @throws IOException
     *             If there is an error reading the properties.
     */
    public PDFTextStripperByArea(String encoding) throws IOException
    {
        super(encoding);
        setPageSeparator("");
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
    public List<String> getRegions()
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
        StringWriter text = regionText.get( regionName );
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
        Iterator<String> regionIter = regions.iterator();
        while( regionIter.hasNext() )
        {
            setStartPage(getCurrentPageNo());
            setEndPage(getCurrentPageNo());
            //reset the stored text for the region so this class
            //can be reused.
            String regionName = regionIter.next();
            Vector<ArrayList<TextPosition>> regionCharactersByArticle = new Vector<ArrayList<TextPosition>>();
            regionCharactersByArticle.add( new ArrayList<TextPosition>() );
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
    protected void processTextPosition( TextPosition text )
    {
        Iterator<String> regionIter = regionArea.keySet().iterator();
        while( regionIter.hasNext() )
        {
            String region = regionIter.next();
            Rectangle2D rect = regionArea.get( region );
            if( rect.contains( text.getX(), text.getY() ) )
            {
                charactersByArticle = (Vector)regionCharacterList.get( region );
                super.processTextPosition( text );
            }
        }
    }

    
    /**
     * This will print the processed page text to the output stream.
     *
     * @throws IOException If there is an error writing the text.
     */
    protected void writePage() throws IOException
    {
        Iterator<String> regionIter = regionArea.keySet().iterator();
        while( regionIter.hasNext() )
        {
            String region = regionIter.next();
            charactersByArticle = (Vector)regionCharacterList.get( region );
            output = regionText.get( region );
            super.writePage();
        }
    }
}
