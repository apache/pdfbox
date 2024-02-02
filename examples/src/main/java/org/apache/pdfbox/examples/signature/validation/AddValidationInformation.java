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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSUpdateInfo;
import org.apache.pdfbox.examples.signature.SigUtils;
import org.apache.pdfbox.examples.signature.cert.CRLVerifier;
import org.apache.pdfbox.examples.signature.cert.CertificateVerificationException;
import org.apache.pdfbox.examples.signature.cert.OcspHelper;
import org.apache.pdfbox.examples.signature.cert.RevokedCertificateException;
import org.apache.pdfbox.examples.signature.validation.CertInformationCollector.CertSignatureInformation;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.encryption.SecurityProvider;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.util.Hex;
import org.bouncycastle.asn1.BEROctetString;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.TimeStampTokenInfo;

/**
 * An example for adding Validation Information to a signed PDF, inspired by ETSI TS 102 778-4
 * V1.1.2 (2009-12), Part 4: PAdES Long Term - PAdES-LTV Profile. This procedure appends the
 * Validation Information of the last signature (more precise its signer(s)) to a copy of the
 * document. The signature and the signed data will not be touched and stay valid.
 * <p>
 * See also <a href="http://eprints.hsr.ch/id/eprint/616">Bachelor thesis (in German) about LTV</a>
 *
 * @author Alexis Suter
 */
public class AddValidationInformation
{
    private static final Log LOG = LogFactory.getLog(AddValidationInformation.class);

    private CertInformationCollector certInformationHelper;
    private COSArray correspondingOCSPs;
    private COSArray correspondingCRLs;
    private COSDictionary vriBase;
    private COSArray ocsps;
    private COSArray crls;
    private COSArray certs;
    private final Map<X509Certificate,COSStream> certMap = new HashMap<>();
    private PDDocument document;
    private final Set<X509Certificate> foundRevocationInformation = new HashSet<>();
    private Calendar signDate;
    private final Set<X509Certificate> ocspChecked = new HashSet<>();
    //TODO foundRevocationInformation and ocspChecked have a similar purpose. One of them should likely
    // be removed and the code improved. When doing so, keep in mind that ocspChecked was added last,
    // because of a problem with freetsa.

    /**
     * Signs the given PDF file.
     * 
     * @param inFile input PDF file
     * @param outFile output PDF file
     * @throws IOException if the input file could not be read
     */
    public void validateSignature(File inFile, File outFile) throws IOException
    {
        if (inFile == null || !inFile.exists())
        {
            String err = "Document for signing ";
            if (null == inFile)
            {
                err += "is null";
            }
            else
            {
                err += "does not exist: " + inFile.getAbsolutePath();
            }
            throw new FileNotFoundException(err);
        }

        try (PDDocument doc = Loader.loadPDF(inFile);
             FileOutputStream fos = new FileOutputStream(outFile))
        {
            int accessPermissions = SigUtils.getMDPPermission(doc);
            if (accessPermissions == 1)
            {
                System.out.println("PDF is certified to forbid changes, "
                        + "some readers may report the document as invalid despite that "
                        + "the PDF specification allows DSS additions");
            }
            document = doc;
            doValidation(inFile.getAbsolutePath(), fos);
        }
    }

    /**
     * Fetches certificate information from the last signature of the document and appends a DSS
     * with the validation information to the document.
     *
     * @param filename in file to extract signature
     * @param output where to write the changed document
     * @throws IOException
     */
    private void doValidation(String filename, OutputStream output) throws IOException
    {
        certInformationHelper = new CertInformationCollector();
        CertSignatureInformation certInfo = null;
        try
        {
            PDSignature signature = SigUtils.getLastRelevantSignature(document);
            if (signature != null)
            {
                certInfo = certInformationHelper.getLastCertInfo(signature, filename);
                signDate = signature.getSignDate();
                if ("ETSI.RFC3161".equals(signature.getSubFilter()))
                {
                    byte[] contents = signature.getContents();
                    TimeStampToken timeStampToken = new TimeStampToken(new CMSSignedData(contents));
                    TimeStampTokenInfo timeStampInfo = timeStampToken.getTimeStampInfo();
                    signDate = Calendar.getInstance();
                    signDate.setTime(timeStampInfo.getGenTime());
                }
            }
        }
        catch (TSPException | CMSException | CertificateProccessingException e)
        {
            throw new IOException("An Error occurred processing the Signature", e);
        }
        if (certInfo == null)
        {
            throw new IOException(
                    "No Certificate information or signature found in the given document");
        }

        PDDocumentCatalog docCatalog = document.getDocumentCatalog();
        COSDictionary catalog = docCatalog.getCOSObject();
        catalog.setNeedToBeUpdated(true);

        COSDictionary dss = getOrCreateDictionaryEntry(COSDictionary.class, catalog, "DSS");

        addExtensions(docCatalog);

        vriBase = getOrCreateDictionaryEntry(COSDictionary.class, dss, "VRI");

        ocsps = getOrCreateDictionaryEntry(COSArray.class, dss, "OCSPs");

        crls = getOrCreateDictionaryEntry(COSArray.class, dss, "CRLs");

        certs = getOrCreateDictionaryEntry(COSArray.class, dss, "Certs");

        addRevocationData(certInfo);

        addAllCertsToCertArray();

        // write incremental
        document.saveIncremental(output);
    }

    /**
     * Gets or creates a dictionary entry. If existing checks for the type and sets need to be
     * updated.
     *
     * @param clazz the class of the dictionary entry, must implement COSUpdateInfo
     * @param parent where to find the element
     * @param name of the element
     * @return a Element of given class, new or existing
     * @throws IOException when the type of the element is wrong
     */
    private static <T extends COSBase & COSUpdateInfo> T getOrCreateDictionaryEntry(Class<T> clazz,
            COSDictionary parent, String name) throws IOException
    {
        T result;
        COSBase element = parent.getDictionaryObject(name);
        if (element != null && clazz.isInstance(element))
        {
            result = clazz.cast(element);
            result.setNeedToBeUpdated(true);
        }
        else if (element != null)
        {
            throw new IOException("Element " + name + " from dictionary is not of type "
                    + clazz.getCanonicalName());
        }
        else
        {
            try
            {
                result = clazz.getDeclaredConstructor().newInstance();
            }
            catch (ReflectiveOperationException | SecurityException e)
            {
                throw new IOException("Failed to create new instance of " + clazz.getCanonicalName(), e);
            }
            result.setDirect(false);
            parent.setItem(COSName.getPDFName(name), result);
        }
        return result;
    }

    /**
     * Fetches and adds revocation information based on the certInfo to the DSS.
     *
     * @param certInfo Certificate information from CertInformationHelper containing certificate
     * chains.
     * @throws IOException
     */
    private void addRevocationData(CertSignatureInformation certInfo) throws IOException
    {
        COSDictionary vri = new COSDictionary();
        vriBase.setItem(certInfo.getSignatureHash(), vri);

        updateVRI(certInfo, vri);

        if (certInfo.getTsaCerts() != null)
        {
            // Don't add RevocationInfo from tsa to VRI's
            correspondingOCSPs = null;
            correspondingCRLs = null;
            addRevocationDataRecursive(certInfo.getTsaCerts());
        }
    }

    /**
     * Tries to get Revocation Data (first OCSP, else CRL) from the given Certificate Chain.
     *
     * @param certInfo from which to fetch revocation data. Will work recursively through its
     * chains.
     * @throws IOException when failed to fetch an revocation data.
     */
    private void addRevocationDataRecursive(CertSignatureInformation certInfo) throws IOException
    {
        if (certInfo.isSelfSigned())
        {
            return;
        }
        // To avoid getting same revocation information twice.
        boolean isRevocationInfoFound = foundRevocationInformation.contains(certInfo.getCertificate());
        if (!isRevocationInfoFound)
        {
            if (certInfo.getOcspUrl() != null && certInfo.getIssuerCertificate() != null)
            {
                isRevocationInfoFound = fetchOcspData(certInfo);
            }
            if (!isRevocationInfoFound && certInfo.getCrlUrl() != null)
            {
                fetchCrlData(certInfo);
                isRevocationInfoFound = true;
            }

            if (certInfo.getOcspUrl() == null && certInfo.getCrlUrl() == null)
            {
                LOG.info("No revocation information for cert " + certInfo.getCertificate().getSubjectX500Principal());
            }
            else if (!isRevocationInfoFound)
            {
                throw new IOException("Could not fetch Revocation Info for Cert: "
                        + certInfo.getCertificate().getSubjectX500Principal());
            }
        }

        if (certInfo.getAlternativeCertChain() != null)
        {
            addRevocationDataRecursive(certInfo.getAlternativeCertChain());
        }

        if (certInfo.getCertChain() != null && certInfo.getCertChain().getCertificate() != null)
        {
            addRevocationDataRecursive(certInfo.getCertChain());
        }
    }

    /**
     * Tries to fetch and add OCSP Data to its containers.
     *
     * @param certInfo the certificate info, for it to check OCSP data.
     * @return true when the OCSP data has successfully been fetched and added
     * @throws IOException when Certificate is revoked.
     */
    private boolean fetchOcspData(CertSignatureInformation certInfo) throws IOException
    {
        try
        {
            addOcspData(certInfo);
            return true;
        }
        catch (OCSPException | CertificateProccessingException | IOException | URISyntaxException e)
        {
            LOG.error("Failed fetching OCSP at " + certInfo.getOcspUrl(), e);
            return false;
        }
        catch (RevokedCertificateException e)
        {
            throw new IOException(e);
        }
    }

    /**
     * Tries to fetch and add CRL Data to its containers.
     *
     * @param certInfo the certificate info, for it to check CRL data.
     * @throws IOException when failed to fetch, because no validation data could be fetched for
     * data.
     */
    private void fetchCrlData(CertSignatureInformation certInfo) throws IOException
    {
        try
        {
            addCrlRevocationInfo(certInfo);
        }
        catch (GeneralSecurityException | IOException | URISyntaxException |
               RevokedCertificateException | CertificateVerificationException e)
        {
            LOG.warn("Failed fetching CRL", e);
            throw new IOException(e);
        }
    }

    /**
     * Fetches and adds OCSP data to storage for the given Certificate.
     * 
     * @param certInfo the certificate info, for it to check OCSP data.
     * @throws IOException
     * @throws OCSPException
     * @throws CertificateProccessingException
     * @throws RevokedCertificateException
     */
    private void addOcspData(CertSignatureInformation certInfo) throws IOException, OCSPException,
            CertificateProccessingException, RevokedCertificateException, URISyntaxException
    {
        if (ocspChecked.contains(certInfo.getCertificate()))
        {
            // This certificate has been OCSP-checked before
            return;
        }
        OcspHelper ocspHelper = new OcspHelper(
                certInfo.getCertificate(),
                signDate.getTime(),
                certInfo.getIssuerCertificate(),
                new HashSet<>(certInformationHelper.getCertificateSet()),
                certInfo.getOcspUrl());
        OCSPResp ocspResp = ocspHelper.getResponseOcsp();
        ocspChecked.add(certInfo.getCertificate());
        BasicOCSPResp basicResponse = (BasicOCSPResp) ocspResp.getResponseObject();
        X509Certificate ocspResponderCertificate = ocspHelper.getOcspResponderCertificate();
        certInformationHelper.addAllCertsFromHolders(basicResponse.getCerts());
        byte[] signatureHash;
        try
        {
            // https://www.etsi.org/deliver/etsi_ts/102700_102799/10277804/01.01.02_60/ts_10277804v010102p.pdf
            // "For the signatures of the CRL and OCSP response, it is the respective signature
            // object represented as a BER-encoded OCTET STRING encoded with primitive encoding"
            BEROctetString encodedSignature = new BEROctetString(basicResponse.getSignature());
            signatureHash = MessageDigest.getInstance("SHA-1").digest(encodedSignature.getEncoded());
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new CertificateProccessingException(ex);
        }
        String signatureHashHex = Hex.getString(signatureHash);

        if (!vriBase.containsKey(signatureHashHex))
        {
            COSArray savedCorrespondingOCSPs = correspondingOCSPs;
            COSArray savedCorrespondingCRLs = correspondingCRLs;

            COSDictionary vri = new COSDictionary();
            vriBase.setItem(signatureHashHex, vri);
            CertSignatureInformation ocspCertInfo = certInformationHelper.getCertInfo(ocspResponderCertificate);

            updateVRI(ocspCertInfo, vri);

            correspondingOCSPs = savedCorrespondingOCSPs;
            correspondingCRLs = savedCorrespondingCRLs;
        }

        byte[] ocspData = ocspResp.getEncoded();

        COSStream ocspStream = writeDataToStream(ocspData);
        ocsps.add(ocspStream);
        if (correspondingOCSPs != null)
        {
            correspondingOCSPs.add(ocspStream);
        }
        foundRevocationInformation.add(certInfo.getCertificate());
    }

    /**
     * Fetches and adds CRL data to storage for the given Certificate.
     * 
     * @param certInfo the certificate info, for it to check CRL data.
     * @throws IOException
     * @throws URISyntaxException
     * @throws RevokedCertificateException
     * @throws GeneralSecurityException
     * @throws CertificateVerificationException 
     */
    private void addCrlRevocationInfo(CertSignatureInformation certInfo)
            throws IOException, RevokedCertificateException, GeneralSecurityException,
            CertificateVerificationException, URISyntaxException
    {
        X509CRL crl = CRLVerifier.downloadCRLFromWeb(certInfo.getCrlUrl());
        X509Certificate issuerCertificate = certInfo.getIssuerCertificate();

        // find the issuer certificate (usually issuer of signature certificate)
        for (X509Certificate certificate : certInformationHelper.getCertificateSet())
        {
            if (certificate.getSubjectX500Principal().equals(crl.getIssuerX500Principal()))
            {
                issuerCertificate = certificate;
                break;
            }
        }
        crl.verify(issuerCertificate.getPublicKey(), SecurityProvider.getProvider().getName());
        CRLVerifier.checkRevocation(crl, certInfo.getCertificate(), signDate.getTime(), certInfo.getCrlUrl());
        COSStream crlStream = writeDataToStream(crl.getEncoded());
        crls.add(crlStream);
        if (correspondingCRLs != null)
        {
            correspondingCRLs.add(crlStream);

            byte[] signatureHash;
            try
            {
                // https://www.etsi.org/deliver/etsi_ts/102700_102799/10277804/01.01.02_60/ts_10277804v010102p.pdf
                // "For the signatures of the CRL and OCSP response, it is the respective signature
                // object represented as a BER-encoded OCTET STRING encoded with primitive encoding"
                BEROctetString berEncodedSignature = new BEROctetString(crl.getSignature());
                signatureHash = MessageDigest.getInstance("SHA-1").digest(berEncodedSignature.getEncoded());
            }
            catch (NoSuchAlgorithmException ex)
            {
                throw new CertificateVerificationException(ex.getMessage(), ex);
            }
            String signatureHashHex = Hex.getString(signatureHash);

            if (!vriBase.containsKey(signatureHashHex))
            {
                COSArray savedCorrespondingOCSPs = correspondingOCSPs;
                COSArray savedCorrespondingCRLs = correspondingCRLs;

                COSDictionary vri = new COSDictionary();
                vriBase.setItem(signatureHashHex, vri);

                CertSignatureInformation crlCertInfo;
                try
                {
                    crlCertInfo = certInformationHelper.getCertInfo(issuerCertificate);
                }
                catch (CertificateProccessingException ex)
                {
                    throw new CertificateVerificationException(ex.getMessage(), ex);
                }

                updateVRI(crlCertInfo, vri);

                correspondingOCSPs = savedCorrespondingOCSPs;
                correspondingCRLs = savedCorrespondingCRLs;
            }
        }
        foundRevocationInformation.add(certInfo.getCertificate());
    }

    private void updateVRI(CertSignatureInformation certInfo, COSDictionary vri) throws IOException
    {
        if (certInfo.getCertificate().getExtensionValue(OCSPObjectIdentifiers.id_pkix_ocsp_nocheck.getId()) == null)
        {
            correspondingOCSPs = new COSArray();
            correspondingCRLs = new COSArray();
            addRevocationDataRecursive(certInfo);
            if (correspondingOCSPs.size() > 0)
            {
                vri.setItem(COSName.OCSP, correspondingOCSPs);
            }
            if (correspondingCRLs.size() > 0)
            {
                vri.setItem(COSName.CRL, correspondingCRLs);
            }
        }

        COSArray correspondingCerts = new COSArray();
        CertSignatureInformation ci = certInfo;
        do
        {
            X509Certificate cert = ci.getCertificate();
            try
            {
                COSStream certStream = writeDataToStream(cert.getEncoded());
                correspondingCerts.add(certStream);
                certMap.put(cert, certStream);
            }
            catch (CertificateEncodingException ex)
            {
                // should not happen because these are existing certificates
                LOG.error(ex, ex);
            }

            if (cert.getExtensionValue(OCSPObjectIdentifiers.id_pkix_ocsp_nocheck.getId()) != null)
            {
                break;
            }
            ci = ci.getCertChain();
        }
        while (ci != null);
        vri.setItem(COSName.CERT, correspondingCerts);

        vri.setDate(COSName.TU, Calendar.getInstance());
    }

    /**
     * Adds all certs to the certs-array. Make sure that all certificates are inside the
     * certificateStore of certInformationHelper. This should be the only call to fill certs.
     *
     * @throws IOException
     */
    private void addAllCertsToCertArray() throws IOException
    {
        for (X509Certificate cert : certInformationHelper.getCertificateSet())
        {
            if (!certMap.containsKey(cert))
            {
                try
                {
                    COSStream certStream = writeDataToStream(cert.getEncoded());
                    certMap.put(cert, certStream);
                }
                catch (CertificateEncodingException ex)
                {
                    throw new IOException(ex);
                }
            }
        }
        certMap.values().forEach(certStream -> certs.add(certStream));
    }

    /**
     * Creates a Flate encoded <code>COSStream</code> object with the given data.
     * 
     * @param data to write into the COSStream
     * @return COSStream a COSStream object that can be added to the document
     * @throws IOException
     */
    private COSStream writeDataToStream(byte[] data) throws IOException
    {
        COSStream stream = document.getDocument().createCOSStream();
        try (OutputStream os = stream.createOutputStream(COSName.FLATE_DECODE))
        {
            os.write(data);
        }
        return stream;
    }

    /**
     * Adds Extensions to the document catalog. So that the use of DSS is identified. Described in
     * PAdES Part 4, Chapter 4.4.
     *
     * @param catalog to add Extensions into
     */
    private void addExtensions(PDDocumentCatalog catalog)
    {
        COSDictionary dssExtensions = new COSDictionary();
        dssExtensions.setDirect(true);
        catalog.getCOSObject().setItem(COSName.EXTENSIONS, dssExtensions);

        COSDictionary adbeExtension = new COSDictionary();
        adbeExtension.setDirect(true);
        dssExtensions.setItem(COSName.ADBE, adbeExtension);

        adbeExtension.setName(COSName.BASE_VERSION, "1.7");
        adbeExtension.setInt(COSName.EXTENSION_LEVEL, 5);

        catalog.setVersion("1.7");
    }

    public static void main(String[] args) throws IOException
    {
        if (args.length != 1)
        {
            usage();
            System.exit(1);
        }

        // register BouncyCastle provider, needed for "exotic" algorithms
        Security.addProvider(SecurityProvider.getProvider());

        // add ocspInformation
        AddValidationInformation addOcspInformation = new AddValidationInformation();

        File inFile = new File(args[0]);
        String name = inFile.getName();
        String substring = name.substring(0, name.lastIndexOf('.'));

        File outFile = new File(inFile.getParent(), substring + "_LTV.pdf");
        addOcspInformation.validateSignature(inFile, outFile);
    }

    private static void usage()
    {
        System.err.println("usage: java " + AddValidationInformation.class.getName() + " "
                + "<pdf_to_add_ocsp>\n");
    }
}
