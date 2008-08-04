/**
 * Copyright (c) 2003-2004, www.pdfbox.org
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
package org.pdfbox.encoding;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.pdfbox.cos.COSName;

import org.pdfbox.util.ResourceLoader;

import org.pdfbox.pdmodel.common.COSObjectable;

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
        BufferedReader glyphStream = null;
        try
        {
            InputStream resource = ResourceLoader.loadResource( "Resources/glyphlist.txt" );
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