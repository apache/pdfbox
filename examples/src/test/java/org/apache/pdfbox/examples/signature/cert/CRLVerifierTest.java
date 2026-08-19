/*
 * Copyright 2026 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.examples.signature.cert;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import javax.naming.NamingException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.pdfbox.examples.signature.SigUtils;

/**
 * @author Tilman Hausherr
 */
class CRLVerifierTest
{
    /**
     * Test downloadCRLFromLDAP(). Get a CRL through an LDAP URI and verify it with the root certificate.
     * 
     * @throws IOException
     * @throws CertificateVerificationException
     * @throws NamingException
     * @throws URISyntaxException
     * @throws GeneralSecurityException 
     */
    @Test
    void testLDAP() throws IOException, CertificateVerificationException, NamingException, URISyntaxException, GeneralSecurityException
    {
        // ChatGPT prompt if the LDAP URI no longer works:
        // Find me a certificate that has a CRL that must be downloaded through LDAP.
        // This gets
        // https://www.si-trust.gov.si/assets/Politike/si-pass-ca/verzija-2-7/SI-PASS-CA-politika-v2.7-2025.pdf
        // which include the LDAP URL and the root certificate.
        // Note that "%20" is needed for the spaces.
        // ChatGPT wasn't able to find a certificate where the CRL Distribution Points extension contains an LDAP URI and no HTTP/HTTPS URI.
        X509CRL crl = CRLVerifier.downloadCRL("ldap://x500.gov.si/cn=SI-TRUST%20Root,oi=VATSI-17659957,o=Republika%20Slovenija,c=SI?certificateRevocationList");
        try (InputStream is = SigUtils.openURL("http://www.ca.gov.si/crt/si-trust-root.crt"))
        {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) factory.generateCertificate(is);
            assertTrue(CertificateVerifier.isSelfSigned(cert));
            assertDoesNotThrow(() -> crl.verify(cert.getPublicKey()));
        }
    }
    
}
