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
        String s = "/FontMatrix [1e-3 0e-3 0e-3 -1E-03 0 0 1.23 -1.23 ] readonly def";
        Type1Lexer t1l = new Type1Lexer(s.getBytes(Charsets.US_ASCII));
        List<Token> tokens = readTokens(t1l);
        Assert.assertEquals(Token.LITERAL, tokens.get(0).getKind());
        Assert.assertEquals("FontMatrix", tokens.get(0).getText());
        Assert.assertEquals(Token.START_ARRAY, tokens.get(1).getKind());
        Assert.assertEquals(Token.REAL, tokens.get(2).getKind());
        Assert.assertEquals(Token.REAL, tokens.get(3).getKind());
        Assert.assertEquals(Token.REAL, tokens.get(4).getKind());
        Assert.assertEquals(Token.REAL, tokens.get(5).getKind());
        Assert.assertEquals(Token.INTEGER, tokens.get(6).getKind());
        Assert.assertEquals(Token.INTEGER, tokens.get(7).getKind());
        Assert.assertEquals(Token.REAL, tokens.get(8).getKind());
        Assert.assertEquals(Token.REAL, tokens.get(9).getKind());
        Assert.assertEquals("1e-3", tokens.get(2).getText());
        Assert.assertEquals("0e-3", tokens.get(3).getText());
        Assert.assertEquals("0e-3", tokens.get(4).getText());
        Assert.assertEquals("-1E-03", tokens.get(5).getText());
        Assert.assertEquals(-1E-3f, tokens.get(5).floatValue(), 0);
        Assert.assertEquals("0", tokens.get(6).getText());
        Assert.assertEquals("0", tokens.get(7).getText());
        Assert.assertEquals("1.23", tokens.get(8).getText());
        Assert.assertEquals("-1.23", tokens.get(9).getText());
        Assert.assertEquals(Token.END_ARRAY, tokens.get(10).getKind());
        Assert.assertEquals(Token.NAME, tokens.get(11).getKind());
        Assert.assertEquals(Token.NAME, tokens.get(12).getKind());
    }

    @Test
    public void testEmptyName() throws IOException
    {
        String s = "dup 127 / put";
        Type1Lexer t1l = new Type1Lexer(s.getBytes(Charsets.US_ASCII));
        Token nextToken;
        try
        {
            do
            {
                nextToken = t1l.nextToken();
            }
            while (nextToken != null);
            Assert.fail("DamagedFontException expected");
        }
        catch (DamagedFontException ex)
        {
            Assert.assertEquals("Could not read token at position 9", ex.getMessage());
        }
    }

    @Test
    public void testProcAndNameAndDictAndString() throws IOException
    {
        String s = "/ND {noaccess def} executeonly def \n 8#173 +2#110 \n%comment \n<< (string \\n \\r \\t \\b \\f \\\\ \\( \\) \\123) >>";
        Type1Lexer t1l = new Type1Lexer(s.getBytes(Charsets.US_ASCII));
        List<Token> tokens = readTokens(t1l);
        Assert.assertEquals(Token.LITERAL, tokens.get(0).getKind());
        Assert.assertEquals("ND", tokens.get(0).getText());
        Assert.assertEquals(Token.START_PROC, tokens.get(1).getKind());
        Assert.assertEquals(Token.NAME, tokens.get(2).getKind());
        Assert.assertEquals("noaccess", tokens.get(2).getText());
        Assert.assertEquals(Token.NAME, tokens.get(3).getKind());
        Assert.assertEquals("def", tokens.get(3).getText());
        Assert.assertEquals(Token.END_PROC, tokens.get(4).getKind());
        Assert.assertEquals(Token.NAME, tokens.get(5).getKind());
        Assert.assertEquals("executeonly", tokens.get(5).getText());
        Assert.assertEquals(Token.NAME, tokens.get(6).getKind());
        Assert.assertEquals("def", tokens.get(6).getText());        
        Assert.assertEquals(Token.INTEGER, tokens.get(7).getKind());
        Assert.assertEquals("123", tokens.get(7).getText());
        Assert.assertEquals(Token.INTEGER, tokens.get(8).getKind());
        Assert.assertEquals("6", tokens.get(8).getText());
        Assert.assertEquals(Token.START_DICT, tokens.get(9).getKind());
        Assert.assertEquals(Token.STRING, tokens.get(10).getKind());
        Assert.assertEquals("string \n \n \t \b \f \\ ( ) \123", tokens.get(10).getText());        
        Assert.assertEquals(Token.END_DICT, tokens.get(11).getKind());
    }
    
    @Test
    public void TestData() throws IOException
    {
        String s = "3 RD 123 ND";
        Type1Lexer t1l = new Type1Lexer(s.getBytes(Charsets.US_ASCII));
        List<Token> tokens = readTokens(t1l);
        Assert.assertEquals(Token.INTEGER, tokens.get(0).getKind());
        Assert.assertEquals(3, tokens.get(0).intValue());
        Assert.assertEquals(Token.CHARSTRING, tokens.get(1).getKind());
        Assert.assertArrayEquals(new byte[] {'1', '2', '3'}, tokens.get(1).getData());
        Assert.assertEquals(Token.NAME, tokens.get(2).getKind());
        Assert.assertEquals("ND", tokens.get(2).getText());
    }

    private List<Token> readTokens(Type1Lexer t1l) throws IOException
    {
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
        return tokens;
    }
}
