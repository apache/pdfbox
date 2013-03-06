/*****************************************************************************
 * 
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
 * 
 ****************************************************************************/

package org.apache.pdfbox.preflight.action.pdfa1b;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.junit.Test;

public class TestGotoAction extends AbstractTestAction
{

    @Test
    public void testGoto_OK() throws Exception
    {
        PDActionGoTo gotoAction = new PDActionGoTo();
        gotoAction.setDestination(new PDDestination()
        {
            public COSBase getCOSObject()
            {
                return COSName.getPDFName("ADest");
            }
        });

        valid(gotoAction, true);
    }

    @Test
    public void testGoto_KO_invalidContent() throws Exception
    {
        PDActionGoTo gotoAction = new PDActionGoTo();
        gotoAction.setDestination(new PDDestination()
        {
            public COSBase getCOSObject()
            {
                return new COSDictionary();
            }
        });

        valid(gotoAction, false, PreflightConstants.ERROR_ACTION_INVALID_TYPE);
    }

    @Test
    public void testGoto_KO_missingD() throws Exception
    {
        PDActionGoTo gotoAction = new PDActionGoTo();
        valid(gotoAction, false, PreflightConstants.ERROR_ACTION_MISING_KEY);
    }
}
