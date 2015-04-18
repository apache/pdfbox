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
package org.apache.pdfbox.cos;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents a null PDF object.
 *
 * @author Ben Litchfield
 */
public final class COSNull extends COSBase
{
    /**
     * The null token.
     */
    public static final byte[] NULL_BYTES = new byte[] {110, 117, 108, 108}; //"null".getBytes( "ISO-8859-1" );

    /**
     * The one null object in the system.
     */
    public static final COSNull NULL = new COSNull();

    /**
     * Constructor.
     */
    private COSNull()
    {
        //limit creation to one instance.
    }

    /**
     * visitor pattern double dispatch method.
     *
     * @param visitor The object to notify when visiting this object.
     * @return any object, depending on the visitor implementation, or null
     * @throws IOException If an error occurs while visiting this object.
     */
    @Override
    public Object accept( ICOSVisitor  visitor ) throws IOException
    {
        return visitor.visitFromNull( this );
    }

    /**
     * This will output this string as a PDF object.
     *
     * @param output The stream to write to.
     * @throws IOException If there is an error writing to the stream.
     */
    public void writePDF( OutputStream output ) throws IOException
    {
        output.write(NULL_BYTES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "COSNull{}";
    }
}
