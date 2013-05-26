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
package org.apache.pdfbox;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.pdfbox.cos.TestCOSFloat;
import org.apache.pdfbox.cos.TestCOSInteger;
import org.apache.pdfbox.cos.TestCOSString;
import org.apache.pdfbox.filter.TestFilters;
import org.apache.pdfbox.io.TestIOUtils;
import org.apache.pdfbox.io.TestRandomAccessBuffer;
import org.apache.pdfbox.io.ccitt.TestCCITTFaxG31DDecodeInputStream;
import org.apache.pdfbox.io.ccitt.TestPackedBitArray;
import org.apache.pdfbox.pdmodel.TestFDF;
import org.apache.pdfbox.pdmodel.TestPDDocument;
import org.apache.pdfbox.pdmodel.TestPDDocumentCatalog;
import org.apache.pdfbox.pdmodel.TestPDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.TestPDNameTreeNode;
import org.apache.pdfbox.pdmodel.common.TestPDNumberTreeNode;
import org.apache.pdfbox.pdmodel.edit.TestPDPageContentStream;
import org.apache.pdfbox.pdmodel.interactive.form.TestFields;
import org.apache.pdfbox.util.TestDateUtil;
import org.apache.pdfbox.util.TestImageIOUtils;
import org.apache.pdfbox.util.TestMatrix;

/**
 * This is a holder for all test cases in the pdfbox system.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.9 $
 */
public class TestAll extends TestCase
{

    /**
     * Constructor.
     *
     * @param name The name of the test to run.
     */
    public TestAll( String name )
    {
        super( name );
    }

    /**
     * The main method to run tests.
     *
     * @param args The command line arguments.
     */
    public static void main( String[] args )
    {
        String[] arg = {TestAll.class.getName() };
        junit.textui.TestRunner.main( arg );
    }

    /**
     * This will get the suite of test that this class holds.
     *
     * @return All of the tests that this class holds.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest( TestDateUtil.suite() );
        suite.addTest( TestMatrix.suite() );
        suite.addTestSuite( TestFilters.class );
        suite.addTest( TestFDF.suite() );
        suite.addTest( TestFields.suite() );
        suite.addTest( TestCOSString.suite() );
        suite.addTest( TestCOSInteger.suite() );
        suite.addTest( TestCOSFloat.suite() );
        suite.addTestSuite( TestPDDocument.class );
        suite.addTestSuite( TestPDDocumentCatalog.class );
        suite.addTestSuite( TestPDDocumentInformation.class );
        suite.addTestSuite( org.apache.pdfbox.pdmodel.graphics.optionalcontent.TestOptionalContentGroups.class );
        suite.addTestSuite( org.apache.pdfbox.util.TestLayerUtility.class );
        suite.addTestSuite( org.apache.pdfbox.TestTextToPdf.class );
        suite.addTest( org.apache.pdfbox.pdmodel.common.function.TestFunctions.suite() );

        suite.addTestSuite( TestIOUtils.class );
        suite.addTestSuite( TestRandomAccessBuffer.class );
        suite.addTestSuite( TestImageIOUtils.class );
        suite.addTestSuite( TestPackedBitArray.class );
        suite.addTestSuite( TestCCITTFaxG31DDecodeInputStream.class );

        suite.addTestSuite( TestExtractText.class );
        
        suite.addTestSuite(TestPDPageContentStream.class);
        
        suite.addTestSuite(TestPDNameTreeNode.class);
        suite.addTestSuite(TestPDNumberTreeNode.class);

        return suite;
    }
}
