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
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.cert.X509Extension;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.encryption.SecurityProvider;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
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
                throw new CertificateVerificationException("The certificate is self-signed.");
            }

            Set<X509Certificate> certSet = CertificateVerifier.downloadExtraCertificates(cert);
            int downloadSize = certSet.size();
            certSet.addAll(additionalCerts);
            if (downloadSize > 0)
            {
                LOG.info("CA issuers: " + (certSet.size() - additionalCerts.size()) + " downloaded certificate(s) are new");
            }

            // Prepare a set of trust anchors (set of root CA certificates)
            // and a set of intermediate certificates
            Set<X509Certificate> intermediateCerts = new HashSet<X509Certificate>();
            Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
            for (X509Certificate additionalCert : certSet)
            {
                if (isSelfSigned(additionalCert))
                {
                    trustAnchors.add(new TrustAnchor(additionalCert, null));
                }
                else
                {
                    intermediateCerts.add(additionalCert);
                }
            }

            if (trustAnchors.isEmpty())
            {
                throw new CertificateVerificationException("No root certificate in the chain");
            }

            // Attempt to build the certification chain and verify it
            PKIXCertPathBuilderResult verifiedCertChain = verifyCertificate(
                    cert, trustAnchors, intermediateCerts, signDate);

            LOG.info("Certification chain verified successfully");

            checkRevocations(cert, certSet, signDate);

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
            OcspHelper ocspHelper = new OcspHelper(cert, signDate, issuerCert, additionalCerts, ocspURL);
            try
            {
                verifyOCSP(ocspHelper, additionalCerts);
            }
            catch (IOException ex)
            {
                // happens with 021496.pdf because OCSP responder no longer exists
                LOG.warn("IOException trying OCSP, will try CRL", ex);
                CRLVerifier.verifyCertificateCRLs(cert, signDate, additionalCerts);
            }
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
     * Download extra certificates from the URI mentioned in id-ad-caIssuers in the "authority
     * information access" extension. The method is lenient, i.e. catches all exceptions.
     *
     * @param ext an X509 object that can have extensions.
     *
     * @return a certificate set, never null.
     */
    public static Set<X509Certificate> downloadExtraCertificates(X509Extension ext)
    {
        // https://tools.ietf.org/html/rfc2459#section-4.2.2.1
        // https://tools.ietf.org/html/rfc3280#section-4.2.2.1
        // https://tools.ietf.org/html/rfc4325
        Set<X509Certificate> resultSet = new HashSet<X509Certificate>();
        byte[] authorityExtensionValue = ext.getExtensionValue(Extension.authorityInfoAccess.getId());
        if (authorityExtensionValue == null)
        {
            return resultSet;
        }
        ASN1Primitive asn1Prim;
        try
        {
            asn1Prim = JcaX509ExtensionUtils.parseExtensionValue(authorityExtensionValue);
        }
        catch (IOException ex)
        {
            LOG.warn(ex.getMessage(), ex);
            return resultSet;
        }
        if (!(asn1Prim instanceof ASN1Sequence))
        {
            LOG.warn("ASN1Sequence expected, got " + asn1Prim.getClass().getSimpleName());
            return resultSet;
        }
        ASN1Sequence asn1Seq = (ASN1Sequence) asn1Prim;
        Enumeration<?> objects = asn1Seq.getObjects();
        while (objects.hasMoreElements())
        {
            // AccessDescription
            ASN1Sequence obj = (ASN1Sequence) objects.nextElement();
            ASN1Encodable oid = obj.getObjectAt(0);
            if (!X509ObjectIdentifiers.id_ad_caIssuers.equals(oid))
            {
                continue;
            }
            ASN1TaggedObject location = (ASN1TaggedObject) obj.getObjectAt(1);
            ASN1OctetString uri = (ASN1OctetString) location.getObject();
            InputStream in = null;
            try
            {
                String urlString = new String(uri.getOctets());
                LOG.info("CA issuers URL: " + urlString);
                in = new URL(urlString).openStream();
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                Collection<? extends Certificate> altCerts = certFactory.generateCertificates(in);
                for (Certificate altCert : altCerts)
                {
                    resultSet.add((X509Certificate) altCert);
                }
                LOG.info("CA issuers URL: " + altCerts.size() + " certificate(s) downloaded");
            }
            catch (IOException ex)
            {
                LOG.warn(ex.getMessage(), ex);
            }
            catch (CertificateException ex)
            {
                LOG.warn(ex.getMessage(), ex);
            }
            finally
            {
                IOUtils.closeQuietly(in);
            }
        }
        LOG.info("CA issuers: Downloaded " + resultSet.size() + " certificate(s) total");
        return resultSet;
    }

    /**
     * Attempts to build a certification chain for given certificate and to
     * verify it. Relies on a set of root CA certificates (trust anchors) and a
     * set of intermediate certificates (to be used as part of the chain).
     *
     * @param cert - certificate for validation
     * @param trustAnchors - set of trust anchors
     * @param intermediateCerts - set of intermediate certificates
     * @param signDate the date when the signing took place
     * @return the certification chain (if verification is successful)
     * @throws GeneralSecurityException - if the verification is not successful
     * (e.g. certification path cannot be built or some certificate in the chain
     * is expired)
     */
    private static PKIXCertPathBuilderResult verifyCertificate(
            X509Certificate cert, Set<TrustAnchor> trustAnchors,
            Set<X509Certificate> intermediateCerts, Date signDate)
            throws GeneralSecurityException
    {
        // Create the selector that specifies the starting certificate
        X509CertSelector selector = new X509CertSelector();
        selector.setCertificate(cert);

        // Configure the PKIX certificate builder algorithm parameters
        PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(trustAnchors, selector);

        // Disable CRL checks (this is done manually as additional step)
        pkixParams.setRevocationEnabled(false);

        // not doing this brings
        // "SunCertPathBuilderException: unable to find valid certification path to requested target"
        // (when using -Djava.security.debug=certpath: "critical policy qualifiers present in certificate")
        // for files like 021496.pdf that have the "Adobe CDS Certificate Policy" 1.2.840.113583.1.2.1
        // CDS = "Certified Document Services"
        // https://www.adobe.com/misc/pdfs/Adobe_CDS_CP.pdf
        pkixParams.setPolicyQualifiersRejected(false);
        // However, maybe there is still work to do:
        // "If the policyQualifiersRejected flag is set to false, it is up to the application
        // to validate all policy qualifiers in this manner in order to be PKIX compliant."

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
                ASN1Encodable oid = obj.getObjectAt(0);
                // accessLocation
                ASN1TaggedObject location = (ASN1TaggedObject) obj.getObjectAt(1);
                if (X509ObjectIdentifiers.id_ad_ocsp.equals(oid)
                        && location.getTagNo() == GeneralName.uniformResourceIdentifier)
                {
                    ASN1OctetString url = (ASN1OctetString) location.getObject();
                    String ocspURL = new String(url.getOctets());
                    LOG.info("OCSP URL: " + ocspURL);
                    return ocspURL;
                }
            }
        }
        return null;
    }

    /**
     * Verify whether the certificate has been revoked at signing date, and verify whether the
     * certificate of the responder has been revoked now.
     *
     * @param ocspHelper the OCSP helper.
     * @param additionalCerts
     * @throws RevokedCertificateException
     * @throws IOException
     * @throws OCSPException
     * @throws CertificateVerificationException
     */
    private static void verifyOCSP(OcspHelper ocspHelper, Set<X509Certificate> additionalCerts)
            throws RevokedCertificateException, IOException, OCSPException, CertificateVerificationException
    {
        Date now = Calendar.getInstance().getTime();
        OCSPResp ocspResponse;
        ocspResponse = ocspHelper.getResponseOcsp();
        if (ocspResponse.getStatus() != OCSPResp.SUCCESSFUL)
        {
            throw new CertificateVerificationException("OCSP check not successful, status: "
                    + ocspResponse.getStatus());
        }
        LOG.info("OCSP check successful");

        BasicOCSPResp basicResponse = (BasicOCSPResp) ocspResponse.getResponseObject();
        X509Certificate ocspResponderCertificate = ocspHelper.getOcspResponderCertificate();
        if (ocspResponderCertificate.getExtensionValue(OCSPObjectIdentifiers.id_pkix_ocsp_nocheck.getId()) != null)
        {
            // https://tools.ietf.org/html/rfc6960#section-4.2.2.2.1
            // A CA may specify that an OCSP client can trust a responder for the
            // lifetime of the responder's certificate.  The CA does so by
            // including the extension id-pkix-ocsp-nocheck.
            LOG.info("Revocation check of OCSP responder certificate skipped (id-pkix-ocsp-nocheck is set)");
            return;
        }

        if (ocspHelper.getCertificateToCheck().equals(ocspResponderCertificate))
        {
            LOG.info("OCSP responder certificate is identical to certificate to check");
            return;
        }

        LOG.info("Check of OCSP responder certificate");
        Set<X509Certificate> additionalCerts2 = new HashSet<X509Certificate>(additionalCerts);
        JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter();
        for (X509CertificateHolder certHolder : basicResponse.getCerts())
        {
            try
            {
                X509Certificate cert = certificateConverter.getCertificate(certHolder);
                if (!ocspResponderCertificate.equals(cert))
                {
                    additionalCerts2.add(cert);
                }
            }
            catch (CertificateException ex)
            {
                // unlikely to happen because the certificate existed as an object
                LOG.error(ex, ex);
            }
        }
        CertificateVerifier.verifyCertificate(ocspResponderCertificate, additionalCerts2, true, now);
        LOG.info("Check of OCSP responder certificate done");
    }
}
