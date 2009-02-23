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
package org.apache.pdfbox.encoding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.io.File;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.util.ResourceLoader;

/**
 * This is an interface to a text encoder.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.15 $
 */
public abstract class Encoding implements COSObjectable
{
    /**
     * This is a mapping from a character code to a character name.
     */
    protected Map codeToName = new HashMap();
    /**
     * This is a mapping from a character name to a character code.
     */
    protected Map nameToCode = new HashMap();

    private static final Map NAME_TO_CHARACTER = new HashMap();
    private static final Map CHARACTER_TO_NAME = new HashMap();

    static
    {
        //Loads the official Adobe Glyph List
        loadGlyphList("Resources/glyphlist.txt");

        // Load an external glyph list file that user can give as JVM property
        String location = System.getProperty("glyphlist_ext");
        if(location != null){
            File external = new File(location);
            if(external.exists()){
                loadGlyphList(location);
            }
        }

        NAME_TO_CHARACTER.put( COSName.getPDFName( ".notdef" ), "" );
        NAME_TO_CHARACTER.put( COSName.getPDFName( "fi" ), "fi" );
        NAME_TO_CHARACTER.put( COSName.getPDFName( "fl" ), "fl" );
        NAME_TO_CHARACTER.put( COSName.getPDFName( "ffi" ), "ffi" );
        NAME_TO_CHARACTER.put( COSName.getPDFName( "ff" ), "ff" );
        NAME_TO_CHARACTER.put( COSName.getPDFName( "pi" ), "pi" );

        Iterator keys = NAME_TO_CHARACTER.keySet().iterator();
        while( keys.hasNext() )
        {
            Object key = keys.next();
            Object value = NAME_TO_CHARACTER.get( key );
            CHARACTER_TO_NAME.put( value, key );
        }
    }
    
    /**
     * Loads a glyph list from a given location and populates the NAME_TO_CHARACTER hashmap
     * for character lookups
     * @param location - The string location of the glyphlist file 
     */
    private static void loadGlyphList(String location){
        BufferedReader glyphStream = null;
        try
        {
            InputStream resource = ResourceLoader.loadResource( location );
            glyphStream = new BufferedReader( new InputStreamReader( resource ) );
            String line = null;
            while( (line = glyphStream.readLine()) != null )
            {
                line = line.trim();
                //lines starting with # are comments which we can ignore.
                if( !line.startsWith("#" ) )
                {
                    int semicolonIndex = line.indexOf( ';' );
                    if( semicolonIndex >= 0 )
                    {
                        try
                        {
                            String characterName = line.substring( 0, semicolonIndex );
                            String unicodeValue = line.substring( semicolonIndex+1, line.length() );
                            StringTokenizer tokenizer = new StringTokenizer( unicodeValue, " ", false );
                            String value = "";
                            while(tokenizer.hasMoreTokens())
                            {
                                int characterCode = Integer.parseInt( tokenizer.nextToken(), 16 );
                                value += (char)characterCode;
                            }
                            NAME_TO_CHARACTER.put( COSName.getPDFName( characterName ), value );
                        }
                        catch( NumberFormatException nfe )
                        {
                            nfe.printStackTrace();
                        }
                    }
                }
            }
        }
        catch( IOException io )
        {
            io.printStackTrace();
        }
        finally
        {
            if( glyphStream != null )
            {
                try
                {
                    glyphStream.close();
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }

            }
        }
    }
    


    /**
     * This will add a character encoding.
     *
     * @param code The character code that matches the character.
     * @param name The name of the character.
     */
    protected void addCharacterEncoding( int code, COSName name )
    {
        Integer intCode = new Integer( code );
        codeToName.put( intCode, name );
        nameToCode.put( name, intCode );
    }

    /**
     * This will get the character code for the name.
     *
     * @param name The name of the character.
     *
     * @return The code for the character.
     *
     * @throws IOException If there is no character code for the name.
     */
    public int getCode( COSName name ) throws IOException
    {
        Integer code = (Integer)nameToCode.get( name );
        if( code == null )
        {
            throw new IOException( "No character code for character name '" + name.getName() + "'" );
        }
        return code.intValue();
    }

    /**
     * This will take a character code and get the name from the code.
     *
     * @param code The character code.
     *
     * @return The name of the character.
     *
     * @throws IOException If there is no name for the code.
     */
    public COSName getName( int code ) throws IOException
    {
        COSName name = (COSName)codeToName.get( new Integer( code ) );
        if( name == null )
        {
            //lets be forgiving for now
            name = COSName.getPDFName( "space" );
            //throw new IOException( getClass().getName() +
            //                       ": No name for character code '" + code + "'" );
        }
        return name;
    }

    /**
     * This will take a character code and get the name from the code.
     *
     * @param c The character.
     *
     * @return The name of the character.
     *
     * @throws IOException If there is no name for the character.
     */
    public COSName getNameFromCharacter( char c ) throws IOException
    {
        COSName name = (COSName)CHARACTER_TO_NAME.get( "" + c );
        if( name == null )
        {
            throw new IOException( "No name for character '" + c + "'" );
        }
        return name;
    }

    /**
     * This will get the character from the code.
     *
     * @param code The character code.
     *
     * @return The printable character for the code.
     *
     * @throws IOException If there is not name for the character.
     */
    public String getCharacter( int code ) throws IOException
    {
        String character = getCharacter( getName( code ) );
        return character;
    }

    /**
     * This will get the character from the name.
     *
     * @param name The name of the character.
     *
     * @return The printable character for the code.
     */
    public static String getCharacter( COSName name )
    {
        String character = (String)NAME_TO_CHARACTER.get( name );
        if( character == null )
        {
            character = name.getName();
        }
        return character;
    }
}
