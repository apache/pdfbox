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
package org.apache.jempbox.xmp;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Suite for all tests in test.jempbox.xmp.
 * 
 * @author $Author: coezbek $
 * @version $Revision: 1.1 $ ($Date: 2006/12/30 17:27:46 $)
 * 
 */
public class AllTests
{

    /**
     * Hide constructor.
     */
    protected AllTests()
    {
    }

    /**
     * Method returns a test representing all tests in the package
     * test.jempbox.xmp.
     * 
     * @return The test representing all tests in the current package.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test for test.jempbox.xmp");
        // $JUnit-BEGIN$
        suite.addTestSuite(XMPSchemaTest.class);
        // $JUnit-END$
        return suite;
    }

}
