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
package org.apache.pdfbox.text;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDPage;

/**
 * This will extract text from a specified region in the PDF.
 *
 * @author Ben Litchfield
 */
public class PDFTextStripperByArea extends PDFTextStripper
{
    private final List<String> regions = new ArrayList<String>();
    private final Map<String, Rectangle2D> regionArea = new HashMap<String, Rectangle2D>();
    private final Map<String, ArrayList<List<TextPosition>>> regionCharacterList
            = new HashMap<String, ArrayList<List<TextPosition>>>();
    private final Map<String, StringWriter> regionText = new HashMap<String, StringWriter>();

    /**
     * Constructor.
     * @throws IOException If there is an error loading properties.
     */
    public PDFTextStripperByArea() throws IOException
    {
        super();
        super.setShouldSeparateByBeads(false);
    }

    /**
     * This method does nothing in this derived class, because beads and regions are incompatible. Beads are
     * ignored when stripping by area.
     *
     * @param aShouldSeparateByBeads The new grouping of beads.
     */
    @Override
    public final void setShouldSeparateByBeads(boolean aShouldSeparateByBeads)
    {
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
     * Delete a region to group text by. If the region does not exist, this method does nothing.
     *
     * @param regionName The name of the region to delete.
     */
    public void removeRegion(String regionName)
    {
        regions.remove(regionName);
        regionArea.remove(regionName);
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
        for (String region : regions)
        {
            setStartPage(getCurrentPageNo());
            setEndPage(getCurrentPageNo());
            //reset the stored text for the region so this class
            //can be reused.
            String regionName = region;
            ArrayList<List<TextPosition>> regionCharactersByArticle = new ArrayList<List<TextPosition>>();
            regionCharactersByArticle.add( new ArrayList<TextPosition>() );
            regionCharacterList.put( regionName, regionCharactersByArticle );
            regionText.put( regionName, new StringWriter() );
        }
        
        if( page.hasContents() )
        {
            processPage( page );
        }
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void processTextPosition( TextPosition text )
    {
        for (String region : regionArea.keySet())
        {
            Rectangle2D rect = regionArea.get( region );
            if( rect.contains( text.getX(), text.getY() ) )
            {
                charactersByArticle = regionCharacterList.get( region );
                super.processTextPosition( text );
            }
        }
    }

    
    /**
     * This will print the processed page text to the output stream.
     *
     * @throws IOException If there is an error writing the text.
     */
    @Override
    protected void writePage() throws IOException
    {
        for (String region : regionArea.keySet())
        {
            charactersByArticle = regionCharacterList.get( region );
            output = regionText.get( region );
            super.writePage();
        }
    }
}
