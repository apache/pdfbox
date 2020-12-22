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
package org.apache.fontbox.encoding;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A PostScript Encoding vector.
 *
 * @author Ben Litchfield
 */
public abstract class Encoding
{
    /**
     * This is a mapping from a character code to a character name.
     */
    protected final Map<Integer,String> codeToName = new HashMap<>(250);

    /**
     * This is a mapping from a character name to a character code.
     */
    protected final Map<String,Integer> nameToCode = new HashMap<>(250);

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
     * @return The code for the character or null if it is not in the encoding.
     */
    public Integer getCode( String name )
    {
        return nameToCode.get( name );
    }

    /**
     * This will take a character code and get the name from the code. This method will never return
     * null.
     *
     * @param code The character code.
     * @return The name of the character, or ".notdef" if the bame doesn't exist.
     */
    public String getName( int code )
    {
        String name = codeToName.get( code );
        if (name != null)
        {
            return name;
        }
        return ".notdef";
    }
    
    /**
     * Returns an unmodifiable view of the code to name mapping.
     * 
     * @return the Code2Name map
     */
    public Map<Integer, String> getCodeToNameMap()
    {
        return Collections.unmodifiableMap(codeToName);
    }
}
