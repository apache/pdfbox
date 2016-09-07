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
package org.apache.pdfbox.pdmodel.interactive.digitalsignature;

import org.apache.pdfbox.pdfwriter.COSWriter;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class to be used when creating PDF signatures externally. COSWriter is used to obtain data to be
 * signed and set the resulted CMS signature.
 *
 */
public class SigningSupport implements ExternalSigningSupport, Closeable
{
    private COSWriter cosWriter;

    public SigningSupport(COSWriter cosWriter)
    {
        this.cosWriter = cosWriter;
    }

    @Override
    public InputStream getContent() throws IOException
    {
        return cosWriter.getDataToSign();
    }

    @Override
    public void setSignature(byte[] signature) throws IOException
    {
        cosWriter.writeExternalSignature(signature);
    }

    @Override
    public void close() throws IOException
    {
        if (cosWriter != null)
        {
            try
            {
                cosWriter.close();
            }
            finally
            {
                cosWriter = null;
            }
        }
    }
}
