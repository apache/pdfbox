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
package org.apache.pdfbox.examples.signature;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.examples.signature.cert.CertificateVerificationException;
import org.apache.pdfbox.examples.signature.cert.CertificateVerifier;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.encryption.SecurityProvider;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.COSFilterInputStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.util.Hex;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.TimeStampTokenInfo;
import org.bouncycastle.util.CollectionStore;
import org.bouncycastle.util.Store;

/**
 * This will get the signature(s) from the document, do some verifications and
 * show the signature(s) and the certificates. This is a complex topic - the
 * code here is an example and not a production-ready solution.
 *
 * @author Ben Litchfield
 */
public final class ShowSignature
{
    private static final Log LOG = LogFactory.getLog(ShowSignature.class);

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private ShowSignature()
    {
    }

    /**
     * This is the entry point for the application.
     *
     * @param args The command-line arguments.
     *
     * @throws IOException If there is an error reading the file.
     * @throws org.bouncycastle.tsp.TSPException
     * @throws org.apache.pdfbox.examples.signature.cert.CertificateVerificationException
     * @throws java.security.GeneralSecurityException
     */
    public static void main(String[] args) throws IOException,
                                                  TSPException,
                                                  CertificateVerificationException,
                                                  GeneralSecurityException
    {
        // register BouncyCastle provider, needed for "exotic" algorithms
        Security.addProvider(SecurityProvider.getProvider());

        ShowSignature show = new ShowSignature();
        show.showSignature( args );
    }

    private void showSignature(String[] args) throws IOException,
                                                     GeneralSecurityException,
                                                     TSPException,
                                                     CertificateVerificationException
    {
        if( args.length != 2 )
        {
            usage();
        }
        else
        {
            String password = args[0];
            File infile = new File(args[1]);
            // use old-style document loading to disable leniency
            // see also https://www.pdf-insecurity.org/
            RandomAccessReadBufferedFile raFile = new RandomAccessReadBufferedFile(infile);
            // If your files are not too large, you can also download the PDF into a byte array
            // with IOUtils.toByteArray() and pass a RandomAccessBuffer() object to the
            // PDFParser constructor.
            PDFParser parser = new PDFParser(raFile, password);
            try (PDDocument document = parser.parse(false))
            {
                for (PDSignature sig : document.getSignatureDictionaries())
                {
                    COSDictionary sigDict = sig.getCOSObject();
                    byte[] contents = sig.getContents();

                    // download the signed content
                    // we're doing this as a stream, to be able to handle huge files                    
                    try (FileInputStream fis = new FileInputStream(infile);
                         InputStream signedContentAsStream = new COSFilterInputStream(fis, sig.getByteRange()))
                    {
                        System.out.println("Signature found");
                        
                        if (sig.getName() != null)
                        {
                            System.out.println("Name:     " + sig.getName());
                        }
                        if (sig.getSignDate() != null)
                        {
                            System.out.println("Modified: " + sdf.format(sig.getSignDate().getTime()));
                        }
                        String subFilter = sig.getSubFilter();
                        if (subFilter != null)
                        {
                            switch (subFilter)
                            {
                                case "adbe.pkcs7.detached":
                                case "ETSI.CAdES.detached":
                                    verifyPKCS7(signedContentAsStream, contents, sig);
                                    break;
                                case "adbe.pkcs7.sha1":
                                {
                                    // example: PDFBOX-1452.pdf
                                    CertificateFactory factory = CertificateFactory.getInstance("X.509");
                                    ByteArrayInputStream certStream = new ByteArrayInputStream(contents);
                                    Collection<? extends Certificate> certs = factory.generateCertificates(certStream);
                                    System.out.println("certs=" + certs);
                                    @SuppressWarnings({"squid:S5542","lgtm [java/weak-cryptographic-algorithm]"})
                                    MessageDigest md = MessageDigest.getInstance("SHA1");
                                    try (DigestInputStream dis = new DigestInputStream(signedContentAsStream, md))
                                    {
                                        while (dis.read() != -1)                                        
                                        {
                                            // do nothing
                                        }
                                    }
                                    byte [] hash = md.digest();
                                    verifyPKCS7(new ByteArrayInputStream(hash), contents, sig);
                                    break;
                                }
                                case "adbe.x509.rsa_sha1":
                                {
                                    // example: PDFBOX-2693.pdf
                                    COSString certString = (COSString) sigDict.getDictionaryObject(COSName.CERT);
                                    //TODO this could also be an array.
                                    if (certString == null)
                                    {
                                        System.err.println("The /Cert certificate string is missing in the signature dictionary");
                                        return;
                                    }
                                    byte[] certData = certString.getBytes();
                                    CertificateFactory factory = CertificateFactory.getInstance("X.509");
                                    ByteArrayInputStream certStream = new ByteArrayInputStream(certData);
                                    Collection<? extends Certificate> certs = factory.generateCertificates(certStream);
                                    System.out.println("certs=" + certs);
                                    
                                    X509Certificate cert = (X509Certificate) certs.iterator().next();
                                    
                                    // to verify signature, see code at
                                    // https://stackoverflow.com/questions/43383859/
                                    
                                    try
                                    {
                                        if (sig.getSignDate() != null)
                                        {
                                            cert.checkValidity(sig.getSignDate().getTime());
                                            System.out.println("Certificate valid at signing time");
                                        }
                                        else
                                        {
                                            System.err.println("Certificate cannot be verified without signing time");
                                        }
                                    }
                                    catch (CertificateExpiredException ex)
                                    {
                                        System.err.println("Certificate expired at signing time");
                                    }
                                    catch (CertificateNotYetValidException ex)
                                    {
                                        System.err.println("Certificate not yet valid at signing time");
                                    }
                                    if (CertificateVerifier.isSelfSigned(cert))
                                    {
                                        System.err.println("Certificate is self-signed, LOL!");
                                    }
                                    else
                                    {
                                        System.out.println("Certificate is not self-signed");
                                        
                                        if (sig.getSignDate() != null)
                                        {
                                            @SuppressWarnings("unchecked")
                                                    Store<X509CertificateHolder> store = new JcaCertStore(certs);
                                            SigUtils.verifyCertificateChain(store, cert, sig.getSignDate().getTime());
                                        }
                                    }
                                    break;
                                }
                                case "ETSI.RFC3161":
                                    // e.g. PDFBOX-1848, file_timestamped.pdf
                                    verifyETSIdotRFC3161(signedContentAsStream, contents);
                                    
                                    // verifyPKCS7(hash, contents, sig) does not work
                                    break;
                                    
                                default:
                                    System.err.println("Unknown certificate type: " + subFilter);
                                    break;
                            }
                        }
                        else
                        {
                            throw new IOException("Missing subfilter for cert dictionary");
                        }
                        
                        int[] byteRange = sig.getByteRange();
                        if (byteRange.length != 4)
                        {
                            System.err.println("Signature byteRange must have 4 items");
                        }
                        else
                        {
                            long fileLen = infile.length();
                            long rangeMax = byteRange[2] + (long) byteRange[3];
                            // multiply content length with 2 (because it is in hex in the PDF) and add 2 for < and >
                            int contentLen = contents.length * 2 + 2;
                            if (fileLen != rangeMax || byteRange[0] != 0 || byteRange[1] + contentLen != byteRange[2])
                            {
                                // a false result doesn't necessarily mean that the PDF is a fake
                                // see this answer why:
                                // https://stackoverflow.com/a/48185913/535646
                                System.out.println("Signature does not cover whole document");
                            }
                            else
                            {
                                System.out.println("Signature covers whole document");
                            }
                            checkContentValueWithFile(infile, byteRange, contents);
                        }
                    }
                }
                analyseDSS(document);
            }
            catch (CMSException | OperatorCreationException ex)
            {
                throw new IOException(ex);
            }
            System.out.println("Analyzed: " + args[1]);
        }
    }

    private void checkContentValueWithFile(File file, int[] byteRange, byte[] contents) throws IOException
    {
        // https://stackoverflow.com/questions/55049270
        // comment by mkl: check whether gap contains a hex value equal
        // byte-by-byte to the Content value, to prevent attacker from using a literal string
        // to allow extra space
        try (RandomAccessReadBufferedFile raf = new RandomAccessReadBufferedFile(file))
        {
            raf.seek(byteRange[1]);
            int c = raf.read();
            if (c != '<')
            {
                System.err.println("'<' expected at offset " + byteRange[1] + ", but got " + (char) c);
            }
            byte[] contentFromFile = new byte[byteRange[2] - byteRange[1] - 2];
            int contentLength = contentFromFile.length;
            int contentBytesRead = raf.read(contentFromFile);
            while (contentBytesRead > -1 && contentBytesRead < contentLength)
            {
                contentBytesRead += raf.read(contentFromFile,
                        contentBytesRead,
                        contentLength - contentBytesRead);
            }
            byte[] contentAsHex = Hex.getString(contents).getBytes(StandardCharsets.US_ASCII);
            if (contentBytesRead != contentAsHex.length)
            {
                System.err.println("Raw content length from file is " +
                        contentBytesRead +
                        ", but internal content string in hex has length " +
                        contentAsHex.length);
            }
            // Compare the two, we can't do byte comparison because of upper/lower case
            // also check that it is really hex
            for (int i = 0; i < contentBytesRead; ++i)
            {
                try
                {
                    if (Integer.parseInt(String.valueOf((char) contentFromFile[i]), 16) !=
                        Integer.parseInt(String.valueOf((char) contentAsHex[i]), 16))
                    {
                        System.err.println("Possible manipulation at file offset " +
                                (byteRange[1] + i + 1) + " in signature content");
                        break;
                    }
                }
                catch (NumberFormatException ex)
                {
                    System.err.println("Incorrect hex value");
                    System.err.println("Possible manipulation at file offset " +
                            (byteRange[1] + i + 1) + " in signature content");
                    break;
                }
            }
            c = raf.read();
            if (c != '>')
            {
                System.err.println("'>' expected at offset " + byteRange[2] + ", but got " + (char) c);
            }
        }
    }

    /**
     * Verify ETSI.RFC3161 TimeStampToken
     *
     * @param signedContentAsStream the byte sequence that has been signed
     * @param contents the /Contents field as a COSString
     * @throws CMSException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws TSPException
     * @throws OperatorCreationException
     * @throws CertificateVerificationException
     * @throws CertificateException 
     */
    private void verifyETSIdotRFC3161(InputStream signedContentAsStream, byte[] contents)
            throws CMSException, NoSuchAlgorithmException, IOException, TSPException,
            OperatorCreationException, CertificateVerificationException, CertificateException
    {
        TimeStampToken timeStampToken = new TimeStampToken(new CMSSignedData(contents));
        TimeStampTokenInfo timeStampInfo = timeStampToken.getTimeStampInfo();
        System.out.println("Time stamp gen time: " + timeStampInfo.getGenTime());
        if (timeStampInfo.getTsa() != null)
        {
            System.out.println("Time stamp tsa name: " + timeStampInfo.getTsa().getName());
        }
        
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream certStream = new ByteArrayInputStream(contents);
        Collection<? extends Certificate> certs = factory.generateCertificates(certStream);
        System.out.println("certs=" + certs);
        
        String hashAlgorithm = timeStampInfo.getMessageImprintAlgOID().getId();
        // compare the hash of the signed content with the hash in the timestamp
        MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
        try (DigestInputStream dis = new DigestInputStream(signedContentAsStream, md))
        {
            while (dis.read() != -1)
            {
                // do nothing
            }
        }
        if (Arrays.equals(md.digest(),
                timeStampInfo.getMessageImprintDigest()))
        {
            System.out.println("ETSI.RFC3161 timestamp signature verified");
        }
        else
        {
            System.err.println("ETSI.RFC3161 timestamp signature verification failed");
        }

        X509Certificate certFromTimeStamp = (X509Certificate) certs.iterator().next();
        SigUtils.checkTimeStampCertificateUsage(certFromTimeStamp);
        SigUtils.validateTimestampToken(timeStampToken);
        SigUtils.verifyCertificateChain(timeStampToken.getCertificates(),
                certFromTimeStamp,
                timeStampInfo.getGenTime());
    }

    /**
     * Verify a PKCS7 signature.
     *
     * @param signedContentAsStream the byte sequence that has been signed
     * @param contents the /Contents field as a COSString
     * @param sig the PDF signature (the /V dictionary)
     * @throws CMSException
     * @throws OperatorCreationException
     * @throws GeneralSecurityException
     * @throws CertificateVerificationException
     */
    private void verifyPKCS7(InputStream signedContentAsStream, byte[] contents, PDSignature sig)
            throws CMSException, OperatorCreationException,
                   CertificateVerificationException, GeneralSecurityException,
                   TSPException, IOException
    {
        // inspiration:
        // http://stackoverflow.com/a/26702631/535646
        // http://stackoverflow.com/a/9261365/535646
        CMSProcessable signedContent = new CMSProcessableInputStream(signedContentAsStream);
        CMSSignedData signedData = new CMSSignedData(signedContent, contents);
        Store<X509CertificateHolder> certificatesStore = signedData.getCertificates();
        if (certificatesStore.getMatches(null).isEmpty())
        {
            throw new IOException("No certificates in signature");
        }
        Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
        if (signers.isEmpty())
        {
            throw new IOException("No signers in signature");
        }
        SignerInformation signerInformation = signers.iterator().next();
        @SuppressWarnings("unchecked")
        Collection<X509CertificateHolder> matches =
                certificatesStore.getMatches(signerInformation.getSID());
        if (matches.isEmpty())
        {
            throw new IOException("Signer '" + signerInformation.getSID().getIssuer() + 
                                  ", serial# " + signerInformation.getSID().getSerialNumber() + 
                                  " does not match any certificates");
        }
        X509CertificateHolder certificateHolder = matches.iterator().next();
        X509Certificate certFromSignedData = new JcaX509CertificateConverter().getCertificate(certificateHolder);
        System.out.println("certFromSignedData: " + certFromSignedData);

        SigUtils.checkCertificateUsage(certFromSignedData);
        
        // Embedded timestamp
        TimeStampToken timeStampToken = SigUtils.extractTimeStampTokenFromSignerInformation(signerInformation);
        if (timeStampToken != null)
        {
            // tested with QV_RCA1_RCA3_CPCPS_V4_11.pdf
            // https://www.quovadisglobal.com/~/media/Files/Repository/QV_RCA1_RCA3_CPCPS_V4_11.ashx
            // also 021496.pdf and 036351.pdf from digitalcorpora
            SigUtils.validateTimestampToken(timeStampToken);
            X509Certificate certFromTimeStamp = SigUtils.getCertificateFromTimeStampToken(timeStampToken);
            // merge both stores using a set to remove duplicates
            HashSet<X509CertificateHolder> certificateHolderSet = new HashSet<>();
            certificateHolderSet.addAll(certificatesStore.getMatches(null));
            certificateHolderSet.addAll(timeStampToken.getCertificates().getMatches(null));
            SigUtils.verifyCertificateChain(new CollectionStore<>(certificateHolderSet),
                    certFromTimeStamp,
                    timeStampToken.getTimeStampInfo().getGenTime());
            SigUtils.checkTimeStampCertificateUsage(certFromTimeStamp);

            // compare the hash of the signature with the hash in the timestamp
            byte[] tsMessageImprintDigest = timeStampToken.getTimeStampInfo().getMessageImprintDigest();
            String hashAlgorithm = timeStampToken.getTimeStampInfo().getMessageImprintAlgOID().getId();
            byte[] sigMessageImprintDigest = MessageDigest.getInstance(hashAlgorithm).digest(signerInformation.getSignature());
            if (Arrays.equals(tsMessageImprintDigest, sigMessageImprintDigest))
            {
                System.out.println("timestamp signature verified");
            }
            else
            {
                System.err.println("timestamp signature verification failed");
            }
        }

        try
        {
            if (sig.getSignDate() != null)
            {
                certFromSignedData.checkValidity(sig.getSignDate().getTime());
                System.out.println("Certificate valid at signing time");
            }
            else
            {
                System.err.println("Certificate cannot be verified without signing time");
            }
        }
        catch (CertificateExpiredException ex)
        {
            System.err.println("Certificate expired at signing time");
        }
        catch (CertificateNotYetValidException ex)
        {
            System.err.println("Certificate not yet valid at signing time");
        }

        // usually not available
        if (signerInformation.getSignedAttributes() != null)
        {
            // From SignedMailValidator.getSignatureTime()
            Attribute signingTime = signerInformation.getSignedAttributes().get(CMSAttributes.signingTime);
            if (signingTime != null)
            {
                Time timeInstance = Time.getInstance(signingTime.getAttrValues().getObjectAt(0));
                try
                {
                    certFromSignedData.checkValidity(timeInstance.getDate());
                    System.out.println("Certificate valid at signing time: " + timeInstance.getDate());
                }
                catch (CertificateExpiredException ex)
                {
                    System.err.println("Certificate expired at signing time");
                }
                catch (CertificateNotYetValidException ex)
                {
                    System.err.println("Certificate not yet valid at signing time");
                }
            }
        }

        if (signerInformation.verify(new JcaSimpleSignerInfoVerifierBuilder().
                setProvider(SecurityProvider.getProvider()).build(certFromSignedData)))
        {
            System.out.println("Signature verified");
        }
        else
        {
            System.out.println("Signature verification failed");
        }

        if (CertificateVerifier.isSelfSigned(certFromSignedData))
        {
            System.err.println("Certificate is self-signed, LOL!");
        }
        else
        {
            System.out.println("Certificate is not self-signed");

            if (sig.getSignDate() != null)
            {
                SigUtils.verifyCertificateChain(certificatesStore, certFromSignedData, sig.getSignDate().getTime());
            }
            else
            {
                System.err.println("Certificate cannot be verified without signing time");
            }
        }
    }

    // for later use: get all root certificates. Will be used to check
    // whether we trust the root in the certificate chain.
    private Set<X509Certificate> getRootCertificates()
            throws GeneralSecurityException, IOException
    {
        Set<X509Certificate> rootCertificates = new HashSet<>();

        // https://stackoverflow.com/questions/3508050/
        String filename = System.getProperty("java.home") + "/lib/security/cacerts";
        KeyStore keystore;
        try (FileInputStream is = new FileInputStream(filename))
        {
            keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(is, null);
        }
        PKIXParameters params = new PKIXParameters(keystore);
        for (TrustAnchor trustAnchor : params.getTrustAnchors())
        {
            rootCertificates.add(trustAnchor.getTrustedCert());
        }

        // https://www.oracle.com/technetwork/articles/javase/security-137537.html
        try
        {
            keystore = KeyStore.getInstance("Windows-ROOT");
            keystore.load(null, null);
            params = new PKIXParameters(keystore);
            for (TrustAnchor trustAnchor : params.getTrustAnchors())
            {
                rootCertificates.add(trustAnchor.getTrustedCert());
            }
        }
        catch (InvalidAlgorithmParameterException | KeyStoreException ex)
        {
            // empty or not windows
        }

        return rootCertificates;
    }

    /**
     * Analyzes the DSS-Dictionary (Document Security Store) of the document. Which is used for signature validation.
     * The DSS is defined in PAdES Part 4 - Long Term Validation.
     * 
     * @param document PDDocument, to get the DSS from
     */
    private void analyseDSS(PDDocument document) throws IOException
    {
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        COSBase dssElement = catalog.getCOSObject().getDictionaryObject("DSS");

        if (dssElement instanceof COSDictionary)
        {
            COSDictionary dss = (COSDictionary) dssElement;
            System.out.println("DSS Dictionary: " + dss);
            COSBase certsElement = dss.getDictionaryObject("Certs");
            if (certsElement instanceof COSArray)
            {
                printStreamsFromArray((COSArray) certsElement, "Cert");
            }
            COSBase ocspsElement = dss.getDictionaryObject("OCSPs");
            if (ocspsElement instanceof COSArray)
            {
                printStreamsFromArray((COSArray) ocspsElement, "Ocsp");
            }
            COSBase crlElement = dss.getDictionaryObject("CRLs");
            if (crlElement instanceof COSArray)
            {
                printStreamsFromArray((COSArray) crlElement, "CRL");
            }
            // TODO: go through VRIs (which indirectly point to the DSS-Data)
        }
    }

    /**
     * Go through the elements of a COSArray containing each an COSStream to print in Hex.
     * 
     * @param elements COSArray of elements containing a COS Stream
     * @param description to append on Print
     * @throws IOException
     */
    private void printStreamsFromArray(COSArray elements, String description) throws IOException
    {
        for (COSBase baseElem : elements)
        {
            COSObject streamObj = (COSObject) baseElem;
            if (streamObj.getObject() instanceof COSStream)
            {
                COSStream cosStream = (COSStream) streamObj.getObject();
                try (InputStream is = cosStream.createInputStream())
                {
                    byte[] streamBytes = is.readAllBytes();
                    System.out.println(description + " (" + elements.indexOf(streamObj) + "): "
                        + Hex.getString(streamBytes));
                }
            }
        }
    }

    /**
     * This will print a usage message.
     */
    private static void usage()
    {
        System.err.println( "usage: java " + ShowSignature.class.getName() +
                            " <password (usually empty)> <inputfile>" );
        // The password is for encrypted files and has nothing to do with the signature.
        // (A PDF can be both encrypted and signed)
    }
}
