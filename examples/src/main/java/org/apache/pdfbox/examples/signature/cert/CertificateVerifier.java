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

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Copied from Apache CXF 2.4.9, initial version:
 * https://svn.apache.org/repos/asf/cxf/tags/cxf-2.4.9/distribution/src/main/release/samples/sts_issue_operation/src/main/java/demo/sts/provider/cert/
 * 
 */
public final class CertificateVerifier
{
    private static final Log LOG = LogFactory.getLog(CertificateVerifier.class);

    private CertificateVerifier()
    {

    }

    /**
     * Attempts to build a certification chain for given certificate and to
     * verify it. Relies on a set of root CA certificates and intermediate
     * certificates that will be used for building the certification chain. The
     * verification process assumes that all self-signed certificates in the set
     * are trusted root CA certificates and all other certificates in the set
     * are intermediate certificates.
     *
     * @param cert - certificate for validation
     * @param additionalCerts - set of trusted root CA certificates that will be
     * used as "trust anchors" and intermediate CA certificates that will be
     * used as part of the certification chain. All self-signed certificates are
     * considered to be trusted root CA certificates. All the rest are
     * considered to be intermediate CA certificates.
     * @param verifySelfSignedCert true if a self-signed certificate is accepted, false if not.
     * @param signDate the date when the signing took place
     * @return the certification chain (if verification is successful)
     * @throws CertificateVerificationException - if the certification is not
     * successful (e.g. certification path cannot be built or some certificate
     * in the chain is expired or CRL checks are failed)
     */
    public static PKIXCertPathBuilderResult verifyCertificate(
            X509Certificate cert, Set<X509Certificate> additionalCerts,
            boolean verifySelfSignedCert, Date signDate)
            throws CertificateVerificationException
    {
        try
        {
            // Check for self-signed certificate
            if (!verifySelfSignedCert && isSelfSigned(cert))
            {
                throw new CertificateVerificationException(
                        "The certificate is self-signed.");
            }

            // Prepare a set of trusted root CA certificates
            // and a set of intermediate certificates
            Set<X509Certificate> trustedRootCerts = new HashSet<X509Certificate>();
            Set<X509Certificate> intermediateCerts = new HashSet<X509Certificate>();
            for (X509Certificate additionalCert : additionalCerts)
            {
                if (isSelfSigned(additionalCert))
                {
                    trustedRootCerts.add(additionalCert);
                }
                else
                {
                    intermediateCerts.add(additionalCert);
                }
            }

            // Attempt to build the certification chain and verify it
            PKIXCertPathBuilderResult verifiedCertChain = verifyCertificate(
                    cert, trustedRootCerts, intermediateCerts, signDate);

            LOG.info("Certification chain verified successfully");

            // Check whether the certificate is revoked by the CRL
            // given in its CRL distribution point extension
            CRLVerifier.verifyCertificateCRLs(cert, signDate, additionalCerts);

            //TODO OCSP might be better, and would be faster too
            // use existing code from Alexis Suter
            // in org.apache.pdfbox.examples.signature.validation ?

            // The chain is built and verified. Return it as a result
            return verifiedCertChain;
        }
        catch (CertPathBuilderException certPathEx)
        {
            throw new CertificateVerificationException(
                    "Error building certification path: "
                    + cert.getSubjectX500Principal(), certPathEx);
        }
        catch (CertificateVerificationException cvex)
        {
            throw cvex;
        }
        catch (Exception ex)
        {
            throw new CertificateVerificationException(
                    "Error verifying the certificate: "
                    + cert.getSubjectX500Principal(), ex);
        }
    }

    /**
     * Checks whether given X.509 certificate is self-signed.
     * @param cert The X.509 certificate to check.
     * @return true if the certificate is self-signed, false if not.
     * @throws java.security.GeneralSecurityException 
     */
    public static boolean isSelfSigned(X509Certificate cert) throws GeneralSecurityException
    {
        try
        {
            // Try to verify certificate signature with its own public key
            PublicKey key = cert.getPublicKey();
            cert.verify(key);
            return true;
        }
        catch (SignatureException | InvalidKeyException sigEx)
        {
            // Invalid signature --> not self-signed
            LOG.debug("Couldn't get signature information - returning false", sigEx);
            return false;
        }
    }

    /**
     * Attempts to build a certification chain for given certificate and to
     * verify it. Relies on a set of root CA certificates (trust anchors) and a
     * set of intermediate certificates (to be used as part of the chain).
     *
     * @param cert - certificate for validation
     * @param trustedRootCerts - set of trusted root CA certificates
     * @param intermediateCerts - set of intermediate certificates
     * @param signDate the date when the signing took place
     * @return the certification chain (if verification is successful)
     * @throws GeneralSecurityException - if the verification is not successful
     * (e.g. certification path cannot be built or some certificate in the chain
     * is expired)
     */
    private static PKIXCertPathBuilderResult verifyCertificate(
            X509Certificate cert, Set<X509Certificate> trustedRootCerts,
            Set<X509Certificate> intermediateCerts, Date signDate)
            throws GeneralSecurityException
    {
        // Create the selector that specifies the starting certificate
        X509CertSelector selector = new X509CertSelector();
        selector.setCertificate(cert);

        // Create the trust anchors (set of root CA certificates)
        Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
        for (X509Certificate trustedRootCert : trustedRootCerts)
        {
            trustAnchors.add(new TrustAnchor(trustedRootCert, null));
        }

        // Configure the PKIX certificate builder algorithm parameters
        PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(trustAnchors, selector);

        // Disable CRL checks (this is done manually as additional step)
        pkixParams.setRevocationEnabled(false);

        pkixParams.setDate(signDate);

        // Specify a list of intermediate certificates
        CertStore intermediateCertStore = CertStore.getInstance("Collection",
                new CollectionCertStoreParameters(intermediateCerts));
        pkixParams.addCertStore(intermediateCertStore);

        // Build and verify the certification chain
        // If this doesn't work although it should, it can be debugged
        // by starting java with -Djava.security.debug=certpath
        // see also
        // https://docs.oracle.com/javase/8/docs/technotes/guides/security/troubleshooting-security.html
        CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
        return (PKIXCertPathBuilderResult) builder.build(pkixParams);
    }
}
