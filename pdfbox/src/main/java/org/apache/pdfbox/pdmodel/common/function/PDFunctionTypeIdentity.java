/*
 * Copyright 2014 The Apache Software Foundation.
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
package org.apache.pdfbox.pdmodel.common.function;

import java.io.IOException;
import org.apache.pdfbox.cos.COSBase;

/**
 * The identity function.
 *
 * @author Tilman Hausherr
 */
public class PDFunctionTypeIdentity extends PDFunction
{

    public PDFunctionTypeIdentity(COSBase function)
    {
        super(null);
    }

    @Override
    public int getFunctionType()
    {
        // shouldn't be called
        throw new UnsupportedOperationException();
    }

    @Override
    public float[] eval(float[] input) throws IOException
    {
        return input;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "FunctionTypeIdentity";
    }

}
