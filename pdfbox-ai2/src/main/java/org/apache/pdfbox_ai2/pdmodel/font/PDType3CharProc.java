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

package org.apache.pdfbox_ai2.pdmodel.font;

import java.io.IOException;
import java.io.InputStream;
import org.apache.pdfbox_ai2.contentstream.PDContentStream;
import org.apache.pdfbox_ai2.cos.COSStream;
import org.apache.pdfbox_ai2.pdmodel.PDResources;
import org.apache.pdfbox_ai2.pdmodel.common.COSObjectable;
import org.apache.pdfbox_ai2.pdmodel.common.PDRectangle;
import org.apache.pdfbox_ai2.pdmodel.common.PDStream;
import org.apache.pdfbox_ai2.util.Matrix;

/**
 * A Type 3 character procedure. This is a standalone PDF content stream.
 *
 * @author John Hewson
 */
public final class PDType3CharProc implements COSObjectable, PDContentStream
{
    private final PDType3Font font;
    private final COSStream charStream;

    public PDType3CharProc(PDType3Font font, COSStream charStream)
    {
        this.font = font;
        this.charStream = charStream;
    }

    @Override
    public COSStream getCOSObject()
    {
        return charStream;
    }

    public PDType3Font getFont()
    {
        return font;
    }
    
    public PDStream getContentStream()
    {
        return new PDStream(charStream);
    }

    @Override
    public InputStream getContents() throws IOException
    {
        return charStream.getUnfilteredStream();
    }

    @Override
    public PDResources getResources()
    {
        return font.getResources();
    }

    @Override
    public PDRectangle getBBox()
    {
        return font.getFontBBox();
    }

    @Override
    public Matrix getMatrix()
    {
        return font.getFontMatrix();
    }

    // todo: add methods for getting the character's width from the stream
}
