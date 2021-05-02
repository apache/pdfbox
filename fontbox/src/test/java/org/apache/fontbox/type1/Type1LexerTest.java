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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tilman Hausherr
 */
class Type1LexerTest
{

    Type1LexerTest()
    {
    }

    /**
     * PDFBOX-5155: test real numbers.
     */
    @Test
    void testRealNumbers() throws IOException
    {
        String s = "/FontMatrix [1e-3 0e-3 0e-3 1E-3 0 0] readonly def";
        Type1Lexer t1l = new Type1Lexer(s.getBytes(StandardCharsets.US_ASCII));
        Token nextToken;
        List<Token> tokens = new ArrayList<>();
        do
        {
            nextToken = t1l.nextToken();
            if (nextToken != null)
            {
                tokens.add(nextToken);
            }
        }
        while (nextToken != null);
        Assertions.assertEquals(Token.LITERAL, tokens.get(0).getKind());
        Assertions.assertEquals("FontMatrix", tokens.get(0).getText());
        Assertions.assertEquals(Token.START_ARRAY, tokens.get(1).getKind());
        Assertions.assertEquals(Token.REAL, tokens.get(2).getKind());
        Assertions.assertEquals(Token.REAL, tokens.get(3).getKind());
        Assertions.assertEquals(Token.REAL, tokens.get(4).getKind());
        Assertions.assertEquals(Token.REAL, tokens.get(5).getKind());
        Assertions.assertEquals(Token.INTEGER, tokens.get(6).getKind());
        Assertions.assertEquals(Token.INTEGER, tokens.get(7).getKind());
        Assertions.assertEquals("1e-3", tokens.get(2).getText());
        Assertions.assertEquals("0e-3", tokens.get(3).getText());
        Assertions.assertEquals("0e-3", tokens.get(4).getText());
        Assertions.assertEquals("1E-3", tokens.get(5).getText());
        Assertions.assertEquals("0", tokens.get(6).getText());
        Assertions.assertEquals("0", tokens.get(7).getText());
        Assertions.assertEquals(Token.END_ARRAY, tokens.get(8).getKind());
        Assertions.assertEquals(Token.NAME, tokens.get(9).getKind());
        Assertions.assertEquals(Token.NAME, tokens.get(10).getKind());
    }

}
