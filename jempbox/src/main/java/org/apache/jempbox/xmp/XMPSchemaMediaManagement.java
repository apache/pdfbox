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

import java.util.List;

import org.apache.jempbox.impl.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Define XMP properties that are related to digital asset management.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class XMPSchemaMediaManagement extends XMPSchema
{
    /**
     * The namespace for this schema.
     */
    public static final String NAMESPACE = "http://ns.adobe.com/xap/1.0/mm/";
    
    /**
     * Construct a new blank PDF schema.
     *
     * @param parent The parent metadata schema that this will be part of.
     */
    public XMPSchemaMediaManagement( XMPMetadata parent )
    {
        super( parent, "xmpMM", NAMESPACE );
    }
    
    /**
     * Constructor from existing XML element.
     * 
     * @param element The existing element.
     * @param prefix The schema prefix.
     */
    public XMPSchemaMediaManagement( Element element, String prefix )
    {
        super( element, prefix );
    }
    
    /**
     * Get a reference to the original document that this document is
     * derived from.
     * 
     * @return A reference to the derived document, or null if one does not exist.
     */
    public ResourceRef getDerivedFrom()
    {
        ResourceRef retval = null;
        NodeList nodes = schema.getElementsByTagName( prefix + ":DerivedFrom" );
        if( nodes.getLength() > 0 )
        {
            Element derived = (Element)nodes.item( 0 );
            retval = new ResourceRef(derived);
        }
        else
        {
            //the old name was RenditionOf, this is now deprecated but lets
            //try to find it in case of older XMP documents.
            NodeList deprecatedNodes = schema.getElementsByTagName( "xmpMM:RenditionOf" );
            if( deprecatedNodes.getLength() > 0 )
            {
                Element derived = (Element)deprecatedNodes.item( 0 );
                retval = new ResourceRef(derived);
            }            
        }
        return retval;
    }
    
    /**
     * Create a new Derived From resource ref that can be populated.  You
     * will still need to call setDerivedFrom after this is created.
     * 
     * @return A new blank derived from instance.
     */
    public ResourceRef createDerivedFrom()
    {
        Element node = schema.getOwnerDocument().createElement( prefix + ":DerivedFrom" );
        ResourceRef ref = new ResourceRef( node );
        return ref;
    }
    
    /**
     * Set or clear the derived from value.
     * 
     * @param resource The resource reference to set.
     * 
     * @see XMPSchemaMediaManagement#createDerivedFrom()
     */
    public void setDerivedFrom( ResourceRef resource )
    {
        XMLUtil.setElementableValue( schema, prefix + ":DerivedFrom", resource );
    }
    
    /**
     * Set the common identifier to all versions of this document.  It should
     * be based on a UUID.
     *
     * @param id An identifier for the document.
     */
    public void setDocumentID( String id )
    {
        setTextProperty( prefix + ":DocumentID", id );
    }
    
    /**
     * Get id that identifies all versions of this document.
     *
     * @return The document id.
     */
    public String getDocumentID()
    {
        return getTextProperty( prefix + ":DocumentID" );
    }
    
    /**
     *
     * @param id An identifier for the current version.
     */
    public void setVersionID( String id )
    {
        setTextProperty( prefix + ":VersionID", id );
    }
    
    /**
     *
     * @return The current version id.
     */
    public String getVersionID()
    {
        return getTextProperty( prefix + ":VersionID" );
    }

    /**
     * Get a list of all historical events that have occured for this resource.
     * 
     * @return A list of ResourceEvent objects or null.
     */
    public List<ResourceEvent> getHistory()
    {
        return getEventSequenceList( prefix + ":History" );
    }
    
    /**
     * Remove an event from the list of events.
     * 
     * @param event The event to remove.
     */
    public void removeHistory( ResourceEvent event )
    {
        removeSequenceValue( prefix + ":History", event );
    }
    
    /**
     * Add a new historical event.
     * 
     * @param event The event to add to the list of history.
     */
    public void addHistory( ResourceEvent event )
    {
        addSequenceValue( prefix + ":History", event );
    }
    
    /**
     * Get a reference to the document prior to it being managed.
     * 
     * @return A reference to the managed document.
     */
    public ResourceRef getManagedFrom()
    {
        ResourceRef retval = null;
        NodeList nodes = schema.getElementsByTagName( prefix + ":ManagedFrom" );
        if( nodes.getLength() > 0 )
        {
            Element derived = (Element)nodes.item( 0 );
            retval = new ResourceRef(derived);
        }
        return retval;
    }
    
    /**
     * Create a new Managed From resource ref that can be populated.  You
     * will still need to call setManagedFrom after this is created.
     * 
     * @return A new blank managed from instance.
     */
    public ResourceRef createManagedFrom()
    {
        Element node = schema.getOwnerDocument().createElement( prefix + ":ManagedFrom" );
        ResourceRef ref = new ResourceRef( node );
        return ref;
    }
    
    /**
     * Set or clear the managed from value.
     * 
     * @param resource The resource reference to set.
     * 
     * @see XMPSchemaMediaManagement#createManagedFrom()
     */
    public void setManagedFrom( ResourceRef resource )
    {
        XMLUtil.setElementableValue( schema, prefix + ":ManagedFrom", resource );
    }
    
    /**
     * Set the asset management system that manages this resource.
     *
     * @param manager The name of the asset management system.
     */
    public void setManager( String manager )
    {
        setTextProperty( prefix + ":Manager", manager );
    }
    
    /**
     * Get the name of the asset management system that manages this resource.
     *
     * @return The name of the asset management system.
     */
    public String getManager()
    {
        return getTextProperty( prefix + ":Manager" );
    }
    
    /**
     * Set the URI identifying the managed resource.
     *
     * @param uri URI to the managed resource.
     */
    public void setManageTo( String uri )
    {
        setTextProperty( prefix + ":ManageTo", uri );
    }
    
    /**
     * Get the URI to the managed resource.
     *
     * @return The managed resource URI.
     */
    public String getManageTo()
    {
        return getTextProperty( prefix + ":ManageTo" );
    }
    
    /**
     * Set the URI identifying information about the managed resource.
     *
     * @param uri URI to the managed resource info.
     */
    public void setManageUI( String uri )
    {
        setTextProperty( prefix + ":ManageUI", uri );
    }
    
    /**
     * Get the URI to the managed resource information.
     *
     * @return The managed resource information URI.
     */
    public String getManageUI()
    {
        return getTextProperty( prefix + ":ManageUI" );
    }
}