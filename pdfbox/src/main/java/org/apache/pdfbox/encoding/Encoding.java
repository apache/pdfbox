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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.util.ResourceLoader;

/**
 * This is an interface to a text encoder.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.15 $
 */
public abstract class Encoding implements COSObjectable {

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(Encoding.class);

    /**
     * This is a mapping from a character code to a character name.
     */
//    protected final Map<Integer, COSName> codeToName =
//        new HashMap<Integer, COSName>();
    protected final Map<Integer, String> codeToName =
        new HashMap<Integer, String>();

    /**
     * This is a mapping from a character name to a character code.
     */
//    protected final Map<COSName, Integer> nameToCode =
//        new HashMap<COSName, Integer>();
    protected final Map<String, Integer> nameToCode =
        new HashMap<String, Integer>();

//    private static final Map<COSName, String> NAME_TO_CHARACTER =
//        new HashMap<COSName, String>();
    private static final Map<String, String> NAME_TO_CHARACTER =
        new HashMap<String, String>();

//    private static final Map<String, COSName> CHARACTER_TO_NAME =
//        new HashMap<String, COSName>();
    private static final Map<String, String> CHARACTER_TO_NAME =
        new HashMap<String, String>();

    static
    {
        //Loads the official Adobe Glyph List
        loadGlyphList("Resources/glyphlist.txt");

        // Load an external glyph list file that user can give as JVM property
        String location = System.getProperty("glyphlist_ext");
        if(location != null)
        {
            File external = new File(location);
            if(external.exists())
            {
                loadGlyphList(location);
            }
        }

        NAME_TO_CHARACTER.put( ".notdef", "" );
        NAME_TO_CHARACTER.put( "fi", "fi" );
        NAME_TO_CHARACTER.put( "fl", "fl" );
        NAME_TO_CHARACTER.put( "ffi", "ffi" );
        NAME_TO_CHARACTER.put( "ff", "ff" );
        NAME_TO_CHARACTER.put( "pi", "pi" );

        // add some (alternative) glyph mappings. These are missing in
        // the original copy of the adobe glyphlist.txt 
        // also mapped as anglebracketleft
        NAME_TO_CHARACTER.put("angbracketleft", "\u3008");
        // also mapped as anglebracketright
        NAME_TO_CHARACTER.put("angbracketright", "\u3009");
        // also mapped as copyright
        NAME_TO_CHARACTER.put("circlecopyrt", "\u00A9");
        NAME_TO_CHARACTER.put("controlNULL", "\u0000");

        for( Map.Entry<String, String> entry : NAME_TO_CHARACTER.entrySet() )
        {
            CHARACTER_TO_NAME.put( entry.getValue(), entry.getKey() );
        }
    }

    /**
     * Loads a glyph list from a given location and populates the NAME_TO_CHARACTER hashmap
     * for character lookups.
     * @param location - The string location of the glyphlist file 
     */
    private static void loadGlyphList(String location)
    {
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
                            NAME_TO_CHARACTER.put( characterName, value );
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
     * Returns an unmodifiable view of the Code2Name mapping.
     * @return the Code2Name map 
     */
    public Map<Integer, String> getCodeToNameMap()
    {
        return Collections.unmodifiableMap(codeToName);
    }
      
    /**
     * Returns an unmodifiable view of the Name2Code mapping.
     * @return the Name2Code map 
     */
    public Map<String, Integer> getNameToCodeMap()
    {
        return Collections.unmodifiableMap(nameToCode);
    }

    /**
     * This will add a character encoding.
     *
     * @param code The character code that matches the character.
     * @param name The name of the character.
     */
    protected void addCharacterEncoding( int code, String name )
    {
        codeToName.put( code, name );
        nameToCode.put( name, code );
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
    public int getCode( String name ) throws IOException
    {
        Integer code = nameToCode.get( name );
        if( code == null )
        {
            throw new IOException( "No character code for character name '" + name + "'" );
        }
        return code;
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
    public String getName( int code ) throws IOException
    {
        String name = codeToName.get( code );
        if( name == null )
        {
            //lets be forgiving for now
            name = "space";
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
    public String getNameFromCharacter( char c ) throws IOException
    {
        String name = CHARACTER_TO_NAME.get( Character.toString(c) );
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
    public static String getCharacter( String name )
    {
        String baseName = name;
 
        String character = NAME_TO_CHARACTER.get( baseName );
        if( character == null )
        {
            String nameStr = baseName;
            // test for Unicode name
            // (uniXXXX - XXXX must be a multiple of four;
            // each representing a hexadecimal Unicode code point)
            if ( nameStr.startsWith( "uni" ) )
            {
                StringBuilder uniStr = new StringBuilder();

                for ( int chPos = 3; chPos + 4 <= nameStr.length(); chPos += 4 )
                {
                    try 
                    {
                        int characterCode = Integer.parseInt( nameStr.substring( chPos, chPos + 4), 16 );

                        if ( ( characterCode > 0xD7FF ) && ( characterCode < 0xE000 ) )
                        {
                            log.warn( "Unicode character name with not allowed code area: " + nameStr );
                        }
                        else
                        {
                            uniStr.append( (char) characterCode );
                        }
                    } 
                    catch (NumberFormatException nfe) 
                    {
                        log.warn( "Not a number in Unicode character name: " + nameStr );
                    }
                }
                character = uniStr.toString();
            }
            else 
            {
                // test if we have a suffix and if so remove it
                if ( nameStr.indexOf('.') > 0 ) 
                {
                    nameStr = nameStr.substring( 0, nameStr.indexOf('.') );
                    baseName = nameStr;
                    getCharacter(baseName);
                }

               character = nameStr;
            }
        }
        return character;
    } 

}
