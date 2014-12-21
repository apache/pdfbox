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

import org.apache.fontbox.encoding.CustomEncoding;
import org.apache.fontbox.encoding.StandardEncoding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses an Adobe Type 1 (.pfb) font. It is used exclusively by Type1Font.
 *
 * The Type 1 font format is a free-text format which is somewhat difficult
 * to parse. This is made worse by the fact that many Type 1 font files do
 * not conform to the specification, especially those embedded in PDFs. This
 * parser therefore tries to be as forgiving as possible.
 *
 * @see "Adobe Type 1 Font Format, Adobe Systems (1999)"
 *
 * @author John Hewson
 */
final class Type1Parser
{
    // constants for encryption
    private static final int EEXEC_KEY = 55665;
    private static final int CHARSTRING_KEY = 4330;

    // state
    private Type1Lexer lexer;
    private Type1Font font;

    /**
     * Parses a Type 1 font and returns a Type1Font class which represents it.
     *
     * @param segment1 Segment 1: ASCII
     * @param segment2 Segment 2: Binary
     * @throws IOException
     */
    public Type1Font parse(byte[] segment1, byte[] segment2) throws IOException
    {
        font = new Type1Font();
        parseASCII(segment1);
        if (segment2.length > 0)
        {
            parseBinary(segment2);
        }
        return font;
    }

    /**
     * Parses the ASCII portion of a Type 1 font.
     */
    private void parseASCII(byte[] bytes) throws IOException
    {
        if (bytes.length == 0)
        {
            throw new IllegalArgumentException("byte[] is empty");
        }

        // %!FontType1-1.0
        // %!PS-AdobeFont-1.0
        if (bytes.length < 2 || (bytes[0] != '%' && bytes[1] != '!'))
        {
            throw new IOException("Invalid start of ASCII segment");
        }

        lexer = new Type1Lexer(bytes);

        // (corrupt?) synthetic font
        if (lexer.peekToken().getText().equals("FontDirectory"))
        {
            read(Token.NAME, "FontDirectory");
            read(Token.LITERAL); // font name
            read(Token.NAME, "known");
            read(Token.START_PROC);
            readProc();
            read(Token.START_PROC);
            readProc();
            read(Token.NAME, "ifelse");
        }

        // font dict
        int length = read(Token.INTEGER).intValue();
        read(Token.NAME, "dict");
        readMaybe(Token.NAME, "dup"); // found in some TeX fonts
        read(Token.NAME, "begin");

        for (int i = 0; i < length; i++)
        {
            // premature end
            if (lexer.peekToken().getKind() == Token.NAME &&
                lexer.peekToken().getText().equals("currentdict"))
            {
                break;
            }

            // key/value
            String key = read(Token.LITERAL).getText();
            if (key.equals("FontInfo"))
            {
                readFontInfo(readSimpleDict());
            }
            else if (key.equals("Metrics"))
            {
                readSimpleDict();
            }
            else if (key.equals("Encoding"))
            {
                readEncoding();
            }
            else
            {
                readSimpleValue(key);
            }
        }

        read(Token.NAME, "currentdict");
        read(Token.NAME, "end");

        read(Token.NAME, "currentfile");
        read(Token.NAME, "eexec");
    }

    private void readSimpleValue(String key) throws IOException
    {
        List<Token> value = readDictValue();
        
        if (key.equals("FontName"))
        {
            font.fontName = value.get(0).getText();
        }
        else if (key.equals("PaintType"))
        {
            font.paintType = value.get(0).intValue();
        }
        else if (key.equals("FontType"))
        {
            font.fontType = value.get(0).intValue();
        }
        else if (key.equals("FontMatrix"))
        {
            font.fontMatrix = arrayToNumbers(value);
        }
        else if (key.equals("FontBBox"))
        {
            font.fontBBox = arrayToNumbers(value);
        }
        else if (key.equals("UniqueID"))
        {
            font.uniqueID = value.get(0).intValue();
        }
        else if (key.equals("StrokeWidth"))
        {
            font.strokeWidth = value.get(0).floatValue();
        }
        else if (key.equals("FID"))
        {
            font.fontID = value.get(0).getText();
        }
    }

    private void readEncoding() throws IOException
    {
        if (lexer.peekToken().getKind() == Token.NAME)
        {
            String name = lexer.nextToken().getText();
            
            if (name.equals("StandardEncoding"))
            {
                font.encoding = StandardEncoding.INSTANCE;
            }
            else
            {
                throw new IOException("Unknown encoding: " + name);
            }
            readMaybe(Token.NAME, "readonly");
            read(Token.NAME, "def");
        }
        else
        {
            read(Token.INTEGER).intValue();
            readMaybe(Token.NAME, "array");
            
            // 0 1 255 {1 index exch /.notdef put } for
            // we have to check "readonly" and "def" too
            // as some fonts don't provide any dup-values, see PDFBOX-2134
            while (!(lexer.peekToken().getKind() == Token.NAME &&
                    (lexer.peekToken().getText().equals("dup") ||
                    lexer.peekToken().getText().equals("readonly") ||
                    lexer.peekToken().getText().equals("def"))))
            {
                lexer.nextToken();
            }
            
            Map<Integer, String> codeToName = new HashMap<Integer, String>();
            while (lexer.peekToken().getKind() == Token.NAME &&
                    lexer.peekToken().getText().equals("dup"))
            {
                read(Token.NAME, "dup");
                int code = read(Token.INTEGER).intValue();
                String name = read(Token.LITERAL).getText();
                read(Token.NAME, "put");
                codeToName.put(code, name);
            }
            font.encoding = new CustomEncoding(codeToName);
            readMaybe(Token.NAME, "readonly");
            read(Token.NAME, "def");
        }
    }

    /**
     * Extracts values from an array as numbers.
     */
    private List<Number> arrayToNumbers(List<Token> value) throws IOException
    {
        List<Number> numbers = new ArrayList<Number>();
        for (int i = 1, size = value.size() - 1; i < size; i++)
        {
            Token token = value.get(i);
            if (token.getKind() == Token.REAL)
            {
                numbers.add(token.floatValue());
            }
            else if (token.getKind() == Token.INTEGER)
            {
                numbers.add(token.intValue());
            }
            else
            {
               throw new IOException("Expected INTEGER or REAL but got " + token.getKind());
            }
        }
        return numbers;
    }

    /**
     * Extracts values from the /FontInfo dictionary.
     */
    private void readFontInfo(Map<String, List<Token>> fontInfo)
    {
        for (Map.Entry<String, List<Token>> entry : fontInfo.entrySet())
        {
            String key = entry.getKey();
            List<Token> value = entry.getValue();

            if (key.equals("version"))
            {
                font.version = value.get(0).getText();
            }
            else if (key.equals("Notice"))
            {
                font.notice = value.get(0).getText();
            }
            else if (key.equals("FullName"))
            {
                font.fullName = value.get(0).getText();
            }
            else if (key.equals("FamilyName"))
            {
                font.familyName = value.get(0).getText();
            }
            else if (key.equals("Weight"))
            {
                font.weight = value.get(0).getText();
            }
            else if (key.equals("ItalicAngle"))
            {
                font.italicAngle = value.get(0).floatValue();
            }
            else if (key.equals("isFixedPitch"))
            {
                font.isFixedPitch = value.get(0).booleanValue();
            }
            else if (key.equals("UnderlinePosition"))
            {
                font.underlinePosition = value.get(0).floatValue();
            }
            else if (key.equals("UnderlineThickness"))
            {
                font.underlineThickness = value.get(0).floatValue();
            }
        }
    }

    /**
     * Reads a dictionary whose values are simple, i.e., do not contain
     * nested dictionaries.
     */
    private Map<String, List<Token>> readSimpleDict() throws IOException
    {
        Map<String, List<Token>> dict = new HashMap<String, List<Token>>();

        int length = read(Token.INTEGER).intValue();
        read(Token.NAME, "dict");
        readMaybe(Token.NAME, "dup");
        read(Token.NAME, "begin");

        for (int i = 0; i < length; i++)
        {
            if (lexer.peekToken().getKind() == Token.NAME &&
               !lexer.peekToken().getText().equals("end"))
            {
                read(Token.NAME);
            }
            // premature end
            if (lexer.peekToken().getKind() == Token.NAME &&
                lexer.peekToken().getText().equals("end"))
            {
                break;
            }

            // simple value
            String key = read(Token.LITERAL).getText();
            List<Token> value = readDictValue();
            dict.put(key, value);
        }

        read(Token.NAME, "end");
        readMaybe(Token.NAME, "readonly");
        read(Token.NAME, "def");

        return dict;
    }

    /**
     * Reads a simple value from a dictionary.
     */
    private List<Token> readDictValue() throws IOException
    {
        List<Token> value = readValue();
        readDef();
        return value;
    }

    /**
     * Reads a simple value. This is either a number, a string,
     * a name, a literal name, an array, a procedure, or a charstring.
     * This method does not support reading nested dictionaries.
     */
    private List<Token> readValue() throws IOException
    {
        List<Token> value = new ArrayList<Token>();
        Token token = lexer.nextToken();
        value.add(token);

        if (token.getKind() == Token.START_ARRAY)
        {
            int openArray = 1;
            while (true)
            {
                if (lexer.peekToken().getKind() == Token.START_ARRAY)
                {
                    openArray++;
                }

                token = lexer.nextToken();
                value.add(token);

                if (token.getKind() == Token.END_ARRAY)
                {
                    openArray--;
                    if (openArray == 0)
                    {
                        break;
                    }
                }
            }
        }
        else if (token.getKind() == Token.START_PROC)
        {
            value.addAll(readProc());
        }

        // postscript wrapper (not in the Type 1 spec)
        if (lexer.peekToken().getText().equals("systemdict"))
        {
            read(Token.NAME, "systemdict");
            read(Token.LITERAL, "internaldict");
            read(Token.NAME, "known");

            read(Token.START_PROC);
            readProc();

            read(Token.START_PROC);
            readProc();

            read(Token.NAME, "ifelse");

            // replace value
            read(Token.START_PROC);
            read(Token.NAME, "pop");
            value.clear();
            value.addAll(readValue());
            read(Token.END_PROC);

            read(Token.NAME, "if");
        }
        return value;
    }

    /**
     * Reads a procedure.
     */
    private List<Token> readProc() throws IOException
    {
        List<Token> value = new ArrayList<Token>();

        int openProc = 1;
        while (true)
        {
            if (lexer.peekToken().getKind() == Token.START_PROC)
            {
                openProc++;
            }

            Token token = lexer.nextToken();
            value.add(token);

            if (token.getKind() == Token.END_PROC)
            {
                openProc--;
                if (openProc == 0)
                {
                    break;
                }
            }
        }
        Token executeonly = readMaybe(Token.NAME, "executeonly");
        if (executeonly != null)
        {
            value.add(executeonly);
        }

        return value;
    }

    /**
     * Parses the binary portion of a Type 1 font.
     */
    private void parseBinary(byte[] bytes) throws IOException
    {
        byte[] decrypted = decrypt(bytes, EEXEC_KEY, 4);
        lexer = new Type1Lexer(decrypted);

        // find /Private dict
        while (!lexer.peekToken().getText().equals("Private"))
        {
            lexer.nextToken();
        }

        // Private dict
        read(Token.LITERAL, "Private");
        int length = read(Token.INTEGER).intValue();
        read(Token.NAME, "dict");
        readMaybe(Token.NAME, "dup");
        read(Token.NAME, "begin");

        int lenIV = 4; // number of random bytes at start of charstring

        for (int i = 0; i < length; i++)
        {
            // premature end
            if (lexer.peekToken().getKind() != Token.LITERAL)
            {
                break;
            }

            // key/value
            String key = read(Token.LITERAL).getText();

            if (key.equals("Subrs"))
            {
                readSubrs(lenIV);
            }
            else if (key.equals("OtherSubrs"))
            {
                readOtherSubrs();
            }
            else if (key.equals("lenIV"))
            {
                lenIV = readDictValue().get(0).intValue();
            }
            else if (key.equals("ND"))
            {
                read(Token.START_PROC);
                read(Token.NAME, "noaccess");
                read(Token.NAME, "def");
                read(Token.END_PROC);
                read(Token.NAME, "executeonly");
                read(Token.NAME, "def");
            }
            else if (key.equals("NP"))
            {
                read(Token.START_PROC);
                read(Token.NAME, "noaccess");
                read(Token.NAME);
                read(Token.END_PROC);
                read(Token.NAME, "executeonly");
                read(Token.NAME, "def");
            }
            else
            {
                readPrivate(key, readDictValue());
            }
        }

        // some fonts have "2 index" here, others have "end noaccess put"
        // sometimes followed by "put". Either way, we just skip until
        // the /CharStrings dict is found
        while (!(lexer.peekToken().getKind() == Token.LITERAL &&
                 lexer.peekToken().getText().equals("CharStrings")))
        {
            lexer.nextToken();
        }

        // CharStrings dict
        read(Token.LITERAL, "CharStrings");
        readCharStrings(lenIV);
    }

    /**
     * Extracts values from the /Private dictionary.
     */
    private void readPrivate(String key, List<Token> value) throws IOException
    {
        if (key.equals("BlueValues"))
        {
            font.blueValues = arrayToNumbers(value);
        }
        else if (key.equals("OtherBlues"))
        {
            font.otherBlues = arrayToNumbers(value);
        }
        else if (key.equals("FamilyBlues"))
        {
            font.familyBlues = arrayToNumbers(value);
        }
        else if (key.equals("FamilyOtherBlues"))
        {
            font.familyOtherBlues = arrayToNumbers(value);
        }
        else if (key.equals("BlueScale"))
        {
            font.blueScale = value.get(0).floatValue();
        }
        else if (key.equals("BlueShift"))
        {
            font.blueShift = value.get(0).intValue();
        }
        else if (key.equals("BlueFuzz"))
        {
            font.blueFuzz = value.get(0).intValue();
        }
        else if (key.equals("StdHW"))
        {
            font.stdHW = arrayToNumbers(value);
        }
        else if (key.equals("StdVW"))
        {
            font.stdVW = arrayToNumbers(value);
        }
        else if (key.equals("StemSnapH"))
        {
            font.stemSnapH = arrayToNumbers(value);
        }
        else if (key.equals("StemSnapV"))
        {
            font.stemSnapV = arrayToNumbers(value);
        }
        else if (key.equals("ForceBold"))
        {
            font.forceBold = value.get(0).booleanValue();
        }
        else if (key.equals("LanguageGroup"))
        {
            font.languageGroup = value.get(0).intValue();
        }
    }

    /**
     * Reads the /Subrs array.
     * @param lenIV The number of random bytes used in charstring encryption.
     */
    private void readSubrs(int lenIV) throws IOException
    {
        // allocate size (array indexes may not be in-order)
        int length = read(Token.INTEGER).intValue();
        for (int i = 0; i < length; i++)
        {
            font.subrs.add(null);
        }
        read(Token.NAME, "array");

        for (int i = 0; i < length; i++)
        {
            // premature end
            if (!(lexer.peekToken().getKind() == Token.NAME &&
                  lexer.peekToken().getText().equals("dup")))
            {
                break;
            }

            read(Token.NAME, "dup");
            Token index = read(Token.INTEGER);
            read(Token.INTEGER);

            // RD
            Token charstring = read(Token.CHARSTRING);
            font.subrs.set(index.intValue(), decrypt(charstring.getData(), CHARSTRING_KEY, lenIV));
            readPut();
        }
        readDef();
    }

    // OtherSubrs are embedded PostScript procedures which we can safely ignore
    private void readOtherSubrs() throws IOException
    {
        if (lexer.peekToken().getKind() == Token.START_ARRAY)
        {
            readValue();
            readDef();
        }
        else
        {
            int length = read(Token.INTEGER).intValue();
            read(Token.NAME, "array");

            for (int i = 0; i < length; i++)
            {
                read(Token.NAME, "dup");
                read(Token.INTEGER); // index
                readValue(); // PostScript
                readPut();
            }
            readDef();
        }
    }

    /**
     * Reads the /CharStrings dictionary.
     * @param lenIV The number of random bytes used in charstring encryption.
     */
    private void readCharStrings(int lenIV) throws IOException
    {
        int length = read(Token.INTEGER).intValue();
        read(Token.NAME, "dict");
        read(Token.NAME, "dup");
        read(Token.NAME, "begin");

        for (int i = 0; i < length; i++)
        {
            // premature end
            if (lexer.peekToken().getKind() == Token.NAME &&
                lexer.peekToken().getText().equals("end"))
            {
                break;
            }
            // key/value
            String name = read(Token.LITERAL).getText();

            // RD
            read(Token.INTEGER);
            Token charstring = read(Token.CHARSTRING);
            font.charstrings.put(name, decrypt(charstring.getData(), CHARSTRING_KEY, lenIV));
            readDef();
        }

        // some fonts have one "end", others two
        read(Token.NAME, "end");
    }

    /**
     * Reads the sequence "noaccess def" or equivalent.
     */
    private void readDef() throws IOException
    {
        readMaybe(Token.NAME, "readonly");
        readMaybe(Token.NAME, "noaccess"); // allows "noaccess ND" (not in the Type 1 spec)

        Token token = read(Token.NAME);
        if (token.getText().equals("ND") || token.getText().equals("|-"))
        {
            return;
        }
        else if (token.getText().equals("noaccess"))
        {
            token = read(Token.NAME);
        }

        if (token.getText().equals("def"))
        {
            return;
        }
        throw new IOException("Found " + token + " but expected ND");
    }

    /**
     * Reads the sequence "noaccess put" or equivalent.
     */
    private void readPut() throws IOException
    {
        readMaybe(Token.NAME, "readonly");

        Token token = read(Token.NAME);
        if (token.getText().equals("NP") || token.getText().equals("|"))
        {
            return;
        }
        else if (token.getText().equals("noaccess")) 
        {
            token = read(Token.NAME);
        }

        if (token.getText().equals("put")) 
        {
            return;
        }
        throw new IOException("Found " + token + " but expected NP");
    }

    /**
     * Reads the next token and throws an error if it is not of the given kind.
     */
    private Token read(Token.Kind kind) throws IOException
    {
        Token token = lexer.nextToken();
        if (token.getKind() != kind)
        {
            throw new IOException("Found " + token + " but expected " + kind);
        }
        return token;
    }

    /**
     * Reads the next token and throws an error if it is not of the given kind
     * and does not have the given value.
     */
    private void read(Token.Kind kind, String name) throws IOException
    {
        Token token = read(kind);
        if (!token.getText().equals(name))
        {
            throw new IOException("Found " + token + " but expected " + name);
        }
    }

    /**
     * Reads the next token if and only if it is of the given kind and
     * has the given value.
     */
    private Token readMaybe(Token.Kind kind, String name) throws IOException
    {
        Token token = lexer.peekToken();
        if (token.getKind() == kind && token.getText().equals(name))
        {
            return lexer.nextToken();
        }
        return null;
    }

    /**
     * Type 1 Decryption (eexec, charstring).
     *
     * @param cipherBytes cipher text
     * @param r key
     * @param n number of random bytes (lenIV)
     * @return plain text
     */
    private byte[] decrypt(byte[] cipherBytes, int r, int n)
    {
        // lenIV of -1 means no encryption (not documented)
        if (n == -1)
        {
            return cipherBytes;
        }
        // empty charstrings and charstrings of insufficient length
        if (cipherBytes.length == 0 || n > cipherBytes.length)
        {
            return new byte[] {};
        }
        // decrypt
        int c1 = 52845;
        int c2 = 22719;
        byte[] plainBytes = new byte[cipherBytes.length - n];
        for (int i = 0; i < cipherBytes.length; i++)
        {
            int cipher = cipherBytes[i] & 0xFF;
            int plain = cipher ^ r >> 8;
            if (i >= n)
            {
                plainBytes[i - n] = (byte) plain;
            }
            r = (cipher + r) * c1 + c2 & 0xffff;
        }
        return plainBytes;
    }
}
