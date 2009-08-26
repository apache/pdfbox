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
package org.apache.pdfbox.cos;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This will test all of the filters in the PDFBox system.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision$
 */
public class TestCOSString extends TestCase
{

    /**
     * Constructor.
     *
     * @param name The name of the test to run.
     */
    public TestCOSString( String name )
    {
        super( name );
    }

    /**
     * This will get the suite of test that this class holds.
     *
     * @return All of the tests that this class holds.
     */
    public static Test suite()
    {
        return new TestSuite( TestCOSString.class );
    }

    /**
     * infamous main method.
     *
     * @param args The command line arguments.
     */
    public static void main( String[] args )
    {
        String[] arg = {TestCOSString.class.getName() };
        junit.textui.TestRunner.main( arg );
    }

    /**
     * This will test all of the filters in the system.
     *
     * @throws IOException If there is an exception while encoding.
     */
    public void testUnicode() throws IOException
    {
        String theString = "\u4e16";
        COSString string = new COSString( theString );
        assertTrue( string.getString().equals( theString ) );
    }
}
