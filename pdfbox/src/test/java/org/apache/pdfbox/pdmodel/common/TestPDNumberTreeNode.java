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
package org.apache.pdfbox.pdmodel.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.pdfbox.cos.COSInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A test case for PDNumberTreeNode.
 * Based on TestPDNameTreeNode.
 * 
 * @author Dominic Tubach
 */
class TestPDNumberTreeNode
{

    private PDNumberTreeNode node1;
    private PDNumberTreeNode node2;
    private PDNumberTreeNode node4;
    private PDNumberTreeNode node5;
    private PDNumberTreeNode node24;
    
    public static class PDTest implements COSObjectable
    {
        private final int value;

        public PDTest(final int value)
        {
            this.value = value;
        }
        
        public PDTest(final COSInteger cosInt)
        {
            this.value = cosInt.intValue();
        }

        @Override
        public COSInteger getCOSObject()
        {
            return COSInteger.get( value );
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + value;
            return result;
        }

        @Override
        public boolean equals(final Object obj )
        {
            if ( this == obj)
            {
                return true;
            }
            if ( obj == null)
            {
                return false;
            }
            if ( getClass() != obj.getClass())
            {
                return false;
            }
            final PDTest other = (PDTest) obj;
            return value == other.value;
        }
    }

    @BeforeEach
    protected void setUp()
    {
        this.node5 = new PDNumberTreeNode(PDTest.class);
        Map<Integer,PDTest> Numbers = new TreeMap<>();
        Numbers.put( 1, new PDTest( 89 ) );
        Numbers.put( 2, new PDTest( 13 ) );
        Numbers.put( 3, new PDTest( 95 ) );
        Numbers.put( 4, new PDTest( 51 ) );
        Numbers.put( 5, new PDTest( 18 ) );
        Numbers.put( 6, new PDTest( 33 ) );
        Numbers.put( 7, new PDTest( 85 ) );
        this.node5.setNumbers( Numbers );

        this.node24 = new PDNumberTreeNode( PDTest.class );
        Numbers = new TreeMap<>();
        Numbers.put( 8, new PDTest( 54 ) );
        Numbers.put( 9, new PDTest( 70 ) );
        Numbers.put( 10, new PDTest( 39 ) );
        Numbers.put( 11, new PDTest( 30 ) );
        Numbers.put( 12, new PDTest( 40 ) );
        this.node24.setNumbers( Numbers );

        this.node2 = new PDNumberTreeNode( PDTest.class );
        List<PDNumberTreeNode> kids = this.node2.getKids();
        if ( kids == null)
        {
            kids = new COSArrayList<>();
        }
        kids.add( this.node5 );
        this.node2.setKids( kids );

        this.node4 = new PDNumberTreeNode( PDTest.class );
        kids = this.node4.getKids();
        if ( kids == null)
        {
            kids = new COSArrayList<>();
        }
        kids.add( this.node24 );
        this.node4.setKids( kids );

        this.node1 = new PDNumberTreeNode( PDTest.class );
        kids = this.node1.getKids();
        if ( kids == null)
        {
            kids = new COSArrayList<>();
        }
        kids.add( this.node2 );
        kids.add( this.node4 );
        this.node1.setKids( kids );
    }
    
    @Test
    void testGetValue() throws IOException
    {
        assertEquals(new PDTest( 51 ), this.node5.getValue( 4 ));
        assertEquals(new PDTest(70), this.node1.getValue( 9 ));
        
        this.node1.setKids( null );
        this.node1.setNumbers( null );
        assertNull( this.node1.getValue( 0 ) );
    }

    @Test
    void testUpperLimit() throws IOException
    {
        assertEquals(7, (int) this.node5.getUpperLimit());
        assertEquals(7, (int) this.node2.getUpperLimit());

        assertEquals(12, (int) this.node24.getUpperLimit());
        assertEquals(12, (int) this.node4.getUpperLimit());

        assertEquals(12, (int) this.node1.getUpperLimit());

        this.node24.setNumbers(new HashMap<>());
        assertNull( this.node24.getUpperLimit() );
        
        this.node5.setNumbers( null );
        assertNull( this.node5.getUpperLimit() );
        
        this.node1.setKids( null );
        assertNull( this.node1.getUpperLimit() );
    }

    @Test
    void testLowerLimit() throws IOException
    {
        assertEquals(1, (int) this.node5.getLowerLimit());
        assertEquals(1, (int) this.node2.getLowerLimit());

        assertEquals(8, (int) this.node24.getLowerLimit());
        assertEquals(8, (int) this.node4.getLowerLimit());

        assertEquals(1, (int) this.node1.getLowerLimit());
        
        this.node24.setNumbers(new HashMap<>());
        assertNull( this.node24.getLowerLimit() );
        
        this.node5.setNumbers( null );
        assertNull( this.node5.getLowerLimit() );
        
        this.node1.setKids( null );
        assertNull( this.node1.getLowerLimit() );
    }

}
