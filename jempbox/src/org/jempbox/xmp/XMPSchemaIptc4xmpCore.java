/**
 * Copyright (c) 2006, www.jempbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.jempbox.org
 *
 */
package org.jempbox.xmp;

import java.util.List;

import org.w3c.dom.Element;

/**
 * Define XMP properties used with IPTC specification.
 * 
 * @author $Author: benlitchfield $
 * @version $Revision: 1.3 $
 */
public class XMPSchemaIptc4xmpCore extends XMPSchema
{
    /**
     * The namespace for this schema.
     */
    public static final String NAMESPACE = "http://iptc.org/std/Iptc4xmpCore/1.0/xmlns/";

    /**
     * Construct a new blank IPTC schema.
     * 
     * @param metadata The parent metadata schema that this will be part of.
     */
    public XMPSchemaIptc4xmpCore(XMPMetadata metadata)
    {
        super(metadata, "Iptc4xmpCore", NAMESPACE);
    }

    /**
     * Constructor from an existing element.
     * 
     * @param element The XML element.
     * @param aPrefix The XML prefix; Iptc4xmpCore.
     */
    public XMPSchemaIptc4xmpCore(Element element, String aPrefix)
    {
        super(element, aPrefix);
    }

    /**
     * Contact Info Address City.
     * 
     * @param city The city name.
     */
    public void setCiAdrCity( String city )
    {
        setTextProperty(prefix + ":CiAdrCity", city);
    }
    
    /**
     * Contact Info Address City.
     * 
     * @return The city.
     */
    public String getCiAdrCity()
    {
        return getTextProperty(prefix + ":CiAdrCity");
    }
    
    /**
     * Contact Info country.
     * 
     * @param country The CI country.
     */
    public void setCiAdrCtry( String country )
    {
        setTextProperty(prefix + ":CiAdrCtry", country);
    }

    /**
     * Contact Info country.
     * 
     * @return The CI country.
     */
    public String getCiAdrCtry()
    {
        return getTextProperty(prefix + ":CiAdrCtry");
    }

    /**
     * Contact Info Extended Address(company name).
     * 
     * @param adr Address info.
     */
    public void setCiAdrExtadr( String adr )
    {
        setTextProperty(prefix + ":CiAdrExtadr", adr);
    }

    /**
     * Contact Info Extended Address(company name).
     * 
     * @return The extended address info.
     */
    public String getCiAdrExtadr()
    {
        return getTextProperty(prefix + ":CiAdrExtadr");
    }

    /**
     * Postal code.
     * 
     * @param po The postal code.
     */
    public void setCiAdrPcode( String po )
    {
        setTextProperty(prefix + ":CiAdrPcode", po);
    }

    /**
     * Postal code.
     * 
     * @return The postal code.
     */
    public String getCiAdrPcode()
    {
        return getTextProperty(prefix + ":CiAdrPcode");
    }

    /**
     * Postal region or state.
     * 
     * @param state The postal region
     */
    public void setCiAdrRegion( String state )
    {
        setTextProperty(prefix + ":CiAdrRegion", state);
    }

    /**
     * Postal region or state.
     * 
     * @return The postal state.
     */
    public String getCiAdrRegion()
    {
        return getTextProperty(prefix + ":CiAdrRegion");
    }

    /**
     * Work email.
     * 
     * @param email The work email.
     */
    public void setCiEmailWork( String email )
    {
        setTextProperty(prefix + ":CiEmailWork", email);
    }

    /**
     * Work email.
     * 
     * @return The work email.
     */
    public String getCiEmailWork()
    {
        return getTextProperty(prefix + ":CiEmailWork");
    }

    /**
     * Work telephone.
     * 
     * @param tel The work telephone.
     */
    public void setCiTelWork( String tel )
    {
        setTextProperty(prefix + ":CiTelWork", tel);
    }

    /**
     * Work Telephone.
     * 
     * @return The work telephone.
     */
    public String getCiTelWork()
    {
        return getTextProperty(prefix + ":CiTelWork");
    }

    /**
     * Work URL.
     * 
     * @param url The work URL.
     */
    public void setCiUrlWork( String url )
    {
        setTextProperty(prefix + ":CiUrlWork", url);
    }

    /**
     * Work URL.
     * 
     * @return work URL.
     */
    public String getCiUrlWork()
    {
        return getTextProperty(prefix + ":CiUrlWork");
    }

    /**
     * Name of location that the content is focussing on.
     * 
     * @param loc The location.
     */
    public void setLocation( String loc )
    {
        setTextProperty(prefix + ":Location", loc);
    }

    /**
     * Name of location that the content is focussing on.
     * @return The location.
     */
    public String getLocation()
    {
        return getTextProperty(prefix + ":Location");
    }

    /**
     * The IPTC scene.
     * 
     * @param scene The IPTC scene.
     */
    public void addScene( String scene )
    {
        addBagValue(prefix + ":Scene", scene);
    }

    /**
     * A list of all the scenes.
     * 
     * @return The list of scenes.
     */
    public List getScenes()
    {
        return getBagList(prefix + ":Scene");
    }

    /**
     * Add IPTC subject code.
     * @param subject The IPTC subject.
     */
    public void addSubjectCode( String subject )
    {
        addBagValue(prefix + ":SubjectCode", subject);
    }

    /**
     * Get a list of all IPTC subject codes.
     * 
     * @return All IPTC subject codes.
     */
    public List getSubjectCodes()
    {
        return getBagList(prefix + ":SubjectCode");
    }

    /**
     * Nature of a news object.
     * 
     * @param genre The news genre.
     */
    public void setIntellectualGenre( String genre )
    {
        setTextProperty(prefix + ":IntellectualGenre", genre);
    }

    /**
     * Nature of a news object.
     * 
     * @return The news genre.
     */
    public String getIntellectualGenre()
    {
        return getTextProperty(prefix + ":IntellectualGenre");
    }

    /**
     * ISO Country Code.
     * 
     * @param code The country code.
     */
    public void setCountryCode( String code )
    {
        setTextProperty(prefix + ":CountryCode", code);
    }

    /**
     * ISO Country Code.
     * 
     * @return The country code.
     */
    public String getCountryCode()
    {
        return getTextProperty(prefix + ":CountryCode");
    }
}