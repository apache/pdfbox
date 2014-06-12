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

package org.apache.fontbox.type1;

/**
 * A lexical token in an Adobe Type 1 font.
 *
 * @see Type1Lexer
 *
 * @author John Hewson
 */
class Token
{
    /**
     * All different types of tokens.  
     *
     */
    static enum Kind
    {
        NONE, STRING, NAME, LITERAL, REAL, INTEGER,
        START_ARRAY,  END_ARRAY, START_PROC,
        END_PROC, CHARSTRING
    }

    // exposed statically for convenience
    static final Kind STRING = Kind.STRING;
    static final Kind NAME = Kind.NAME;
    static final Kind LITERAL = Kind.LITERAL;
    static final Kind REAL = Kind.REAL;
    static final Kind INTEGER = Kind.INTEGER;
    static final Kind START_ARRAY = Kind.START_ARRAY;
    static final Kind END_ARRAY = Kind.END_ARRAY;
    static final Kind START_PROC = Kind.START_PROC;
    static final Kind END_PROC = Kind.END_PROC;
    static final Kind CHARSTRING = Kind.CHARSTRING;

    private String text;
    private byte[] data;
    private Kind kind;

    /**
     * Constructs a new Token object given its text and kind.
     * @param text
     * @param type
     */
    public Token(String text, Kind type)
    {
        this.text = text;
        this.kind = type;
    }

    /**
     * Constructs a new Token object given its single-character text and kind.
     * @param character
     * @param type
     */
    public Token(char character, Kind type)
    {
        this.text = Character.toString(character);
        this.kind = type;
    }

    /**
     * Constructs a new Token object given its raw data and kind.
     * This is for CHARSTRING tokens only.
     * @param data
     * @param type
     */
    public Token(byte[] data, Kind type)
    {
        this.data = data;
        this.kind = type;
    }

    public String getText()
    {
        return text;
    }

    public Kind getKind()
    {
        return kind;
    }

    public int intValue()
    {
        // some fonts have reals where integers should be, so we tolerate it
        return (int)Float.parseFloat(text);
    }

    public float floatValue()
    {
        return Float.parseFloat(text);
    }

    public boolean booleanValue()
    {
        return text.equals("true");
    }

    public byte[] getData()
    {
        return data;
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        if (kind == CHARSTRING)
        {
            return "Token[kind=CHARSTRING, data=" + data.length + " bytes]";
        }
        else
        {
            return "Token[kind=" + kind + ", text=" + text + "]";
        }
    }
}