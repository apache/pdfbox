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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.Attributes;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.SignerInfoGeneratorBuilder;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.tsp.TSPException;

/**
 * An example for singing a PDF with bouncy castle.
 * A keystore can be created with the java keytool, for example:
 *
 * {@code keytool -genkeypair -storepass 123456 -storetype pkcs12 -alias test -validity 365
 *        -v -keyalg RSA -keystore keystore.p12 }
 *
 * @author Thomas Chojecki
 * @author Vakhtang Koroghlishvili
 * @author John Hewson
 */
public class CreateSignature implements SignatureInterface
{
    private PrivateKey privateKey;
    private Certificate[] certificateChain;
    private TSAClient tsaClient;

    /**
     * Initialize the signature creator with a keystore and certficate password.
     * @param keystore the keystore containing the signing certificate
     * @param password the password for recovering the key
     * @throws KeyStoreException if the keystore has not been initialized (loaded)
     * @throws NoSuchAlgorithmException if the algorithm for recovering the key cannot be found
     * @throws UnrecoverableKeyException if the given password is wrong
     */
    public CreateSignature(KeyStore keystore, char[] password)
            throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException
    {
        // grabs the first alias from the keystore and get the private key. An
        // TODO alternative method or constructor could be used for setting a specific
        // alias that should be used.
        Enumeration<String> aliases = keystore.aliases();
        String alias;
        if (aliases.hasMoreElements())
        {
            alias = aliases.nextElement();
        }
        else
        {
            throw new KeyStoreException("Keystore is empty");
        }
        privateKey = (PrivateKey) keystore.getKey(alias, password);
        certificateChain = keystore.getCertificateChain(alias);
    }

    /**
     * Signs the given PDF file. Alters the original file on disk.
     * @param file the PDF file to sign
     * @throws IOException if the file could not be read or written
     */
    public void signDetached(File file) throws IOException
    {
        signDetached(file, file, null);
    }

    /**
     * Signs the given PDF file.
     * @param inFile is the PDF file
     * @throws IOException if the input file could not be read
     */
    public void signDetached(File inFile, File outFile) throws IOException
    {
        signDetached(inFile, outFile, null);
    }

    /**
     * Signs the given PDF file.
     * @param inFile is the PDF file
     * @param tsaClient TSA client
     * @throws IOException if the input file could not be read
     */
    public void signDetached(File inFile, File outFile, TSAClient tsaClient) throws IOException
    {
        if (inFile == null || !inFile.exists())
        {
            throw new FileNotFoundException("Document for signing does not exist");
        }

        FileOutputStream fos = new FileOutputStream(outFile);

        // sign
        PDDocument doc = PDDocument.loadLegacy(inFile);
        signDetached(doc, fos, tsaClient);
        doc.close();
    }

    public void signDetached(PDDocument document, OutputStream output, TSAClient tsaClient)
            throws IOException
    {
        this.tsaClient = tsaClient;

        // create signature dictionary
        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName("Example User");
        signature.setLocation("Los Angeles, CA");
        signature.setReason("Testing");
        // TODO extract the above details from the signing certificate? Reason as a parameter?

        // the signing date, needed for valid signature
        signature.setSignDate(Calendar.getInstance());

        // register signature dictionary and sign interface
        document.addSignature(signature, this);

        // write incremental (only for signing purpose)
        document.saveIncremental(output);
    }

    /**
     * We just extend CMS signed Data
     *
     * @param signedData -Generated CMS signed data
     * @return CMSSignedData - Extended CMS signed data
     */
    private CMSSignedData signTimeStamps(CMSSignedData signedData)
            throws IOException, TSPException
    {
        SignerInformationStore signerStore = signedData.getSignerInfos();
        List<SignerInformation> newSigners = new ArrayList<SignerInformation>();

        for (SignerInformation signer : (Collection<SignerInformation>)signerStore.getSigners())
        {
            newSigners.add(signTimeStamp(signer));
        }

        // TODO do we have to return a new store?
        return CMSSignedData.replaceSigners(signedData, new SignerInformationStore(newSigners));
    }

    /**
     * We are extending CMS Signature
     *
     * @param signer information about signer
     * @return information about SignerInformation
     */
    private SignerInformation signTimeStamp(SignerInformation signer)
            throws IOException, TSPException
    {
        AttributeTable unsignedAttributes = signer.getUnsignedAttributes();

        ASN1EncodableVector vector = new ASN1EncodableVector();
        if (unsignedAttributes != null)
        {
            vector = unsignedAttributes.toASN1EncodableVector();
        }

        byte[] token = tsaClient.getTimeStampToken(signer.getSignature());
        ASN1ObjectIdentifier oid = PKCSObjectIdentifiers.id_aa_signatureTimeStampToken;
        ASN1Encodable signatureTimeStamp = new Attribute(oid, new DERSet(byteToASN1Object(token)));

        vector.add(signatureTimeStamp);
        Attributes signedAttributes = new Attributes(vector);

        SignerInformation newSigner = SignerInformation.replaceUnsignedAttributes(
                signer, new AttributeTable(signedAttributes));

        // TODO can this actually happen?
        if (newSigner == null)
        {
            return signer;
        }

        return newSigner;
    }

    /**
     * Bytes to ASN.1
     * @param data time stamp token byte
     * @return ASN1Object which is created by the ASN1InputStream of the time stamp token
     * @throws IOException if we can't cast ASN1Primitive to ASN1Object
     */
    private ASN1Object byteToASN1Object(byte[] data) throws IOException
    {
        ASN1InputStream in = new ASN1InputStream(data);
        try
        {
            return in.readObject();
        }
        finally
        {
            in.close();
        }
    }

    /**
     * SignatureInterface implementation.
     *
     * This method will be called from inside of the pdfbox and create the PKCS #7 signature.
     * The given InputStream contains the bytes that are given by the byte range.
     *
     * This method is for internal use only. <-- TODO this method should be private
     *
     * Use your favorite cryptographic library to implement PKCS #7 signature creation.
     */
    @Override
    public byte[] sign(InputStream content) throws IOException
    {
        try
        {
            org.bouncycastle.asn1.x509.Certificate certificate =
                    org.bouncycastle.asn1.x509.Certificate.getInstance(ASN1Primitive.fromByteArray(certificateChain[0].getEncoded()));
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();

            
            AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256WITHRSAENCRYPTION");
            AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
            RSAPrivateKey privateRSAKey = (RSAPrivateKey)privateKey; 
            RSAKeyParameters keyParams = new RSAKeyParameters(true, privateRSAKey.getModulus(), privateRSAKey.getPrivateExponent()); 
            ContentSigner sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(keyParams);

            gen.addSignerInfoGenerator(
                    new SignerInfoGeneratorBuilder(new BcDigestCalculatorProvider())
                        .build(sigGen, new X509CertificateHolder(certificate)));
            CMSProcessableInputStream processable = new CMSProcessableInputStream(content);
            CMSSignedData signedData = gen.generate(processable, false);
            if (tsaClient != null)
            {
                signedData = signTimeStamps(signedData);
            }
            return signedData.getEncoded();
        }
        catch (GeneralSecurityException e)
        {
            throw new IOException(e);
        }
        catch (CMSException e)
        {
            throw new IOException(e);
        }
        catch (TSPException e)
        {
            throw new IOException(e);
        }
        catch (OperatorCreationException e)
        {
            throw new IOException(e);
        }
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException
    {
        if (args.length < 3)
        {
            usage();
            System.exit(1);
        }

        String tsaUrl = null;
        for(int i = 0; i < args.length; i++)
        {
            if (args[i].equals("-tsa"))
            {
                i++;
                if (i >= args.length)
                {
                    usage();
                }
                tsaUrl = args[i];
            }
        }

        // load the keystore
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        char[] password = args[1].toCharArray(); // TODO use Java 6 java.io.Console.readPassword
        keystore.load(new FileInputStream(args[0]), password);
        // TODO alias command line argument

        // TSA client
        TSAClient tsaClient = null;
        if (tsaUrl != null)
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            tsaClient = new TSAClient(new URL(tsaUrl), null, null, digest);
        }

        // sign PDF
        CreateSignature signing = new CreateSignature(keystore, password);

        File inFile = new File(args[2]);
        String name = inFile.getName();
        String substring = name.substring(0, name.lastIndexOf("."));

        File outFile = new File(inFile.getParent(), substring + "_signed.pdf");
        signing.signDetached(inFile, outFile, tsaClient);
    }

    private static void usage()
    {
        System.err.println("usage: java " + CreateSignature.class.getName() + " " +
                           "<pkcs12_keystore> <password> <pdf_to_sign>\n" + "" +
                           "options:\n" +
                           "  -tsa <url>    sign timestamp using the given TSA server");
    }
}
