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

package org.apache.pdfbox.preflight.parser;

import static org.junit.Assert.assertEquals;

import org.apache.pdfbox.preflight.PreflightConfiguration;
import org.apache.pdfbox.preflight.PreflightContext;
import org.apache.pdfbox.preflight.exception.MissingValidationProcessException;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.process.ValidationProcess;
import org.junit.Test;

public class TestPreflightConfiguration
{

    @Test(expected = MissingValidationProcessException.class)
    public void testGetValidationProcess_MissingProcess() throws Exception
    {
        PreflightConfiguration configuration = PreflightConfiguration.createPdfA1BConfiguration();
        configuration.getInstanceOfProcess("unknownProcess");
    }

    @Test
    public void testGetValidationProcess_MissingProcess_NoError() throws Exception
    {
        PreflightConfiguration configuration = PreflightConfiguration.createPdfA1BConfiguration();
        configuration.setErrorOnMissingProcess(false);
        configuration.getInstanceOfProcess("unknownProcess");
    }

    @Test
    public void testReplaceValidationProcess() throws Exception
    {
        PreflightConfiguration configuration = PreflightConfiguration.createPdfA1BConfiguration();

        String processName = "mock-process";
        configuration.replaceProcess(processName, MockProcess.class);
        assertEquals(MockProcess.class, configuration.getInstanceOfProcess(processName).getClass());

        configuration.replaceProcess(processName, MockProcess2.class);
        assertEquals(MockProcess2.class, configuration.getInstanceOfProcess(processName).getClass());
    }

    public static class MockProcess implements ValidationProcess
    {
        public void validate(PreflightContext ctx) throws ValidationException
        {
        }
    }

    public static class MockProcess2 extends MockProcess
    {
        public void validate(PreflightContext ctx) throws ValidationException
        {
        }
    }
}
