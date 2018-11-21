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

import java.io.IOException;
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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.encryption.SecurityProvider;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPResp;

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

            checkRevocations(cert, additionalCerts, signDate);

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

    private static void checkRevocations(X509Certificate cert,
                                         Set<X509Certificate> additionalCerts,
                                         Date signDate)
            throws IOException, CertificateVerificationException, OCSPException,
                   RevokedCertificateException, GeneralSecurityException
    {
        if (isSelfSigned(cert))
        {
            // root, we're done
            return;
        }
        X509Certificate issuerCert = null;
        for (X509Certificate additionalCert : additionalCerts)
        {
            if (cert.getIssuerX500Principal().equals(additionalCert.getSubjectX500Principal()))
            {
                issuerCert = additionalCert;
                break;
            }
        }
        // issuerCert is never null here. If it hadn't been found, then there wouldn't be a 
        // verifiedCertChain earlier.

        // Try checking the certificate through OCSP (faster than CRL)
        String ocspURL = extractOCSPURL(cert);
        if (ocspURL != null)
        {
            OcspHelper ocspHelper = new OcspHelper(cert, issuerCert, ocspURL);
            verifyOCSP(ocspHelper, signDate);
        }
        else
        {
            LOG.info("OCSP not available, will try CRL");

            // Check whether the certificate is revoked by the CRL
            // given in its CRL distribution point extension
            CRLVerifier.verifyCertificateCRLs(cert, signDate, additionalCerts);
        }

        // now check the issuer
        checkRevocations(issuerCert, additionalCerts, signDate);
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
            cert.verify(key, SecurityProvider.getProvider().getName());
            return true;
        }
        catch (SignatureException ex)
        {
            // Invalid signature --> not self-signed
            LOG.debug("Couldn't get signature information - returning false", ex);
            return false;
        }
        catch (InvalidKeyException ex)
        {
            // Invalid signature --> not self-signed
            LOG.debug("Couldn't get signature information - returning false", ex);
            return false;
        }
        catch (IOException ex)
        {
            // Invalid signature --> not self-signed
            LOG.debug("Couldn't get signature information - returning false", ex);
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

    /**
     * Extract the OCSP URL from an X.509 certificate if available.
     *
     * @param cert X.509 certificate
     * @return the URL of the OCSP validation service
     * @throws IOException 
     */
    private static String extractOCSPURL(X509Certificate cert) throws IOException
    {
        byte[] authorityExtensionValue = cert.getExtensionValue(Extension.authorityInfoAccess.getId());
        if (authorityExtensionValue != null)
        {
            // copied from CertInformationHelper.getAuthorityInfoExtensionValue()
            // DRY refactor should be done some day
            ASN1Sequence asn1Seq = (ASN1Sequence) JcaX509ExtensionUtils.parseExtensionValue(authorityExtensionValue);
            Enumeration<?> objects = asn1Seq.getObjects();
            while (objects.hasMoreElements())
            {
                // AccessDescription
                ASN1Sequence obj = (ASN1Sequence) objects.nextElement();
                ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) obj.getObjectAt(0);
                // accessLocation
                DERTaggedObject location = (DERTaggedObject) obj.getObjectAt(1);
                if (oid.equals(X509ObjectIdentifiers.id_ad_ocsp)
                        && location.getTagNo() == GeneralName.uniformResourceIdentifier)
                {
                    DEROctetString url = (DEROctetString) location.getObject();
                    String ocspURL = new String(url.getOctets());
                    LOG.info("OCSP URL: " + ocspURL);
                    return ocspURL;
                }
            }
        }
        return null;
    }

    /**
     * Verify whether the certificate has been revoked at signing date.
     *
     * @param ocspHelper the OCSP helper.
     * @param signDate the signing date.
     * @throws RevokedCertificateException
     * @throws IOException
     * @throws OCSPException
     * @throws CertificateVerificationException
     */
    private static void verifyOCSP(OcspHelper ocspHelper, Date signDate)
            throws RevokedCertificateException, IOException, OCSPException, CertificateVerificationException
    {
        try
        {
            OCSPResp basicResponse = ocspHelper.getResponseOcsp();
            if (basicResponse.getStatus() != OCSPResp.SUCCESSFUL)
            {
                throw new CertificateVerificationException("OCSP check not successful, status: "
                        + basicResponse.getStatus());
            }
            else
            {
                LOG.info("OCSP check successful");
            }
        }
        catch (RevokedCertificateException ex)
        {
            if (ex.getRevocationTime().compareTo(signDate) <= 0)
            {
                throw ex;
            }
            LOG.info("OCSP check successful: The certificate was revoked after signing on " +
                    ex.getRevocationTime());
        }
    }
}
