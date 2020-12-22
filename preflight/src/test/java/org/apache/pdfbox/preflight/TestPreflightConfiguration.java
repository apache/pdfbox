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
package org.apache.pdfbox.preflight;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.pdfbox.preflight.exception.MissingValidationProcessException;
import org.apache.pdfbox.preflight.exception.ValidationException;
import org.apache.pdfbox.preflight.process.BookmarkValidationProcess;
import org.apache.pdfbox.preflight.process.EmptyValidationProcess;
import org.apache.pdfbox.preflight.process.ValidationProcess;
import org.apache.pdfbox.preflight.process.reflect.ResourcesValidationProcess;
import org.junit.jupiter.api.Test;

class TestPreflightConfiguration
{
    @Test
    void testGetValidationProcess_MissingProcess() throws Exception
    {
        final PreflightConfiguration configuration = PreflightConfiguration.createPdfA1BConfiguration();
        assertThrows(MissingValidationProcessException.class, () -> {
            configuration.getInstanceOfProcess("unknownProcess");
        });
    }

    @Test
    void testGetValidationProcess_MissingProcess_NoError() throws Exception
    {
        final PreflightConfiguration configuration = PreflightConfiguration.createPdfA1BConfiguration();
        configuration.setErrorOnMissingProcess(false);
        assertDoesNotThrow(() -> {
            configuration.getInstanceOfProcess("unknownProcess");
        });
    }

    @Test
    void testReplaceValidationProcess() throws Exception
    {
        final PreflightConfiguration configuration = PreflightConfiguration.createPdfA1BConfiguration();

        final String processName = "mock-process";
        configuration.replaceProcess(processName, MockProcess.class);
        assertEquals(MockProcess.class, configuration.getInstanceOfProcess(processName).getClass());

        configuration.replaceProcess(processName, MockProcess2.class);
        assertEquals(MockProcess2.class, configuration.getInstanceOfProcess(processName).getClass());
    }
    
    @Test
    void testGetValidationProcess() throws Exception
    {
        final PreflightConfiguration confg = PreflightConfiguration.createPdfA1BConfiguration();
        final ValidationProcess vp = confg.getInstanceOfProcess(PreflightConfiguration.BOOKMARK_PROCESS);
        assertNotNull(vp);
        assertTrue(vp instanceof BookmarkValidationProcess);
    }
    
    @Test
    void testGetValidationPageProcess() throws Exception
    {
        final PreflightConfiguration confg = PreflightConfiguration.createPdfA1BConfiguration();
        final ValidationProcess vp = confg.getInstanceOfProcess(PreflightConfiguration.RESOURCES_PROCESS);
        assertNotNull(vp);
        assertTrue(vp instanceof ResourcesValidationProcess);
    }
    
    @Test
    void testGetValidationProcess_noError() throws Exception
    {
        final PreflightConfiguration confg = PreflightConfiguration.createPdfA1BConfiguration();
        confg.setErrorOnMissingProcess(false);
        confg.removeProcess(PreflightConfiguration.BOOKMARK_PROCESS);
        final ValidationProcess vp = confg.getInstanceOfProcess(PreflightConfiguration.BOOKMARK_PROCESS);
        assertNotNull(vp);
        assertTrue(vp instanceof EmptyValidationProcess);
    }
    
    @Test
    void testGetValidationPageProcess_noError() throws Exception
    {
        final PreflightConfiguration confg = PreflightConfiguration.createPdfA1BConfiguration();
        confg.setErrorOnMissingProcess(false);
        confg.removePageProcess(PreflightConfiguration.RESOURCES_PROCESS);
        final ValidationProcess vp = confg.getInstanceOfProcess(PreflightConfiguration.RESOURCES_PROCESS);
        assertNotNull(vp);
        assertTrue(vp instanceof EmptyValidationProcess);
    }

    @Test
    void testGetMissingValidationProcess() throws Exception
    {
        final PreflightConfiguration confg = PreflightConfiguration.createPdfA1BConfiguration();
        confg.removeProcess(PreflightConfiguration.BOOKMARK_PROCESS);
        assertThrows(ValidationException.class, () -> {
            confg.getInstanceOfProcess(PreflightConfiguration.BOOKMARK_PROCESS);
        });
    }

    @Test
    void testGetMissingValidationPageProcess() throws Exception
    {
        final PreflightConfiguration confg = PreflightConfiguration.createPdfA1BConfiguration();
        confg.removePageProcess(PreflightConfiguration.RESOURCES_PROCESS);
        assertThrows(ValidationException.class, () -> {
            confg.getInstanceOfProcess(PreflightConfiguration.RESOURCES_PROCESS);
        });
    }
    
    @Test
    void testGetMissingValidationProcess2() throws Exception
    {
        final PreflightConfiguration confg = PreflightConfiguration.createPdfA1BConfiguration();
        confg.replaceProcess(PreflightConfiguration.BOOKMARK_PROCESS, null);
        assertThrows(ValidationException.class, () -> {
            confg.getInstanceOfProcess(PreflightConfiguration.BOOKMARK_PROCESS);
        });
    }

    @Test
    void testGetMissingValidationPageProcess2() throws Exception
    {
        final PreflightConfiguration confg = PreflightConfiguration.createPdfA1BConfiguration();
        confg.replacePageProcess(PreflightConfiguration.RESOURCES_PROCESS, null);
        assertThrows(ValidationException.class, () -> {
            confg.getInstanceOfProcess(PreflightConfiguration.RESOURCES_PROCESS);
        });
    }

    static class MockProcess implements ValidationProcess
    {
        public void validate(final PreflightContext ctx) throws ValidationException
        {
        }
    }

    static class MockProcess2 extends MockProcess
    {
        public void validate(final PreflightContext ctx) throws ValidationException
        {
        }
    }
}
