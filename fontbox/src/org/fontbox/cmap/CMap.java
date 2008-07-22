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
package org.fontbox.cmap;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a CMap file.
 *
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.3 $
 */
public class CMap
{
    private List codeSpaceRanges = new ArrayList();
    private Map singleByteMappings = new HashMap();
    private Map doubleByteMappings = new HashMap();

    /**
     * Creates a new instance of CMap.
     */
    public CMap()
    {
        //default constructor
    }
    
    /**
     * This will tell if this cmap has any one byte mappings.
     * 
     * @return true If there are any one byte mappings, false otherwise.
     */
    public boolean hasOneByteMappings()
    {
        return singleByteMappings.size() > 0;
    }
    
    /**
     * This will tell if this cmap has any two byte mappings.
     * 
     * @return true If there are any two byte mappings, false otherwise.
     */
    public boolean hasTwoByteMappings()
    {
        return doubleByteMappings.size() > 0;
    }

    /**
     * This will perform a lookup into the map.
     *
     * @param code The code used to lookup.
     * @param offset The offset into the byte array.
     * @param length The length of the data we are getting.
     *
     * @return The string that matches the lookup.
     */
    public String lookup( byte[] code, int offset, int length )
    {
        String result = null;
        Integer key = null;
        if( length == 1 )
        {
            
            key = new Integer( (code[offset]+256)%256 );
            result = (String)singleByteMappings.get( key );
        }
        else if( length == 2 )
        {
            int intKey = (code[offset]+256)%256;
            intKey <<= 8;
            intKey += (code[offset+1]+256)%256;
            key = new Integer( intKey );

            result = (String)doubleByteMappings.get( key );
        }

        return result;
    }

    /**
     * This will add a mapping.
     *
     * @param src The src to the mapping.
     * @param dest The dest to the mapping.
     *
     * @throws IOException if the src is invalid.
     */
    public void addMapping( byte[] src, String dest ) throws IOException
    {
        if( src.length == 1 )
        {
            singleByteMappings.put( new Integer( src[0] ), dest );
        }
        else if( src.length == 2 )
        {
            int intSrc = src[0]&0xFF;
            intSrc <<= 8;
            intSrc |= (src[1]&0xFF);
            doubleByteMappings.put( new Integer( intSrc ), dest );
        }
        else
        {
            throw new IOException( "Mapping code should be 1 or two bytes and not " + src.length );
        }
    }


    /**
     * This will add a codespace range.
     *
     * @param range A single codespace range.
     */
    public void addCodespaceRange( CodespaceRange range )
    {
        codeSpaceRanges.add( range );
    }

    /**
     * Getter for property codeSpaceRanges.
     *
     * @return Value of property codeSpaceRanges.
     */
    public List getCodeSpaceRanges()
    {
        return codeSpaceRanges;
    }
    
    /**
     * Implementation of the usecmap operator.  This will
     * copy all of the mappings from one cmap to another.
     * 
     * @param cmap The cmap to load mappings from.
     */
    public void useCmap( CMap cmap )
    {
        this.codeSpaceRanges.addAll( cmap.codeSpaceRanges );
        this.singleByteMappings.putAll( cmap.singleByteMappings );
        this.doubleByteMappings.putAll( cmap.doubleByteMappings );
    }

}