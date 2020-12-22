/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox.examples.signature.validation;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.examples.signature.cert.CertificateVerifier;
import org.apache.pdfbox.pdmodel.encryption.SecurityProvider;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.Store;

/**
 * This class helps to extract data/information from a signature. The information is held in
 * CertSignatureInformation. Some information is needed for validation processing of the
 * participating certificates.
 *
 * @author Alexis Suter
 *
 */
public class CertInformationCollector
{
    private static final Log LOG = LogFactory.getLog(CertInformationCollector.class);

    private static final int MAX_CERTIFICATE_CHAIN_DEPTH = 5;

    private final Set<X509Certificate> certificateSet = new HashSet<>();
    private final Set<String> urlSet = new HashSet<>();

    private final JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();

    private CertSignatureInformation rootCertInfo;

    /**
     * Gets the certificate information of a signature.
     * 
     * @param signature the signature of the document.
     * @param fileName of the document.
     * @return the CertSignatureInformation containing all certificate information
     * @throws CertificateProccessingException when there is an error processing the certificates
     * @throws IOException on a data processing error
     */
    public CertSignatureInformation getLastCertInfo(final PDSignature signature, final String fileName)
            throws CertificateProccessingException, IOException
    {
        try (FileInputStream documentInput = new FileInputStream(fileName))
        {
            final byte[] signatureContent = signature.getContents(documentInput);
            return getCertInfo(signatureContent);
        }
    }

    /**
     * Processes one signature and its including certificates.
     *
     * @param signatureContent the byte[]-Content of the signature
     * @return the CertSignatureInformation for this signature
     * @throws IOException
     * @throws CertificateProccessingException
     */
    private CertSignatureInformation getCertInfo(final byte[] signatureContent)
            throws CertificateProccessingException, IOException
    {
        rootCertInfo = new CertSignatureInformation();

        rootCertInfo.signatureHash = CertInformationHelper.getSha1Hash(signatureContent);

        try
        {
            final CMSSignedData signedData = new CMSSignedData(signatureContent);
            final SignerInformation signerInformation = processSignerStore(signedData, rootCertInfo);
            addTimestampCerts(signerInformation);
        }
        catch (final CMSException e)
        {
            LOG.error("Error occurred getting Certificate Information from Signature", e);
            throw new CertificateProccessingException(e);
        }
        return rootCertInfo;
    }

    /**
     * Processes an embedded signed timestamp, that has been placed into a signature. The
     * certificates and its chain(s) will be processed the same way as the signature itself.
     *
     * @param signerInformation of the signature, to get unsigned attributes from it.
     * @throws IOException
     * @throws CertificateProccessingException
     */
    private void addTimestampCerts(final SignerInformation signerInformation)
            throws IOException, CertificateProccessingException
    {
        final AttributeTable unsignedAttributes = signerInformation.getUnsignedAttributes();
        if (unsignedAttributes == null)
        {
            return;
        }
        final Attribute tsAttribute = unsignedAttributes
                .get(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken);
        if (tsAttribute == null)
        {
            return;
        }
        final ASN1Encodable obj0 = tsAttribute.getAttrValues().getObjectAt(0);
        if (!(obj0 instanceof ASN1Object))
        {
            return;
        }
        final ASN1Object tsSeq = (ASN1Object) obj0;

        try
        {
            final CMSSignedData signedData = new CMSSignedData(tsSeq.getEncoded("DER"));
            rootCertInfo.tsaCerts = new CertSignatureInformation();
            processSignerStore(signedData, rootCertInfo.tsaCerts);
        }
        catch (final CMSException e)
        {
            throw new IOException("Error parsing timestamp token", e);
        }
    }

    /**
     * Processes a signer store and goes through the signers certificate-chain. Adds the found data
     * to the certInfo. Handles only the first signer, although multiple would be possible, but is
     * not yet practicable.
     *
     * @param signedData data from which to get the SignerInformation
     * @param certInfo where to add certificate information
     * @return Signer Information of the processed certificatesStore for further usage.
     * @throws IOException on data-processing error
     * @throws CertificateProccessingException on a specific error with a certificate
     */
    private SignerInformation processSignerStore(
            final CMSSignedData signedData, final CertSignatureInformation certInfo)
            throws IOException, CertificateProccessingException
    {
        final Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
        final SignerInformation signerInformation = signers.iterator().next();

        @SuppressWarnings("unchecked") final Store<X509CertificateHolder> certificatesStore = signedData.getCertificates();
        @SuppressWarnings("unchecked") final Collection<X509CertificateHolder> matches = certificatesStore
                .getMatches((Selector<X509CertificateHolder>) signerInformation.getSID());

        final X509Certificate certificate = getCertFromHolder(matches.iterator().next());
        certificateSet.add(certificate);

        final Collection<X509CertificateHolder> allCerts = certificatesStore.getMatches(null);
        addAllCerts(allCerts);
        traverseChain(certificate, certInfo, MAX_CERTIFICATE_CHAIN_DEPTH);
        return signerInformation;
    }

    /**
     * Traverse through the Cert-Chain of the given Certificate and add it to the CertInfo
     * recursively.
     *
     * @param certificate Actual Certificate to be processed
     * @param certInfo where to add the Certificate (and chain) information
     * @param maxDepth Max depth from this point to go through CertChain (could be infinite)
     * @throws IOException on data-processing error
     * @throws CertificateProccessingException on a specific error with a certificate
     */
    private void traverseChain(final X509Certificate certificate, final CertSignatureInformation certInfo,
                               final int maxDepth) throws IOException, CertificateProccessingException
    {
        certInfo.certificate = certificate;

        // Certificate Authority Information Access
        // As described in https://tools.ietf.org/html/rfc3280.html#section-4.2.2.1
        final byte[] authorityExtensionValue = certificate.getExtensionValue(Extension.authorityInfoAccess.getId());
        if (authorityExtensionValue != null)
        {
            CertInformationHelper.getAuthorityInfoExtensionValue(authorityExtensionValue, certInfo);
        }

        if (certInfo.issuerUrl != null)
        {
            getAlternativeIssuerCertificate(certInfo, maxDepth);
        }

        // As described in https://tools.ietf.org/html/rfc3280.html#section-4.2.1.14
        final byte[] crlExtensionValue = certificate.getExtensionValue(Extension.cRLDistributionPoints.getId());
        if (crlExtensionValue != null)
        {
            certInfo.crlUrl = CertInformationHelper.getCrlUrlFromExtensionValue(crlExtensionValue);
        }

        try
        {
            certInfo.isSelfSigned = CertificateVerifier.isSelfSigned(certificate);
        }
        catch (final GeneralSecurityException ex)
        {
            throw new CertificateProccessingException(ex);
        }
        if (maxDepth <= 0 || certInfo.isSelfSigned)
        {
            return;
        }

        for (final X509Certificate issuer : certificateSet)
        {
            try
            {
                certificate.verify(issuer.getPublicKey(), SecurityProvider.getProvider());
                LOG.info("Found the right Issuer Cert! for Cert: " + certificate.getSubjectX500Principal()
                    + "\n" + issuer.getSubjectX500Principal());
                certInfo.issuerCertificate = issuer;
                certInfo.certChain = new CertSignatureInformation();
                traverseChain(issuer, certInfo.certChain, maxDepth - 1);
                break;
            }
            catch (final GeneralSecurityException ex)
            {
                // not the issuer
            }                
        }
        if (certInfo.issuerCertificate == null)
        {
            throw new IOException(
                    "No Issuer Certificate found for Cert: '" +
                            certificate.getSubjectX500Principal() + "', i.e. Cert '" +
                            certificate.getIssuerX500Principal() + "' is missing in the chain");
        }
    }

    /**
     * Get alternative certificate chain, from the Authority Information (a url). If the chain is
     * not included in the signature, this is the main chain. Otherwise there might be a second
     * chain. Exceptions which happen on this chain will be logged and ignored, because the cert
     * might not be available at the time or other reasons.
     *
     * @param certInfo base Certificate Information, on which to put the alternative Certificate
     * @param maxDepth Maximum depth to dig through the chain from here on.
     * @throws CertificateProccessingException on a specific error with a certificate
     */
    private void getAlternativeIssuerCertificate(final CertSignatureInformation certInfo, final int maxDepth)
            throws CertificateProccessingException
    {
        if (urlSet.contains(certInfo.issuerUrl))
        {
            return;
        }
        urlSet.add(certInfo.issuerUrl);
        LOG.info("Get alternative issuer certificate from: " + certInfo.issuerUrl);
        try
        {
            final URL certUrl = new URL(certInfo.issuerUrl);
            final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            try (InputStream in = certUrl.openStream())
            {
                final X509Certificate altIssuerCert = (X509Certificate) certFactory
                        .generateCertificate(in);
                certificateSet.add(altIssuerCert);

                certInfo.alternativeCertChain = new CertSignatureInformation();
                traverseChain(altIssuerCert, certInfo.alternativeCertChain, maxDepth - 1);
            }
        }
        catch (final IOException | CertificateException e)
        {
            LOG.error("Error getting alternative issuer certificate from " + certInfo.issuerUrl, e);
        }
    }

    /**
     * Gets the X509Certificate out of the X509CertificateHolder.
     *
     * @param certificateHolder to get the certificate from
     * @return a X509Certificate or <code>null</code> when there was an Error with the Certificate
     * @throws CertificateProccessingException on failed conversion from X509CertificateHolder to
     * X509Certificate
     */
    private X509Certificate getCertFromHolder(final X509CertificateHolder certificateHolder)
            throws CertificateProccessingException
    {
        try
        {
            return certConverter.getCertificate(certificateHolder);
        }
        catch (final CertificateException e)
        {
            LOG.error("Certificate Exception getting Certificate from certHolder.", e);
            throw new CertificateProccessingException(e);
        }
    }

    /**
     * Adds multiple Certificates out of a Collection of X509CertificateHolder into certificateSet.
     *
     * @param certHolders Collection of X509CertificateHolder
     */
    private void addAllCerts(final Collection<X509CertificateHolder> certHolders)
    {
        for (final X509CertificateHolder certificateHolder : certHolders)
        {
            try
            {
                final X509Certificate certificate = getCertFromHolder(certificateHolder);
                certificateSet.add(certificate);
            }
            catch (final CertificateProccessingException e)
            {
                LOG.warn("Certificate Exception getting Certificate from certHolder.", e);
            }
        }
    }

    /**
     * Gets a list of X509Certificate out of an array of X509CertificateHolder. The certificates
     * will be added to certificateSet.
     *
     * @param certHolders Array of X509CertificateHolder
     * @throws CertificateProccessingException when one of the Certificates could not be parsed.
     */
    public void addAllCertsFromHolders(final X509CertificateHolder[] certHolders)
            throws CertificateProccessingException
    {
        addAllCerts(Arrays.asList(certHolders));
    }

    /**
     * Traverse a certificate.
     *
     * @param certificate
     * @return
     * @throws CertificateProccessingException 
     */
    CertSignatureInformation getCertInfo(final X509Certificate certificate) throws CertificateProccessingException
    {
        try
        {
            final CertSignatureInformation certSignatureInformation = new CertSignatureInformation();
            traverseChain(certificate, certSignatureInformation, MAX_CERTIFICATE_CHAIN_DEPTH);
            return certSignatureInformation;
        }
        catch (final IOException ex)
        {
            throw new CertificateProccessingException(ex);
        }
    }

    /**
     * Get the set of all processed certificates until now.
     * 
     * @return a set of serial numbers to certificates.
     */
    public Set<X509Certificate> getCertificateSet()
    {
        return certificateSet;
    }

    /**
     * Data class to hold Signature, Certificate (and its chain(s)) and revocation Information
     */
    public class CertSignatureInformation
    {
        private X509Certificate certificate;
        private String signatureHash;
        private boolean isSelfSigned = false;
        private String ocspUrl;
        private String crlUrl;
        private String issuerUrl;
        private X509Certificate issuerCertificate;
        private CertSignatureInformation certChain;
        private CertSignatureInformation tsaCerts;
        private CertSignatureInformation alternativeCertChain;

        public String getOcspUrl()
        {
            return ocspUrl;
        }

        public void setOcspUrl(final String ocspUrl)
        {
            this.ocspUrl = ocspUrl;
        }

        public void setIssuerUrl(final String issuerUrl)
        {
            this.issuerUrl = issuerUrl;
        }

        public String getCrlUrl()
        {
            return crlUrl;
        }

        public X509Certificate getCertificate()
        {
            return certificate;
        }

        public boolean isSelfSigned()
        {
            return isSelfSigned;
        }

        public X509Certificate getIssuerCertificate()
        {
            return issuerCertificate;
        }

        public String getSignatureHash()
        {
            return signatureHash;
        }

        public CertSignatureInformation getCertChain()
        {
            return certChain;
        }

        public CertSignatureInformation getTsaCerts()
        {
            return tsaCerts;
        }

        public CertSignatureInformation getAlternativeCertChain()
        {
            return alternativeCertChain;
        }
    }
}
