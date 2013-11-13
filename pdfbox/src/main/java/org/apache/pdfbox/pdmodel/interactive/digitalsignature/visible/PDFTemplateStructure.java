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
package org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible;

import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;

/**
 * Structure of PDF document with visible signature
 * 
 * @author Vakhtang koroghlishvili (Gogebashvili)
 * 
 */
public class PDFTemplateStructure
{

    private PDPage page;
    private PDDocument template;
    private PDAcroForm acroForm;
    private PDSignatureField signatureField;
    private PDSignature pdSignature;
    private COSDictionary acroFormDictionary;
    private PDRectangle singatureRectangle;
    private AffineTransform affineTransform;
    private COSArray procSet;
    private PDJpeg jpedImage;
    private PDRectangle formaterRectangle;
    private PDStream holderFormStream;
    private PDResources holderFormResources;
    private PDXObjectForm holderForm;
    private PDAppearanceDictionary appearanceDictionary;
    private PDStream innterFormStream;
    private PDResources innerFormResources;
    private PDXObjectForm innerForm;
    private PDStream imageFormStream;
    private PDResources imageFormResources;
    private List<PDField> acroFormFields;
    private String innerFormName;
    private String imageFormName;
    private String imageName;
    private COSDocument visualSignature;
    private PDXObjectForm imageForm;
    private COSDictionary widgetDictionary;

    public PDPage getPage()
    {
        return page;
    }

    public void setPage(PDPage page)
    {
        this.page = page;
    }

    public PDDocument getTemplate()
    {
        return template;
    }

    public void setTemplate(PDDocument template)
    {
        this.template = template;
    }

    public PDAcroForm getAcroForm()
    {
        return acroForm;
    }

    public void setAcroForm(PDAcroForm acroForm)
    {
        this.acroForm = acroForm;
    }

    public PDSignatureField getSignatureField()
    {
        return signatureField;
    }

    public void setSignatureField(PDSignatureField signatureField)
    {
        this.signatureField = signatureField;
    }

    public PDSignature getPdSignature()
    {
        return pdSignature;
    }

    public void setPdSignature(PDSignature pdSignature)
    {
        this.pdSignature = pdSignature;
    }

    public COSDictionary getAcroFormDictionary()
    {
        return acroFormDictionary;
    }

    public void setAcroFormDictionary(COSDictionary acroFormDictionary)
    {
        this.acroFormDictionary = acroFormDictionary;
    }

    public PDRectangle getSingatureRectangle()
    {
        return singatureRectangle;
    }

    public void setSignatureRectangle(PDRectangle singatureRectangle)
    {
        this.singatureRectangle = singatureRectangle;
    }

    public AffineTransform getAffineTransform()
    {
        return affineTransform;
    }

    public void setAffineTransform(AffineTransform affineTransform)
    {
        this.affineTransform = affineTransform;
    }

    public COSArray getProcSet()
    {
        return procSet;
    }

    public void setProcSet(COSArray procSet)
    {
        this.procSet = procSet;
    }

    public PDJpeg getJpedImage()
    {
        return jpedImage;
    }

    public void setJpedImage(PDJpeg jpedImage)
    {
        this.jpedImage = jpedImage;
    }

    public PDRectangle getFormaterRectangle()
    {
        return formaterRectangle;
    }

    public void setFormaterRectangle(PDRectangle formaterRectangle)
    {
        this.formaterRectangle = formaterRectangle;
    }

    public PDStream getHolderFormStream()
    {
        return holderFormStream;
    }

    public void setHolderFormStream(PDStream holderFormStream)
    {
        this.holderFormStream = holderFormStream;
    }

    public PDXObjectForm getHolderForm()
    {
        return holderForm;
    }

    public void setHolderForm(PDXObjectForm holderForm)
    {
        this.holderForm = holderForm;
    }

    public PDResources getHolderFormResources()
    {
        return holderFormResources;
    }

    public void setHolderFormResources(PDResources holderFormResources)
    {
        this.holderFormResources = holderFormResources;
    }

    public PDAppearanceDictionary getAppearanceDictionary()
    {
        return appearanceDictionary;
    }

    public void setAppearanceDictionary(PDAppearanceDictionary appearanceDictionary)
    {
        this.appearanceDictionary = appearanceDictionary;
    }

    public PDStream getInnterFormStream()
    {
        return innterFormStream;
    }

    public void setInnterFormStream(PDStream innterFormStream)
    {
        this.innterFormStream = innterFormStream;
    }

    public PDResources getInnerFormResources()
    {
        return innerFormResources;
    }

    public void setInnerFormResources(PDResources innerFormResources)
    {
        this.innerFormResources = innerFormResources;
    }

    public PDXObjectForm getInnerForm()
    {
        return innerForm;
    }

    public void setInnerForm(PDXObjectForm innerForm)
    {
        this.innerForm = innerForm;
    }

    public String getInnerFormName()
    {
        return innerFormName;
    }

    public void setInnerFormName(String innerFormName)
    {
        this.innerFormName = innerFormName;
    }

    public PDStream getImageFormStream()
    {
        return imageFormStream;
    }

    public void setImageFormStream(PDStream imageFormStream)
    {
        this.imageFormStream = imageFormStream;
    }

    public PDResources getImageFormResources()
    {
        return imageFormResources;
    }

    public void setImageFormResources(PDResources imageFormResources)
    {
        this.imageFormResources = imageFormResources;
    }

    public PDXObjectForm getImageForm()
    {
        return imageForm;
    }

    public void setImageForm(PDXObjectForm imageForm)
    {
        this.imageForm = imageForm;
    }

    public String getImageFormName()
    {
        return imageFormName;
    }

    public void setImageFormName(String imageFormName)
    {
        this.imageFormName = imageFormName;
    }

    public String getImageName()
    {
        return imageName;
    }

    public void setImageName(String imageName)
    {
        this.imageName = imageName;
    }

    public COSDocument getVisualSignature()
    {
        return visualSignature;
    }

    public void setVisualSignature(COSDocument visualSignature)
    {
        this.visualSignature = visualSignature;
    }

    public List<PDField> getAcroFormFields()
    {
        return acroFormFields;
    }

    public void setAcroFormFields(List<PDField> acroFormFields)
    {
        this.acroFormFields = acroFormFields;
    }
    
    public ByteArrayInputStream templateAppearanceStream() throws IOException, COSVisitorException
    {
        COSDocument visualSignature = getVisualSignature();

        ByteArrayOutputStream memoryOut = new ByteArrayOutputStream();
        COSWriter memoryWriter = new COSWriter(memoryOut);
        memoryWriter.write(visualSignature);

        ByteArrayInputStream input = new ByteArrayInputStream(memoryOut.toByteArray());

        getTemplate().close();

        return input;
    }

    public COSDictionary getWidgetDictionary()
    {
        return widgetDictionary;
    }

    public void setWidgetDictionary(COSDictionary widgetDictionary)
    {
        this.widgetDictionary = widgetDictionary;
    }

}
