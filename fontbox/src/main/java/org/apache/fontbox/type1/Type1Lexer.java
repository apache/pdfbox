/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.fontbox.type1;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Lexer for the ASCII portions of an Adobe Type 1 font.
 *
 * @see Type1Parser
 *
 * The PostScript language, of which Type 1 fonts are a subset, has a
 * somewhat awkward lexical structure. It is neither regular nor
 * context-free, and the execution of the program can modify the
 * the behaviour of the lexer/parser.
 *
 * Nevertheless, this class represents an attempt to artificially seperate
 * the PostScript parsing process into separate lexing and parsing phases
 * in order to reduce the complexity of the parsing phase.
 *
 * @see "PostScript Language Reference 3rd ed, Adobe Systems (1999)"
 *
 * @author John Hewson
 */
class Type1Lexer
{
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(Type1Lexer.class);
    
    private final ByteBuffer buffer;
    private Token aheadToken;
    private int openParens = 0;

    /**
     * Constructs a new Type1Lexer given a header-less .pfb segment.
     * @param bytes Header-less .pfb segment
     * @throws IOException
     */
    Type1Lexer(byte[] bytes) throws IOException
    {
        buffer = ByteBuffer.wrap(bytes);
        aheadToken = readToken(null);
    }

    /**
     * Returns the next token and consumes it.
     * @return The next token.
     */
    public Token nextToken() throws IOException
    {
        Token curToken = aheadToken;
        //System.out.println(curToken); // for debugging
        aheadToken = readToken(curToken);
        return curToken;
    }

    /**
     * Returns the next token without consuming it.
     * @return The next token
     */
    public Token peekToken()
    {
        return aheadToken;
    }

    /**
     * Reads an ASCII char from the buffer.
     */
    private char getChar()
    {
        return (char) buffer.get();
    }

    /**
     * Reads a single token.
     * @param prevToken the previous token
     */
    private Token readToken(Token prevToken) throws IOException
    {
        boolean skip;
        do
        {
            skip = false;
            while (buffer.hasRemaining())
            {
                char c = getChar();

                // delimiters
                if (c == '%')
                {
                    // comment
                    readComment();
                }
                else if (c == '(')
                {
                    return readString();
                }
                else if (c == ')')
                {
                    // not allowed outside a string context
                    throw new IOException("unexpected closing parenthesis");
                }
                else if (c == '[')
                {
                    return new Token(c, Token.START_ARRAY);
                }
                else if (c == '{')
                {
                    return new Token(c, Token.START_PROC);
                }
                else if (c == ']')
                {
                    return new Token(c, Token.END_ARRAY);
                }
                else if (c == '}')
                {
                    return new Token(c, Token.END_PROC);
                }
                else if (c == '/')
                {
                    return new Token(readRegular(), Token.LITERAL);
                }
                else if (Character.isWhitespace(c))
                {
                    skip = true;
                }
                else if (c == 0)
                {
                    LOG.warn("NULL byte in font, skipped");
                    skip = true;
                }
                else
                {
                    buffer.position(buffer.position() -1);

                    // regular character: try parse as number
                    Token number = tryReadNumber();
                    if (number != null)
                    {
                        return number;
                    }
                    else
                    {
                        // otherwise this must be a name
                        String name = readRegular();
                        if (name == null)
                        {
                            // the stream is corrupt
                            throw new DamagedFontException("Could not read token at position " +
                                                           buffer.position());
                        }

                        if (name.equals("RD") || name.equals("-|"))
                        {
                            // return the next CharString instead
                            if (prevToken.getKind() == Token.INTEGER)
                            {
                                return readCharString(prevToken.intValue());
                            }
                            else
                            {
                                throw new IOException("expected INTEGER before -| or RD");
                            }
                        }
                        else
                        {
                            return new Token(name, Token.NAME);
                        }
                    }
                }
            }
        } while (skip);
        return null;
    }

    /**
     * Reads a number or returns null.
     */
    private Token tryReadNumber()
    {
        buffer.mark();

        StringBuilder sb = new StringBuilder();
        StringBuilder radix = null;
        char c = getChar();
        boolean hasDigit = false;

        // optional + or -
        if (c == '+' || c == '-')
        {
            sb.append(c);
            c = getChar();
        }

        // optional digits
        while (Character.isDigit(c))
        {
            sb.append(c);
            c = getChar();
            hasDigit = true;
        }

        // optional .
        if (c == '.')
        {
            sb.append(c);
            c = getChar();
        }
        else if (c == '#')
        {
            // PostScript radix number takes the form base#number
            radix = sb;
            sb = new StringBuilder();
            c = getChar();
        }
        else if (sb.length() == 0 || !hasDigit)
        {
            // failure
            buffer.reset();
            return null;
        }
        else
        {
            // integer
            buffer.position(buffer.position() -1);
            return new Token(sb.toString(), Token.INTEGER);
        }

        // required digit
        if (Character.isDigit(c))
        {
            sb.append(c);
            c = getChar();
        }
        else
        {
            // failure
            buffer.reset();
            return null;
        }

        // optional digits
        while (Character.isDigit(c))
        {
            sb.append(c);
            c = getChar();
        }

        // optional E
        if (c == 'E')
        {
            sb.append(c);
            c = getChar();
            
            // optional minus
            if (c == '-')
            {
                sb.append(c);
                c = getChar();
            }

            // required digit
            if (Character.isDigit(c))
            {
                sb.append(c);
                c = getChar();
            }
            else
            {
                // failure
                buffer.reset();
                return null;
            }

            // optional digits
            while (Character.isDigit(c))
            {
                sb.append(c);
                c = getChar();
            }
        }
        
        buffer.position(buffer.position() - 1);
        if (radix != null)
        {
            Integer val = Integer.parseInt(sb.toString(), Integer.parseInt(radix.toString()));
            return new Token(val.toString(), Token.INTEGER);
        }
        return new Token(sb.toString(), Token.REAL);
    }

    /**
     * Reads a sequence of regular characters, i.e. not delimiters
     * or whitespace
     */
    private String readRegular()
    {
        StringBuilder sb = new StringBuilder();
        while (buffer.hasRemaining())
        {
            buffer.mark();
            char c = getChar();
            if (Character.isWhitespace(c) ||
                c == '(' || c == ')' ||
                c == '<' || c == '>' ||
                c == '[' || c == ']' ||
                c == '{' || c == '}' ||
                c == '/' || c == '%' )
            {
                buffer.reset();
                break;
            }
            else
            {
                sb.append(c);
            }
        }
        String regular = sb.toString();
        if (regular.length() == 0)
        {
            return null;
        }
        return regular;
    }

    /**
     * Reads a line comment.
     */
    private String readComment()
    {
        StringBuilder sb = new StringBuilder();
        while (buffer.hasRemaining())
        {
            char c = getChar();
            if (c == '\r' || c == '\n')
            {
                break;
            }
            else
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Reads a (string).
     */
    private Token readString()
    {
        StringBuilder sb = new StringBuilder();

        while (buffer.hasRemaining())
        {
            char c = getChar();

            // string context
            if (c == '(')
            {
                openParens++;
                sb.append('(');
            }
            else if (c == ')')
            {
                if (openParens == 0)
                {
                    // end of string
                    return new Token(sb.toString(), Token.STRING);
                }
                else
                {
                    sb.append(')');
                    openParens--;
                }
            }
            else if (c == '\\')
            {
                // escapes: \n \r \t \b \f \\ \( \)
                char c1 = getChar();
                switch (c1)
                {
                    case 'n':
                    case 'r': sb.append("\n"); break;
                    case 't': sb.append('\t'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case '\\': sb.append('\\'); break;
                    case '(': sb.append('('); break;
                    case ')': sb.append(')'); break;
                }
                // octal \ddd
                if (Character.isDigit(c1))
                {
                    String num = String.valueOf(new char[] { c1, getChar(), getChar() });
                    Integer code = Integer.parseInt(num, 8);
                    sb.append((char)(int)code);
                }
            }
            else if (c == '\r' || c == '\n')
            {
                sb.append("\n");
            }
            else
            {
                sb.append(c);
            }
        }
        return null;
    }

    /**
     * Reads a binary CharString.
     */
    private Token readCharString(int length)
    {
        buffer.get(); // space
        byte[] data = new byte[length];
        buffer.get(data);
        return new Token(data, Token.CHARSTRING);
    }
}
