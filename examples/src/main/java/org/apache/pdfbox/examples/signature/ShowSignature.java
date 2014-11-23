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
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * This will read a document from the filesystem, decrypt it and do something with the signature.
 * usage: java org.apache.pdfbox.examples.signature.ShowSignature &lt;password&gt; &lt;inputfile&gt;
 *
 * @author Ben Litchfield
 */
public class ShowSignature
{
    private ShowSignature()
    {
    }

    /**
     * This is the entry point for the application.
     *
     * @param args The command-line arguments.
     *
     * @throws Exception If there is an error reading the file.
     */
    public static void main( String[] args ) throws IOException, CertificateException
    {
        ShowSignature show = new ShowSignature();
        show.showSignature( args );
    }

    private void showSignature( String[] args ) throws IOException, CertificateException
    {
        if( args.length != 2 )
        {
            usage();
        }
        else
        {
            String password = args[0];
            String infile = args[1];
            PDDocument document = null;
            try
            {
                document = PDDocument.load( new File(infile), password );
                if( !document.isEncrypted() )
                {
                    System.err.println( "Warning: Document is not encrypted." );
                }

                COSDictionary trailer = document.getDocument().getTrailer();
                COSDictionary root = (COSDictionary)trailer.getDictionaryObject( COSName.ROOT );
                COSDictionary acroForm = (COSDictionary)root.getDictionaryObject( COSName.ACRO_FORM );
                COSArray fields = (COSArray)acroForm.getDictionaryObject( COSName.FIELDS );
                for( int i=0; i<fields.size(); i++ )
                {
                    COSDictionary field = (COSDictionary)fields.getObject( i );
                    COSName type = field.getCOSName( COSName.FT );
                    if( COSName.SIG.equals( type ) )
                    {
                        COSDictionary cert = (COSDictionary)field.getDictionaryObject( COSName.V );
                        if( cert != null )
                        {
                            System.out.println( "Certificate found" );
                            System.out.println( "Name=" + cert.getDictionaryObject( COSName.NAME ) );
                            System.out.println( "Modified=" + cert.getDictionaryObject( COSName.M ) );
                            COSName subFilter = (COSName)cert.getDictionaryObject( COSName.SUBFILTER );
                            if( subFilter != null )
                            {
                                if( subFilter.getName().equals( "adbe.x509.rsa_sha1" ) )
                                {
                                    COSString certString = (COSString)cert.getDictionaryObject(
                                        COSName.getPDFName( "Cert" ) );
                                    byte[] certData = certString.getBytes();
                                    CertificateFactory factory = CertificateFactory.getInstance( "X.509" );
                                    ByteArrayInputStream certStream = new ByteArrayInputStream( certData );
                                    Collection<? extends Certificate> certs = factory.generateCertificates( certStream );
                                    System.out.println( "certs=" + certs );
                                }
                                else if( subFilter.getName().equals( "adbe.pkcs7.sha1" ) )
                                {
                                    COSString certString = (COSString)cert.getDictionaryObject(
                                        COSName.CONTENTS );
                                    byte[] certData = certString.getBytes();
                                    CertificateFactory factory = CertificateFactory.getInstance( "X.509" );
                                    ByteArrayInputStream certStream = new ByteArrayInputStream( certData );
                                    Collection<? extends Certificate> certs = factory.generateCertificates( certStream );
                                    System.out.println( "certs=" + certs );
                                }
                                else
                                {
                                    System.err.println( "Unknown certificate type:" + subFilter );
                                }
                            }
                            else
                            {
                                throw new IOException( "Missing subfilter for cert dictionary" );
                            }
                        }
                        else
                        {
                            System.out.println( "Signature found, but no certificate" );
                        }
                    }
                }
            }
            finally
            {
                if( document != null )
                {
                    document.close();
                }
            }
        }
    }

    /**
     * This will print a usage message.
     */
    private static void usage()
    {
        System.err.println( "usage: java org.apache.pdfbox.examples.signature.ShowSignature " +
                            "<password> <inputfile>" );
    }
}
