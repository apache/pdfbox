/*

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

package org.apache.pdfbox.preflight.action.pdfa1b;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.filespecification.PDFileSpecification;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionRemoteGoTo;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.junit.jupiter.api.Test;

class TestGotoRemoteAction extends AbstractTestAction
{

    @Test
    void testGoto_OK() throws Exception
    {
        final PDActionRemoteGoTo gotoAction = new PDActionRemoteGoTo();
        gotoAction.setD(COSName.getPDFName("ADest"));
        gotoAction.setFile(new PDFileSpecification()
        {
            @Override
            public COSName getCOSObject()
            {
                return COSName.getPDFName("ADest");
            }

            @Override
            public void setFile(final String file)
            {
            }

            @Override
            public String getFile()
            {
                return "pouey";
            }
        });
        valid(gotoAction, true);
    }

    @Test
    void testGoto_KO_InvalidContent() throws Exception
    {
        final PDActionRemoteGoTo gotoAction = new PDActionRemoteGoTo();
        gotoAction.setD(new COSDictionary());
        gotoAction.setFile(new PDFileSpecification()
        {
            @Override
            public COSName getCOSObject()
            {
                return COSName.getPDFName("ADest");
            }

            @Override
            public void setFile(final String file)
            {
            }

            @Override
            public String getFile()
            {
                return "pouey";
            }
        });
        valid(gotoAction, false, PreflightConstants.ERROR_ACTION_INVALID_TYPE);
    }

    @Test
    void testGoto_KO_MissingD() throws Exception
    {
        final PDActionRemoteGoTo gotoAction = new PDActionRemoteGoTo();
        gotoAction.setFile(new PDFileSpecification()
        {
            @Override
            public COSName getCOSObject()
            {
                return COSName.getPDFName("ADest");
            }

            @Override
            public void setFile(final String file)
            {
            }

            @Override
            public String getFile()
            {
                return "pouey";
            }
        });
        valid(gotoAction, false, PreflightConstants.ERROR_ACTION_MISING_KEY);
    }

    @Test
    void testGoto_KO_MissingF() throws Exception
    {
        final PDActionRemoteGoTo gotoAction = new PDActionRemoteGoTo();
        gotoAction.setD(COSName.getPDFName("ADest"));
        valid(gotoAction, false, PreflightConstants.ERROR_ACTION_MISING_KEY);
    }
}
