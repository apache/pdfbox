/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pdfbox.examples.signature.cert;

import java.security.cert.PKIXCertPathBuilderResult;

/**
 * Copied from Apache CXF 2.4.9, initial version:
 * https://svn.apache.org/repos/asf/cxf/tags/cxf-2.4.9/distribution/src/main/release/samples/sts_issue_operation/src/main/java/demo/sts/provider/cert/
 * 
 */
public class CertificateVerificationResult
{
    private boolean valid;
    private PKIXCertPathBuilderResult result;
    private Throwable exception;

    /**
     * Constructs a certificate verification result for valid certificate by
     * given certification path.
     */
    public CertificateVerificationResult(PKIXCertPathBuilderResult result)
    {
        this.valid = true;
        this.result = result;
    }

    public CertificateVerificationResult(Throwable exception)
    {
        this.valid = false;
        this.exception = exception;
    }

    public boolean isValid()
    {
        return valid;
    }

    public PKIXCertPathBuilderResult getResult()
    {
        return result;
    }

    public Throwable getException()
    {
        return exception;
    }
}
