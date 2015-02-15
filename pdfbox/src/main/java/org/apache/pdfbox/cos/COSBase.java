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

import org.apache.pdfbox.pdmodel.common.COSObjectable;

import java.io.IOException;

/**
 * The base object that all objects in the PDF document will extend.
 *
 * @author Ben Litchfield
 */
public abstract class COSBase implements COSObjectable
{
    private boolean direct;

    /**
     * Constructor.
     */
    public COSBase()
    {
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    @Override
    public COSBase getCOSObject()
    {
        return this;
    }

    /**
     * visitor pattern double dispatch method.
     *
     * @param visitor The object to notify when visiting this object.
     * @return any object, depending on the visitor implementation, or null
     * @throws IOException If an error occurs while visiting this object.
     */
    public abstract Object accept(ICOSVisitor visitor) throws IOException;
    
    /**
     * If the state is set true, the dictionary will be written direct into the called object. 
     * This means, no indirect object will be created.
     * 
     * @return the state
     */
    public boolean isDirect() 
    {
        return direct;
    }
    
    /**
     * Set the state true, if the dictionary should be written as a direct object and not indirect.
     * 
     * @param direct set it true, for writting direct object
     */
    public void setDirect(boolean direct)
    {
      this.direct = direct;
    }
}
