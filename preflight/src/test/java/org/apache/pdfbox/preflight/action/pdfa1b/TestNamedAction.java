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

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.preflight.PreflightConstants;
import org.junit.jupiter.api.Test;

class TestNamedAction extends AbstractTestAction
{
    protected COSDictionary createNamedAction()
    {
        final COSDictionary namedAction = new COSDictionary();
        namedAction.setItem(COSName.TYPE, COSName.getPDFName("Action"));
        namedAction.setItem(COSName.S, COSName.getPDFName("Named"));

        return namedAction;
    }

    @Test
    void testFirstPage() throws Exception
    {
        final COSDictionary namedAction = createNamedAction();
        namedAction
                .setItem(COSName.N, COSName.getPDFName(PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_NAMED_FIRST));
        valid(namedAction, true);
    }

    @Test
    void testLastPage() throws Exception
    {
        final COSDictionary namedAction = createNamedAction();
        namedAction.setItem(COSName.N, COSName.getPDFName(PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_NAMED_LAST));
        valid(namedAction, true);
    }

    @Test
    void testNextPage() throws Exception
    {
        final COSDictionary namedAction = createNamedAction();
        namedAction.setItem(COSName.N, COSName.getPDFName(PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_NAMED_NEXT));
        valid(namedAction, true);
    }

    @Test
    void testPrevPage() throws Exception
    {
        final COSDictionary namedAction = createNamedAction();
        namedAction.setItem(COSName.N, COSName.getPDFName(PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_NAMED_PREV));
        valid(namedAction, true);
    }

    @Test
    void testMissingN() throws Exception
    {
        final COSDictionary namedAction = createNamedAction();
        valid(namedAction, false, PreflightConstants.ERROR_ACTION_MISING_KEY);
    }

    @Test
    void testForbiddenN() throws Exception
    {
        final COSDictionary namedAction = createNamedAction();
        namedAction.setItem(COSName.N, COSName.getPDFName("unknown"));
        valid(namedAction, false, PreflightConstants.ERROR_ACTION_FORBIDDEN_ACTIONS_NAMED);
    }
}
