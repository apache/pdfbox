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
package org.apache.pdfbox.pdmodel.common.function.type4;

/**
 * Parser for PDF Type 4 functions. This implements a small subset of the PostScript
 * language but is no full PostScript interpreter.
 *
 * @version $Revision$
 */
public class Parser
{

    /** Used to indicate the parsers current state. */
    private static enum State
    {
        NEWLINE, WHITESPACE, COMMENT, TOKEN
    }

    private Parser()
    {
        //nop
    }

    /**
     * Parses a Type 4 function and sends the syntactic elements to the given
     * syntax handler.
     * @param input the text source
     * @param handler the syntax handler
     */
    public static void parse(CharSequence input, SyntaxHandler handler)
    {
        Tokenizer tokenizer = new Tokenizer(input, handler);
        tokenizer.tokenize();
    }

    /**
     * This interface defines all possible syntactic elements of a Type 4 function.
     * It is called by the parser as the function is interpreted.
     */
    public interface SyntaxHandler
    {

        /**
         * Indicates that a new line starts.
         * @param text the new line character (CR, LF, CR/LF or FF)
         */
        void newLine(CharSequence text);

        /**
         * Called when whitespace characters are encountered.
         * @param text the whitespace text
         */
        void whitespace(CharSequence text);

        /**
         * Called when a token is encountered. No distinction between operators and values
         * is done here.
         * @param text the token text
         */
        void token(CharSequence text);

        /**
         * Called for a comment.
         * @param text the comment
         */
        void comment(CharSequence text);
    }

    /**
     * Abstract base class for a {@link SyntaxHandler}.
     */
    public abstract static class AbstractSyntaxHandler implements SyntaxHandler
    {

        /** {@inheritDoc} */
        public void comment(CharSequence text)
        {
            //nop
        }

        /** {@inheritDoc} */
        public void newLine(CharSequence text)
        {
            //nop
        }

        /** {@inheritDoc} */
        public void whitespace(CharSequence text)
        {
            //nop
        }

    }

    /**
     * Tokenizer for Type 4 functions.
     */
    private static class Tokenizer
    {

        private static final char NUL = '\u0000'; //NUL
        private static final char EOT = '\u0004'; //END OF TRANSMISSION
        private static final char TAB = '\u0009'; //TAB CHARACTER
        private static final char FF = '\u000C'; //FORM FEED
        private static final char CR = '\r'; //CARRIAGE RETURN
        private static final char LF = '\n'; //LINE FEED
        private static final char SPACE = '\u0020'; //SPACE

        private CharSequence input;
        private int index;
        private SyntaxHandler handler;
        private State state = State.WHITESPACE;
        private StringBuilder buffer = new StringBuilder();

        private Tokenizer(CharSequence text, SyntaxHandler syntaxHandler)
        {
            this.input = text;
            this.handler = syntaxHandler;
        }

        private boolean hasMore()
        {
            return index < input.length();
        }

        private char currentChar()
        {
            return input.charAt(index);
        }

        private char nextChar()
        {
            index++;
            if (!hasMore())
            {
                return EOT;
            }
            else
            {
                return currentChar();
            }
        }

        private char peek()
        {
            if (index < input.length() - 1)
            {
                return input.charAt(index + 1);
            }
            else
            {
                return EOT;
            }
        }

        private State nextState()
        {
            char ch = currentChar();
            switch (ch)
            {
            case CR:
            case LF:
            case FF: //FF
                state = State.NEWLINE;
                break;
            case NUL:
            case TAB:
            case SPACE:
                state = State.WHITESPACE;
                break;
            case '%':
                state = State.COMMENT;
                break;
            default:
                state = State.TOKEN;
            }
            return state;
        }

        private void tokenize()
        {
            while (hasMore())
            {
                buffer.setLength(0);
                nextState();
                switch (state)
                {
                case NEWLINE:
                    scanNewLine();
                    break;
                case WHITESPACE:
                    scanWhitespace();
                    break;
                case COMMENT:
                    scanComment();
                    break;
                default:
                    scanToken();
                }
            }
        }

        private void scanNewLine()
        {
            assert state == State.NEWLINE;
            char ch = currentChar();
            buffer.append(ch);
            if (ch == CR)
            {
                if (peek() == LF)
                {
                    //CRLF is treated as one newline
                    buffer.append(nextChar());
                }
            }
            handler.newLine(buffer);
            nextChar();
        }

        private void scanWhitespace()
        {
            assert state == State.WHITESPACE;
            buffer.append(currentChar());
            loop:
            while (hasMore())
            {
                char ch = nextChar();
                switch (ch)
                {
                case NUL:
                case TAB:
                case SPACE:
                    buffer.append(ch);
                    break;
                default:
                    break loop;
                }
            }
            handler.whitespace(buffer);
        }

        private void scanComment()
        {
            assert state == State.COMMENT;
            buffer.append(currentChar());
            loop:
            while (hasMore())
            {
                char ch = nextChar();
                switch (ch)
                {
                case CR:
                case LF:
                case FF:
                    break loop;
                default:
                    buffer.append(ch);
                }
            }
            //EOF reached
            handler.comment(buffer);
        }

        private void scanToken()
        {
            assert state == State.TOKEN;
            char ch = currentChar();
            buffer.append(ch);
            switch (ch)
            {
            case '{':
            case '}':
                handler.token(buffer);
                nextChar();
                return;
            default:
                //continue
            }
            loop:
            while (hasMore())
            {
                ch = nextChar();
                switch (ch)
                {
                case NUL:
                case TAB:
                case SPACE:
                case CR:
                case LF:
                case FF:
                case EOT:
                case '{':
                case '}':
                    break loop;
                default:
                    buffer.append(ch);
                }
            }
            //EOF reached
            handler.token(buffer);
        }

    }

}
