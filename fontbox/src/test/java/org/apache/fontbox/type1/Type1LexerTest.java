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
import java.util.ArrayList;
import java.util.List;

import org.apache.fontbox.util.Charsets;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Tilman Hausherr
 */
public class Type1LexerTest
{

    public Type1LexerTest()
    {
    }

    /**
     * PDFBOX-5155: test real numbers.
     */
    @Test
    public void testRealNumbers() throws IOException
    {
        String s = "/FontMatrix [1e-3 0e-3 0e-3 1E-3 0 0] readonly def";
        Type1Lexer t1l = new Type1Lexer(s.getBytes(Charsets.US_ASCII));
        Token nextToken;
        List<Token> tokens = new ArrayList<Token>();
        do
        {
            nextToken = t1l.nextToken();
            if (nextToken != null)
            {
                tokens.add(nextToken);
            }
        }
        while (nextToken != null);
        Assert.assertEquals(Token.LITERAL, tokens.get(0).getKind());
        Assert.assertEquals("FontMatrix", tokens.get(0).getText());
        Assert.assertEquals(Token.START_ARRAY, tokens.get(1).getKind());
        Assert.assertEquals(Token.REAL, tokens.get(2).getKind());
        Assert.assertEquals(Token.REAL, tokens.get(3).getKind());
        Assert.assertEquals(Token.REAL, tokens.get(4).getKind());
        Assert.assertEquals(Token.REAL, tokens.get(5).getKind());
        Assert.assertEquals(Token.INTEGER, tokens.get(6).getKind());
        Assert.assertEquals(Token.INTEGER, tokens.get(7).getKind());
        Assert.assertEquals("1e-3", tokens.get(2).getText());
        Assert.assertEquals("0e-3", tokens.get(3).getText());
        Assert.assertEquals("0e-3", tokens.get(4).getText());
        Assert.assertEquals("1E-3", tokens.get(5).getText());
        Assert.assertEquals("0", tokens.get(6).getText());
        Assert.assertEquals("0", tokens.get(7).getText());
        Assert.assertEquals(Token.END_ARRAY, tokens.get(8).getKind());
        Assert.assertEquals(Token.NAME, tokens.get(9).getKind());
        Assert.assertEquals(Token.NAME, tokens.get(10).getKind());
    }

}
