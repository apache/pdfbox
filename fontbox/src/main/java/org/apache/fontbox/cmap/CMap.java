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
package org.apache.fontbox.cmap;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

/**
 * This class represents a CMap file.
 *
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.3 $
 */
public class CMap
{
    
    private int wmode = 0;
    private String cmapName = null;
    private String cmapVersion = null;
    private int cmapType = -1;
    
    private String registry = null;
    private String ordering = null;
    private int supplement = 0;
    
    private List<CodespaceRange> codeSpaceRanges = new ArrayList<CodespaceRange>();
    private Map<Integer,String> singleByteMappings = new HashMap<Integer,String>();
    private Map<Integer,String> doubleByteMappings = new HashMap<Integer,String>();

    private final Map<Integer,String> cid2charMappings = new HashMap<Integer,String>();
    private final Map<String,Integer> char2CIDMappings = new HashMap<String,Integer>();
    private final List<CIDRange> cidRanges = new LinkedList<CIDRange>();

    private static final String SPACE = " ";
    private int spaceMapping = -1;
    
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
     * This will tell if this cmap has any CID mappings.
     * 
     * @return true If there are any CID mappings, false otherwise.
     */
    public boolean hasCIDMappings()
    {
        return !char2CIDMappings.isEmpty() || !cidRanges.isEmpty();
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
        return lookup(getCodeFromArray(code, offset, length), length);
    }

    /**
     * This will perform a lookup into the map.
     *
     * @param code The code used to lookup.
     * @param length The length of the data we are getting.
     *
     * @return The string that matches the lookup.
     */
    public String lookup( int code, int length )
    {
        String result = null;
        if( length == 1 )
        {
            result = singleByteMappings.get( code );
        }
        else if( length == 2 )
        {
            result = doubleByteMappings.get( code );
        }
        return result;
    }
    
    /**
     * This will perform a lookup into the CID map.
     *
     * @param cid The CID used to lookup.
     *
     * @return The string that matches the lookup.
     */
    public String lookupCID(int cid) 
    {
        if (cid2charMappings.containsKey(cid)) 
        {
            return cid2charMappings.get(cid);
        } 
        else 
        {
            for (CIDRange range : cidRanges) 
            {
                int ch = range.unmap(cid);
                if (ch != -1) 
                {
                    return Character.toString((char) ch);
                }
            }
            return null;
        }
    }

    /**
     * This will perform a lookup into the CID map.
     *
     * @param code The code used to lookup.
     * @param offset the offset into the array.
     * @param length the length of the subarray.
     *
     * @return The CID that matches the lookup.
     */
    public int lookupCID(byte[] code, int offset, int length) 
    {
        if (isInCodeSpaceRanges(code,offset,length)) 
        {
            int codeAsInt = getCodeFromArray(code, offset, length);
            if (char2CIDMappings.containsKey(codeAsInt)) 
            {
                return char2CIDMappings.get(codeAsInt);
            } 
            else 
            {
                for (CIDRange range : cidRanges) 
                {
                    int ch = range.map((char)codeAsInt);
                    if (ch != -1) 
                    {
                        return ch;
                    }
                }
                return -1;
            }
        }
        return -1;
    }
    
    /**
     * Convert the given part of a byte array to an integer.
     * @param data the byte array
     * @param offset The offset into the byte array.
     * @param length The length of the data we are getting.
     * @return the resulting integer
     */
    private int getCodeFromArray( byte[] data, int offset, int length )
    {
        int code = 0;
        for( int i=0; i<length; i++ )
        {
            code <<= 8;
            code |= (data[offset+i]+256)%256;
        }
        return code;
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
        
        int srcLength = src.length;
        int intSrc = getCodeFromArray(src, 0, srcLength);
        if ( SPACE.equals(dest) )
        {
            spaceMapping = intSrc;
        }
        if( srcLength == 1 )
        {
            singleByteMappings.put( intSrc, dest );
        }
        else if( srcLength == 2 )
        {
            doubleByteMappings.put( intSrc, dest );
        }
        else
        {
            throw new IOException( "Mapping code should be 1 or two bytes and not " + src.length );
        }
    }

    /**
     * This will add a CID mapping.
     *
     * @param src The CID to the mapping.
     * @param dest The dest to the mapping.
     *
     * @throws IOException if the src is invalid.
     */
    public void addCIDMapping( int src, String dest ) throws IOException
    {
        cid2charMappings.put( src, dest );
        char2CIDMappings.put( dest, src );
    }

    /**
     * This will add a CID Range.
     *
     * @param from starting charactor of the CID range.
     * @param to ending character of the CID range.
     * @param cid the cid to be started with.
     *
     */
    public void addCIDRange(char from, char to, int cid) 
    {
        cidRanges.add(0, new CIDRange(from, to, cid));
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
    public List<CodespaceRange> getCodeSpaceRanges()
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

    /**
     *  Check whether the given byte array is in codespace ranges or not.
     *  
     *  @param code The byte array to look for in the codespace range.
     *  
     *  @return true if the given byte array is in the codespace range.
     */
    public boolean isInCodeSpaceRanges( byte[] code )
    {
        return isInCodeSpaceRanges(code, 0, code.length);
    }
 
    /**
     *  Check whether the given byte array is in codespace ranges or not.
     *  
     *  @param code The byte array to look for in the codespace range.
     *  @param offset The starting offset within the byte array.
     *  @param length The length of the part of the array.
     *  
     *  @return true if the given byte array is in the codespace range.
     */
    public boolean isInCodeSpaceRanges( byte[] code, int offset, int length )
    {
        Iterator<CodespaceRange> it = codeSpaceRanges.iterator();
        while ( it.hasNext() ) 
        {
            CodespaceRange range = it.next();
            if ( range != null && range.isInRange(code, offset, length) )
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the WMode of a CMap.
     *
     * 0 represents a horizontal and 1 represents a vertical orientation.
     * 
     * @return the wmode
     */
    public int getWMode() 
    {
        return wmode;
    }

    /**
     * Sets the WMode of a CMap.
     * 
     * @param newWMode the new WMode.
     */
    public void setWMode(int newWMode) 
    {
        wmode = newWMode;
    }

    /**
     * Returns the name of the CMap.
     * 
     * @return the CMap name.
     */
    public String getName() 
    {
        return cmapName;
    }

    /**
     * Sets the name of the CMap.
     * 
     * @param name the CMap name.
     */
    public void setName(String name) 
    {
        cmapName = name;
    }

    /**
     * Returns the version of the CMap.
     * 
     * @return the CMap version.
     */
    public String getVersion() 
    {
        return cmapVersion;
    }

    /**
     * Sets the version of the CMap.
     * 
     * @param version the CMap version.
     */
    public void setVersion(String version) 
    {
        cmapVersion = version;
    }

    /**
     * Returns the type of the CMap.
     * 
     * @return the CMap type.
     */
    public int getType() 
    {
        return cmapType;
    }

    /**
     * Sets the type of the CMap.
     * 
     * @param type the CMap type.
     */
    public void setType(int type) 
    {
        cmapType = type;
    }

    /**
     * Returns the registry of the CIDSystemInfo.
     * 
     * @return the registry.
     */
    public String getRegistry() 
    {
        return registry;
    }

    /**
     * Sets the registry of the CIDSystemInfo.
     * 
     * @param newRegistry the registry.
     */
    public void setRegistry(String newRegistry) 
    {
        registry = newRegistry;
    }

    /**
     * Returns the ordering of the CIDSystemInfo.
     * 
     * @return the ordering.
     */
    public String getOrdering() 
    {
        return ordering;
    }

    /**
     * Sets the ordering of the CIDSystemInfo.
     * 
     * @param newOrdering the ordering.
     */
    public void setOrdering(String newOrdering) 
    {
        ordering = newOrdering;
    }

    /**
     * Returns the supplement of the CIDSystemInfo.
     * 
     * @return the supplement.
     */
    public int getSupplement() 
    {
        return supplement;
    }

    /**
     * Sets the supplement of the CIDSystemInfo.
     * 
     * @param newSupplement the supplement.
     */
    public void setSupplement(int newSupplement) 
    {
        supplement = newSupplement;
    }
    
    /** 
     * Returns the mapping for the space character.
     * 
     * @return the mapped code for the space character
     */
    public int getSpaceMapping()
    {
        return spaceMapping;
    }
}