/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.pdfbox.pdfviewer.font;

import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFFontROS;
import org.apache.pdfbox.encoding.Encoding;

/**
 * This class provides a glyph to GeneralPath conversion for CFF fonts.
 * 
 */
public class CFFGlyph2D implements Glyph2D
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(CFFGlyph2D.class);

    private HashMap<Integer, GeneralPath> glyphs = new HashMap<Integer, GeneralPath>();
    private HashMap<String, Integer> nameToGlyph = new HashMap<String, Integer>();
    private Map<Integer, String> codeToName = new LinkedHashMap<Integer, String>();
    private String fontname = null;

    /**
     * Constructor.
     * 
     */
    public CFFGlyph2D(CFFFont cffFont, Encoding encoding)
    {
        fontname = cffFont.getName();
        Collection<CFFFont.Mapping> mappings = cffFont.getMappings();
        // start with CFF built-in encoding        
        for (CFFFont.Mapping mapping : mappings)
        {
        	int code;
        	if (cffFont instanceof CFFFontROS) 
        	{
        		code = mapping.getSID();
        	}
        	else 
        	{
        		code = mapping.getCode();
        	}
        	codeToName.put(code, mapping.getName());        
        }
        // override existing entries with an optional PDF Encoding
        if (encoding != null) 
        {
        	Map<Integer, String> encodingCodeToName = encoding.getCodeToNameMap();
        	for (Integer key : encodingCodeToName.keySet())
        	{
        		codeToName.put(key, encodingCodeToName.get(key));
        	}
        }
        int glyphId = 0;
        for (CFFFont.Mapping mapping : mappings)
        {
            GeneralPath glyph = null;
            try
            {
                glyph = mapping.getType1CharString().getPath();
            }
            catch (IOException exception)
            {
                LOG.error("CFF glyph rendering fails!", exception);
            }
            if (glyph != null)
            {
                glyphs.put(glyphId, glyph);
                nameToGlyph.put(mapping.getName(), glyphId);
                glyphId++;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeneralPath getPathForGlyphId(int glyphId)
    {
        if (glyphs.containsKey(glyphId))
        {
            return glyphs.get(glyphId);
        }
        else
        {
            LOG.debug(fontname + ": glyph " + glyphId + " not found!");
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeneralPath getPathForCharactercode(int code)
    {
    	if (codeToName.containsKey(code))
    	{
    		String name = codeToName.get(code);
    		if (nameToGlyph.containsKey(name))
    		{
                return getPathForGlyphId(nameToGlyph.get(name));
    		}
    	}
        else
        {
            LOG.debug(fontname + ": glyphmapping for " + code + " not found!");
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfGlyphs()
    {
        if (glyphs != null)
        {
            return glyphs.size();
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose()
    {
        if (glyphs != null)
        {
            glyphs.clear();
        }
        if (codeToName != null)
        {
        	codeToName.clear();
        }
        if (nameToGlyph != null)
        {
        	nameToGlyph.clear();
        }
    }
}
