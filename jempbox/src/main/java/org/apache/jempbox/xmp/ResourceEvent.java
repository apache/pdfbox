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
package org.apache.jempbox.xmp;

import java.io.IOException;
import java.util.Calendar;

import org.apache.jempbox.impl.DateConverter;
import org.apache.jempbox.impl.XMLUtil;
import org.w3c.dom.Element;

/**
 * This class represents a high level event that occured during the processing 
 * of this resource.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class ResourceEvent implements Elementable
{   

    /**
     * Namespace for a resource event.
     */
    public static final String NAMESPACE = "http://ns.adobe.com/xap/1.0/sType/ResourceEvent#";
    
    /**
     * A predefined action.
     */
    public static final String ACTION_CONVERTED = "converted";
    /**
     * A predefined action.
     */
    public static final String ACTION_COPIED = "copied";
    /**
     * A predefined action.
     */
    public static final String ACTION_CREATED = "created";
    /**
     * A predefined action.
     */
    public static final String ACTION_CROPPED = "cropped";
    /**
     * A predefined action.
     */
    public static final String ACTION_EDITED = "edited";
    /**
     * A predefined action.
     */
    public static final String ACTION_FILTERED = "filtered";
    /**
     * A predefined action.
     */
    public static final String ACTION_FORMATTED = "formatted";
    /**
     * A predefined action.
     */
    public static final String ACTION_VERSION_UPDATED = "version_updated";
    /**
     * A predefined action.
     */
    public static final String ACTION_PRINTED = "printed";
    /**
     * A predefined action.
     */
    public static final String ACTION_PUBLISHED = "published";
    /**
     * A predefined action.
     */
    public static final String ACTION_MANAGED = "managed";
    /**
     * A predefined action.
     */
    public static final String ACTION_PRODUCED = "produced";
    /**
     * A predefined action.
     */
    public static final String ACTION_RESIZED = "resized";
    
    /**
     * The DOM representation of this object.
     */
    protected Element parent = null;
    
    /**
     * Create a resource reference based on a existing parent property set.
     *
     * @param parentElement The parent element that will store the resource properties.
     */
    public ResourceEvent( Element parentElement )
    {
        parent = parentElement;
        if( !parent.hasAttribute( "xmlns:stEvt" ) )
        {
            parent.setAttributeNS( 
                XMPSchema.NS_NAMESPACE, 
                "xmlns:stEvt", 
                NAMESPACE );
        }
    }
    
    /**
     * Create resource event based on schema.
     *
     * @param schema The schema that this event will be part of.
     */
    public ResourceEvent( XMPSchema schema ) 
    {
        parent = schema.getElement().getOwnerDocument().createElement( "rdf:li" );
        parent.setAttributeNS( 
            XMPSchema.NS_NAMESPACE, 
            "xmlns:stEvt", 
            NAMESPACE );               
    }
    
    /**
     * Get the underlying XML element.
     * 
     * @return The XML element that this object represents.
     */
    public Element getElement()
    {
        return parent;
    }
    
    /**
     * Get the action that occured.  See the ACTION_XXX constants.
     * 
     * @return An action key, such as 'created' or 'printed'.
     */
    public String getAction()
    {
        return XMLUtil.getStringValue( parent, "stEvt:action" );
    }
    
    /**
     * Set the action that this event represents.  See the ACTION_XXX constants.
     * 
     * @param action The action that this event represents.
     */
    public void setAction( String action )
    {
        XMLUtil.setStringValue( parent, "stEvt:action", action );
    }
    
    /**
     * Get the referenced resource's instance id.
     * 
     * @return The id of the reference document instance.
     */
    public String getInstanceID()
    {
        return XMLUtil.getStringValue( parent, "stEvt:instanceID" );
    }
    
    /**
     * Set the referenced resource's document instance id.
     * 
     * @param id The id of the reference document instance.
     */
    public void setInstanceID( String id )
    {
        XMLUtil.setStringValue( parent, "stEvt:instanceID", id );
    }
    
    /**
     * Get an additional description of the event.
     * 
     * @return Additional description of this event
     */
    public String getParameters()
    {
        return XMLUtil.getStringValue( parent, "stEvt:parameters" );
    }
    
    /**
     * Set some addition description to this event.
     * 
     * @param param The additional action parameters.
     */
    public void setParameters( String param )
    {
        XMLUtil.setStringValue( parent, "stEvt:parameters", param );
    }
    
    /**
     * Get the software that performed this action.
     * 
     * @return The software that performed the action.
     */
    public String getSoftwareAgent()
    {
        return XMLUtil.getStringValue( parent, "stEvt:softwareAgent" );
    }
    
    /**
     * Set the software that performed this operation.
     * 
     * @param software The name of the software that performed this action.
     */
    public void setSoftwareAgent( String software )
    {
        XMLUtil.setStringValue( parent, "stEvt:softwareAgent", software );
    }
    
    /**
     * Get the date/time that this event occured.
     * 
     * @return The date of the event.
     * 
     * @throws IOException If there is an error creating the date.
     */
    public Calendar getWhen() throws IOException
    {
        return DateConverter.toCalendar( XMLUtil.getStringValue( parent, "stEvt:when" ) );
    }
    
    /**
     * Set when the event occured.
     * 
     * @param when The date that the event occured.
     */
    public void setWhen( Calendar when )
    {
        XMLUtil.setStringValue( parent, "stEvt:when", DateConverter.toISO8601( when ) );
    }
    
    /**
     * Get name of the asset management system that manages this resource.
     * 
     * @return The name of a asset management system.
     */
    public String getManager()
    {
        return XMLUtil.getStringValue( parent, "stRef:manager" );
    }
    
    /**
     * Set the name of the system that manages this resource.
     * 
     * @param manager The name of the management system.
     */
    public void setMangager( String manager )
    {
        XMLUtil.setStringValue( parent, "stRef:manager", manager );
    }
    
    /**
     * Get name of the variant of asset management system that manages this resource.
     * 
     * @return The name of a asset management system.
     */
    public String getManagerVariant()
    {
        return XMLUtil.getStringValue( parent, "stRef:managerVariant" );
    }
    
    /**
     * Set the name of the variant of the system that manages this resource.
     * 
     * @param managerVariant The name of the management system.
     */
    public void setMangagerVariant( String managerVariant )
    {
        XMLUtil.setStringValue( parent, "stRef:managerVariant", managerVariant );
    }
    
    /**
     * URI identifying the managed resource.
     * 
     * @return The URI to resource.
     */
    public String getManagerTo()
    {
        return XMLUtil.getStringValue( parent, "stRef:managerTo" );
    }
    
    /**
     * Set the URI to the managed resource.
     * 
     * @param managerTo The URI to the managed resource.
     */
    public void setMangagerTo( String managerTo )
    {
        XMLUtil.setStringValue( parent, "stRef:managerTo", managerTo );
    }
    
    /**
     * URI to info about the managed resource.
     * 
     * @return The URI to the resource info.
     */
    public String getManagerUI()
    {
        return XMLUtil.getStringValue( parent, "stRef:managerUI" );
    }
    
    /**
     * Set the URI to the info about the managed resource.
     * 
     * @param managerUI The URI to the managed resource information.
     */
    public void setMangagerUI( String managerUI )
    {
        XMLUtil.setStringValue( parent, "stRef:managerUI", managerUI );
    }
    
}