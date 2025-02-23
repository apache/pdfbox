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

/**
 * A {@link COSUpdateState} instance manages update states for a {@link COSUpdateInfo}. Such states are used to create
 * a {@link COSIncrement} for the incremental saving of a {@link COSDocument}.
 *
 * @author Christian Appl
 * @see COSDocumentState
 * @see COSUpdateInfo
 * @see COSIncrement
 */
public class COSUpdateState
{
    
    /**
     * The {@link COSUpdateInfo} the {@link COSUpdateState} does manage update states for.
     */
    private final COSUpdateInfo updateInfo;
    /**
     * The {@link COSDocumentState} the {@link #updateInfo} is linked to.
     */
    private COSDocumentState originDocumentState = null;
    /**
     * The actual update state of {@link #updateInfo}.
     * <ul>
     * <li>{@code true}, if {@link #updateInfo} has been updated after the document completed parsing.</li>
     * <li>{@code false}, if {@link #updateInfo} has remained unaltered since the document completed parsing.</li>
     * </ul>
     */
    private boolean updated = false;
    
    /**
     * Creates a new {@link COSUpdateState} for the given {@link COSUpdateInfo}.
     *
     * @param updateInfo The {@link COSUpdateInfo}, that shall be managed by this {@link COSUpdateState}.
     */
    public COSUpdateState(COSUpdateInfo updateInfo)
    {
        this.updateInfo = updateInfo;
    }
    
    /**
     * <p>
     * Links the given {@link COSDocumentState} to the {@link #updated} state of the managed {@link #updateInfo}.<br>
     * </p>
     * <p>
     * This shall also initialize {@link #updated} accordingly and will also set the same {@link COSDocumentState} for
     * all possibly contained substructures.
     * </p>
     * <p>
     * Should {@link #originDocumentState} already have been set, by a prior call to this method, this shall deny to
     * overwrite it.
     * </p>
     * <p>
     * {@link COSDocumentState#isAcceptingUpdates()} shall determine, whether updates to {@link #updateInfo} are
     * allowed.
     * </p>
     * <p>
     * As long as no {@link COSDocumentState} is linked to this {@link COSUpdateState}, it shall not accept updates.
     * </p>
     *
     * @param originDocumentState The {@link COSDocumentState} that shall be linked to this {@link COSUpdateState}.
     * @see #originDocumentState
     * @see #updated
     */
    public void setOriginDocumentState(COSDocumentState originDocumentState)
    {
        setOriginDocumentState(originDocumentState, false);
    }
    
    /**
     * <p>
     * Links the given {@link COSDocumentState} to the {@link #updated} state of the managed {@link #updateInfo}.<br>
     * </p>
     * <p>
     * This shall also initialize {@link #updated} accordingly and will also set the same {@link COSDocumentState} for
     * all possibly contained substructures.
     * </p>
     * <p>
     * Should {@link #originDocumentState} already have been set, by a prior call to this method, this shall deny to
     * overwrite it.
     * </p>
     * <p>
     * {@link COSDocumentState#isAcceptingUpdates()} shall determine, whether updates to {@link #updateInfo} are
     * allowed.
     * </p>
     * <p>
     * As long as no {@link COSDocumentState} is linked to this {@link COSUpdateState}, it shall not accept updates.
     * </p>
     * <p>
     * Additionally to {@link #setOriginDocumentState(COSDocumentState)}, this shall also deny changing
     * {@link #updated}, should the flag {@code dereferencing} indicate, that this is caused by dereferencing a
     * {@link COSObject}.
     * </p>
     *
     * @param originDocumentState The {@link COSDocumentState} that shall be linked to this {@link COSUpdateState}.
     * @param dereferencing       {@code true}, if this update of the {@link COSDocumentState} is caused by
     *                            dereferencing a {@link COSObject}.
     * @see #originDocumentState
     * @see #updated
     */
    private void setOriginDocumentState(COSDocumentState originDocumentState, boolean dereferencing)
    {
        if(this.originDocumentState != null || originDocumentState == null)
        {
            return;
        }
        this.originDocumentState = originDocumentState;
        if(!dereferencing)
        {
            update();
        }
        
        if(updateInfo instanceof COSDictionary)
        {
            COSDictionary dictionary = (COSDictionary) updateInfo;
            for(COSBase entry : dictionary.getValues())
            {
                if (entry instanceof COSUpdateInfo)
                {
                    ((COSUpdateInfo) entry).getUpdateState().setOriginDocumentState(originDocumentState, dereferencing);
                }
            }
        }
        else if(updateInfo instanceof COSArray)
        {
            COSArray array = (COSArray) updateInfo;
            for(COSBase entry : array)
            {
                if (entry instanceof COSUpdateInfo)
                {
                    ((COSUpdateInfo) entry).getUpdateState().setOriginDocumentState(originDocumentState, dereferencing);
                }
            }
        }
        else if(updateInfo instanceof COSObject)
        {
            COSObject object = (COSObject) updateInfo;
            COSBase reference;
            if(object.isDereferenced() && (reference = object.getObject()) instanceof COSUpdateInfo)
            {
                ((COSUpdateInfo) reference).getUpdateState().setOriginDocumentState(originDocumentState, dereferencing);
            }
        }
    }
    
    /**
     * <p>
     * Returns the {@link #originDocumentState}, that is linked to the managed {@link #updateInfo}.
     * </p>
     * <p>
     * {@link COSDocumentState#isAcceptingUpdates()} shall determine, whether updates to {@link #updateInfo} are
     * allowed.
     * </p>
     * <p>
     * As long as no {@link COSDocumentState} is linked to this {@link COSUpdateState}, it shall not accept updates.
     * </p>
     *
     * @return The {@link COSDocumentState} linked to this {@link COSUpdateState}.
     * @see #setOriginDocumentState(COSDocumentState)
     */
    public COSDocumentState getOriginDocumentState()
    {
        return originDocumentState;
    }
    
    /**
     * Returns {@code true}, if the linked {@link #originDocumentState} {@link COSDocumentState#isAcceptingUpdates()}
     * and such a {@link COSDocumentState} has been linked to this {@link COSUpdateState}.
     *
     * @return {@code true}, if the linked {@link #originDocumentState} {@link COSDocumentState#isAcceptingUpdates()}
     * and such a {@link COSDocumentState} has been linked to this {@link COSUpdateState}.
     * @see #originDocumentState
     * @see COSDocumentState#isAcceptingUpdates()
     */
    boolean isAcceptingUpdates()
    {
        return originDocumentState != null && originDocumentState.isAcceptingUpdates();
    }
    
    /**
     * Returns the actual {@link #updated} state of the managed {@link #updateInfo}.
     *
     * @return The actual {@link #updated} state of the managed {@link #updateInfo}
     * @see #updated
     */
    public boolean isUpdated()
    {
        return updated;
    }
    
    /**
     * Calls {@link #update(boolean)} with {@code true} as the new update state.<br>
     * This shall only then have an effect, if {@link #isAcceptingUpdates()} returns {@code true}.
     *
     * @see #update(boolean)
     * @see #updated
     * @see #isAcceptingUpdates()
     */
    void update()
    {
        update(true);
    }
    
    /**
     * Sets the {@link #updated} state of the managed {@link #updateInfo} to the given state.<br>
     * This shall only then have an effect, if {@link #isAcceptingUpdates()} returns {@code true}.
     *
     * @param updated The state to set for {@link #updated}.
     * @see #update(boolean)
     * @see #updated
     * @see #isAcceptingUpdates()
     */
    void update(boolean updated)
    {
        if(isAcceptingUpdates())
        {
            this.updated = updated;
        }
    }
    
    /**
     * <p>
     * Shall call {@link #update()} for this {@link COSUpdateState} and shall
     * {@link #setOriginDocumentState(COSDocumentState)} for the given child, initializing it´s {@link #updated} state
     * and {@link #originDocumentState}.
     * </p>
     * <p>
     * This shall have no effect for a child, that is not an instance of {@link COSUpdateInfo}.
     * </p>
     *
     * @param child The child that shall also be updated.
     * @see #update()
     * @see #setOriginDocumentState(COSDocumentState)
     */
    void update(COSBase child)
    {
        update();
        if(child instanceof COSUpdateInfo)
        {
            ((COSUpdateInfo) child).getUpdateState().setOriginDocumentState(originDocumentState);
        }
    }
    
    /**
     * <p>
     * Shall call {@link #update()} for this {@link COSUpdateState} and shall
     * {@link #setOriginDocumentState(COSDocumentState)} for the given children, initializing their {@link #updated}
     * state and {@link #originDocumentState}.
     * </p>
     * <p>
     * This shall have no effect for a child, that is not an instance of {@link COSUpdateInfo}.
     * </p>
     *
     * @param children The children that shall also be updated.
     * @see #update()
     * @see #setOriginDocumentState(COSDocumentState)
     */
    void update(COSArray children)
    {
        update((Iterable<COSBase>) children);
    }
    
    /**
     * <p>
     * Shall call {@link #update()} for this {@link COSUpdateState} and shall
     * {@link #setOriginDocumentState(COSDocumentState)} for the given children, initializing their {@link #updated}
     * state and {@link #originDocumentState}.
     * </p>
     * <p>
     * This shall have no effect for a child, that is not an instance of {@link COSUpdateInfo}.
     * </p>
     *
     * @param children The children that shall also be updated.
     * @see #update()
     * @see #setOriginDocumentState(COSDocumentState)
     */
    void update(Iterable<COSBase> children)
    {
        update();
        if(children == null)
        {
            return;
        }
        for(COSBase child : children)
        {
            if(child instanceof COSUpdateInfo)
            {
                ((COSUpdateInfo) child).getUpdateState().setOriginDocumentState(originDocumentState);
            }
        }
    }
    
    /**
     * This shall {@link #setOriginDocumentState(COSDocumentState, boolean)} for the dereferenced child,
     * initializing its {@link #originDocumentState}.
     * <p>
     * This shall have no effect for a child, that is not an instance of {@link COSUpdateInfo} and will never change
     * the child´s {@link #updated} state.
     * </p>
     *
     * @param child The child, that has been dereferenced.
     * @see #setOriginDocumentState(COSDocumentState, boolean)
     */
    void dereferenceChild(COSBase child)
    {
        if(child instanceof COSUpdateInfo)
        {
            ((COSUpdateInfo) child).getUpdateState().setOriginDocumentState(originDocumentState, true);
        }
    }
    
    /**
     * Uses the managed {@link #updateInfo} as the base object of a new {@link COSIncrement}.
     *
     * @return A {@link COSIncrement} based on the managed {@link #updateInfo}.
     * @see COSUpdateInfo
     * @see COSIncrement
     */
    COSIncrement toIncrement()
    {
        return new COSIncrement(updateInfo);
    }
    
}
