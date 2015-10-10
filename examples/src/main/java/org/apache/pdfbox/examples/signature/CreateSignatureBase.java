/*
 * Copyright 2015 The Apache Software Foundation.
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

package org.apache.pdfbox.examples.signature;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.util.Store;

public abstract class CreateSignatureBase implements SignatureInterface
{
    private PrivateKey privateKey;
    private Certificate certificate;
    private TSAClient tsaClient;

    public void setPrivateKey(PrivateKey privateKey)
    {
        this.privateKey = privateKey;
    }

    public void setCertificate(Certificate certificate)
    {
        this.certificate = certificate;
    }

    public void setTsaClient(TSAClient tsaClient)
    {
        this.tsaClient = tsaClient;
    }

    public TSAClient getTsaClient()
    {
        return tsaClient;
    }

    /**
     * Does nothing. Override this if needed.
     *
     * @param signedData Generated CMS signed data
     * @return CMSSignedData Extended CMS signed data
     */
    protected CMSSignedData signTimeStamps(CMSSignedData signedData) throws IOException, TSPException
    {
        return signedData;
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
            List<Certificate> certList = new ArrayList<Certificate>();
            certList.add(certificate);
            Store certs = new JcaCertStore(certList);
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            org.bouncycastle.asn1.x509.Certificate cert = org.bouncycastle.asn1.x509.Certificate.getInstance(ASN1Primitive.fromByteArray(certificate.getEncoded()));
            ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA256WithRSA").build(privateKey);
            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build()).build(sha1Signer, new X509CertificateHolder(cert)));
            gen.addCertificates(certs);
            CMSProcessableInputStream msg = new CMSProcessableInputStream(content);
            CMSSignedData signedData = gen.generate(msg, false);
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

}
