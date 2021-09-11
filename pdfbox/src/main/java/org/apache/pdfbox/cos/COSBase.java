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

import org.apache.pdfbox.cos.observer.event.COSDirectObjectEvent;
import org.apache.pdfbox.cos.observer.event.COSEvent;
import org.apache.pdfbox.cos.observer.COSObserver;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The base object that all objects in the PDF document will extend.
 *
 * @author Ben Litchfield
 */
public abstract class COSBase implements COSObjectable
{

    private final List<COSObserver> cosChangeObservers = new ArrayList<>();
    private boolean direct;
    private COSObjectKey key;

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
     * @param direct set it true, for writing direct object
     */
    public void setDirect(boolean direct)
    {
        this.direct = direct;
        if(direct){
            reportUpdate(new COSDirectObjectEvent<>(this));
        }
    }

    /**
     * This will return the COSObjectKey of an indirect object.
     * 
     * @return the COSObjectKey
     */
    public COSObjectKey getKey()
    {
        return key;
    }

    /**
     * Set the COSObjectKey of an indirect object.
     * 
     * @param key the COSObjectKey of the indirect object
     */
    public void setKey(COSObjectKey key)
    {
        this.key = key;
    }

    /**
     * Register the given {@link COSObserver} to this {@link COSBase}.
     *
     * @param observer The {@link COSObserver} to register.
     * @see #reportUpdate(COSEvent)
     */
    public void registerObserver(COSObserver observer)
    {
        this.cosChangeObservers.add(observer);
    }

    /**
     * Unregister the given {@link COSObserver} from this {@link COSBase}.
     *
     * @param observer The {@link COSObserver} to unregister.
     * @see #reportUpdate(COSEvent)
     */
    public void unregisterObserver(COSObserver observer)
    {
        this.cosChangeObservers.remove(observer);
    }

    /**
     * Returns the {@link COSObserver}s registered to this {@link COSBase}.
     *
     * @return A list of all {@link COSObserver}s currently registered to this {@link COSBase}.
     */
    public List<COSObserver> getRegisteredObservers()
    {
        return this.cosChangeObservers;
    }

    /**
     * Report a change to this {@link COSBase} as a {@link COSEvent} to all registered {@link COSObserver}s.
     *
     * @param event      The {@link COSEvent} to report.
     * @param <COS_TYPE> The explicit {@link COSBase} type of the reporting object.
     * @see #registerObserver(COSObserver)
     */
    public <COS_TYPE extends COSBase> void reportUpdate(COSEvent<COS_TYPE> event)
    {
        for (COSObserver observer : this.cosChangeObservers) {
            observer.reportUpdate(event);
        }
    }

}
