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
 * @author <a href="mailto:vakhtang.koroghlishvili@gmail.com"> vakhtang koroghlishvili (gogebashvili) </a>
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

    /**
     * Returns document page.
     * @return
     */
    public PDPage getPage()
    {
        return page;
    }

    /**
     * Sets document page
     * @param page
     */
    public void setPage(PDPage page)
    {
        this.page = page;
    }

    /**
     * Gets PDDocument template.
     * This represents a digital signature
     *  that can be attached to a document
     * @return
     */
    public PDDocument getTemplate()
    {
        return template;
    }

    /**
     * Wets PDDocument template.
     * This represents a digital signature
     * that can be attached to a document
     * @param template
     */
    public void setTemplate(PDDocument template)
    {
        this.template = template;
    }

    /**
     * Gets Acroform
     * @return
     */
    public PDAcroForm getAcroForm()
    {
        return acroForm;
    }

    /**
     * Sets Acroform
     * @param acroForm
     */
    public void setAcroForm(PDAcroForm acroForm)
    {
        this.acroForm = acroForm;
    }

    /**
     * Gets Signature field
     * @return
     */
    public PDSignatureField getSignatureField()
    {
        return signatureField;
    }

    /**
     * Sets signature field
     * @param signatureField
     */
    public void setSignatureField(PDSignatureField signatureField)
    {
        this.signatureField = signatureField;
    }

    /**
     * Gets PDSignature
     * @return
     */
    public PDSignature getPdSignature()
    {
        return pdSignature;
    }

    /**
     * Sets PDSignature
     * @param pdSignature
     */
    public void setPdSignature(PDSignature pdSignature)
    {
        this.pdSignature = pdSignature;
    }

    /**
     * Gets Dictionary of AcroForm. Thats <b> /DR </b>
     * entry in the AcroForm
     * @return
     */
    public COSDictionary getAcroFormDictionary()
    {
        return acroFormDictionary;
    }

    /**
     * Acroform have its Dictionary, so we here set
     * the Dictionary  which is in this location:
     * <b> AcroForm/DR <b>
     * @param acroFormDictionary
     */
    public void setAcroFormDictionary(COSDictionary acroFormDictionary)
    {
        this.acroFormDictionary = acroFormDictionary;
    }

    /**
     * Gets SignatureRectangle
     * @return
     */
    public PDRectangle getSingatureRectangle()
    {
        return singatureRectangle;
    }

    /**
     * Sets SignatureRectangle
     * @param singatureRectangle
     */
    public void setSignatureRectangle(PDRectangle singatureRectangle)
    {
        this.singatureRectangle = singatureRectangle;
    }

    /**
     * Gets AffineTransform
     * @return
     */
    public AffineTransform getAffineTransform()
    {
        return affineTransform;
    }

    /**
     * Sets AffineTransform
     * @param affineTransform
     */
    public void setAffineTransform(AffineTransform affineTransform)
    {
        this.affineTransform = affineTransform;
    }

    /**
     * Gets ProcSet Array
     * @return
     */
    public COSArray getProcSet()
    {
        return procSet;
    }

    /**
     * Sets ProcSet Array
     * @param procSet
     */
    public void setProcSet(COSArray procSet)
    {
        this.procSet = procSet;
    }

    /**
     * Gets the image of visible signature
     * @return
     */
    public PDJpeg getJpedImage()
    {
        return jpedImage;
    }

    /**
     * Sets the image of visible signature
     * @param jpedImage
     */
    public void setJpedImage(PDJpeg jpedImage)
    {
        this.jpedImage = jpedImage;
    }

    /**
     * Gets formatter rectangle
     * @return
     */
    public PDRectangle getFormaterRectangle()
    {
        return formaterRectangle;
    }

    /**
     * Sets formatter rectangle
     * @param formaterRectangle
     */
    public void setFormaterRectangle(PDRectangle formaterRectangle)
    {
        this.formaterRectangle = formaterRectangle;
    }

    /**
     * Sets HolderFormStream
     * @return
     */
    public PDStream getHolderFormStream()
    {
        return holderFormStream;
    }

    /**
     * Sets stream of holder form Stream 
     * @param holderFormStream
     */
    public void setHolderFormStream(PDStream holderFormStream)
    {
        this.holderFormStream = holderFormStream;
    }

    /**
     * Gets Holder form.
     * That form is here <b> AcroForm/DR/XObject/{holder form name} </b>
     * By default, name stars with FRM. We also add number of form
     * to the name.
     * @return
     */
    public PDXObjectForm getHolderForm()
    {
        return holderForm;
    }

    /**
     * In the structure, form will be contained by XObject in the <b>AcroForm/DR/ </b>
     * @param holderForm
     */
    public void setHolderForm(PDXObjectForm holderForm)
    {
        this.holderForm = holderForm;
    }

    /**
     * Gets Holder form resources
     * @return
     */
    public PDResources getHolderFormResources()
    {
        return holderFormResources;
    }

    /**
     * Sets holder form resources
     * @param holderFormResources
     */
    public void setHolderFormResources(PDResources holderFormResources)
    {
        this.holderFormResources = holderFormResources;
    }

    /**
     * Gets AppearanceDictionary
     * That is <b>/AP</b> entry the appearance dictionary.
     * @return
     */
    public PDAppearanceDictionary getAppearanceDictionary()
    {
        return appearanceDictionary;
    }

    /**
     * Sets AppearanceDictionary
     * That is <b>/AP</b> entry the appearance dictionary.
     * @param appearanceDictionary
     */
    public void setAppearanceDictionary(PDAppearanceDictionary appearanceDictionary)
    {
        this.appearanceDictionary = appearanceDictionary;
    }

    /**
     * Gets Inner form Stream.
     * @return
     */
    public PDStream getInnterFormStream()
    {
        return innterFormStream;
    }

    /**
     * Sets inner form stream
     * @param innterFormStream
     */
    public void setInnterFormStream(PDStream innterFormStream)
    {
        this.innterFormStream = innterFormStream;
    }

    /**
     * Gets inner form Resource
     * @return
     */
    public PDResources getInnerFormResources()
    {
        return innerFormResources;
    }

    /**
     * Sets inner form resource
     * @param innerFormResources
     */
    public void setInnerFormResources(PDResources innerFormResources)
    {
        this.innerFormResources = innerFormResources;
    }

    /**
     * Gets inner form that is in this location:
     * <b> AcroForm/DR/XObject/{holder form name}/Resources/XObject/{inner name} </b>
     * By default inner form name starts with "n". Then we add number of form
     * to the name.
     * @return
     */
    public PDXObjectForm getInnerForm()
    {
        return innerForm;
    }

    /**
     * sets inner form to this location:
     * <b> AcroForm/DR/XObject/{holder form name}/Resources/XObject/{destination} </b>
     * @param innerForm
     */
    public void setInnerForm(PDXObjectForm innerForm)
    {
        this.innerForm = innerForm;
    }

    /**
     * Gets name of inner form
     * @return
     */
    public String getInnerFormName()
    {
        return innerFormName;
    }

    /**
     * Sets inner form name
     * @param innerFormName
     */
    public void setInnerFormName(String innerFormName)
    {
        this.innerFormName = innerFormName;
    }

    /**
     * Gets Image form stream
     * @return
     */
    public PDStream getImageFormStream()
    {
        return imageFormStream;
    }

    /**
     * Sets image form stream
     * @param imageFormStream
     */
    public void setImageFormStream(PDStream imageFormStream)
    {
        this.imageFormStream = imageFormStream;
    }

    /**
     * Gets image form resources
     * @return
     */
    public PDResources getImageFormResources()
    {
        return imageFormResources;
    }

    /**
     * Sets image form resource
     * @param imageFormResources
     */
    public void setImageFormResources(PDResources imageFormResources)
    {
        this.imageFormResources = imageFormResources;
    }

    /**
     * Gets Image form. Image form is in this structure: 
     * <b>/AcroForm/DR/{holder form}/Resources/XObject /{inner form} </b>
     * /Resources/XObject/{image form name}.
     * @return
     */
    public PDXObjectForm getImageForm()
    {
        return imageForm;
    }

    /**
     * Sets Image form. Image form will be in this structure: 
     * <b>/AcroForm/DR/{holder form}/Resources/XObject /{inner form}
     * /Resources/XObject/{image form name}.</b> By default we start
     *  image form name with "img". Then we  add number of image 
     *  form to the form name.
     * Sets image form
     * @param imageForm
     */
    public void setImageForm(PDXObjectForm imageForm)
    {
        this.imageForm = imageForm;
    }

    /**
     * Gets image form name
     * @return
     */
    public String getImageFormName()
    {
        return imageFormName;
    }

    /**
     * Sets image form name
     * @param imageFormName
     */
    public void setImageFormName(String imageFormName)
    {
        this.imageFormName = imageFormName;
    }

    /**
     * Gets visible signature image name
     * @return
     */
    public String getImageName()
    {
        return imageName;
    }

    /**
     * Sets visible signature image name
     * @param imageName
     */
    public void setImageName(String imageName)
    {
        this.imageName = imageName;
    }

    /**
     * Gets COSDocument of visible Signature.
     * @see org.apache.pdfbox.cos.COSDocument
     * @return
     */
    public COSDocument getVisualSignature()
    {
        return visualSignature;
    }

    /**
     * 
     * Sets COSDocument of visible Signature.
     * @see org.apache.pdfbox.cos.COSDocument
     * @param visualSignature
     */
    public void setVisualSignature(COSDocument visualSignature)
    {
        this.visualSignature = visualSignature;
    }

    /**
     * Gets acroFormFields
     * @return
     */
    public List<PDField> getAcroFormFields()
    {
        return acroFormFields;
    }

    /**
     * Sets acroFormFields
     * @param acroFormFields
     */
    public void setAcroFormFields(List<PDField> acroFormFields)
    {
        this.acroFormFields = acroFormFields;
    }
    
   /**
    * Gets AP of the created template
    * @return
    * @throws IOException
    * @throws COSVisitorException
    */
    public ByteArrayInputStream getTemplateAppearanceStream() throws IOException, COSVisitorException
    {
        COSDocument visualSignature = getVisualSignature();
        ByteArrayOutputStream memoryOut = new ByteArrayOutputStream();
        COSWriter memoryWriter = new COSWriter(memoryOut);
        memoryWriter.write(visualSignature);

        ByteArrayInputStream input = new ByteArrayInputStream(memoryOut.toByteArray());

        getTemplate().close();

        return input;
    }

    /**
     * Gets Widget Dictionary.
     * {@link org.apache.pdfbox.pdmodel.interactive.form.PDField}
     * @see org.apache.pdfbox.pdmodel.interactive.form.PDField.getWidget() 
     * @return
     */
    public COSDictionary getWidgetDictionary()
    {
        return widgetDictionary;
    }

    /**
     * Sets Widget Dictionary.
     * {@link org.apache.pdfbox.pdmodel.interactive.form.PDField}
     * @see org.apache.pdfbox.pdmodel.interactive.form.PDField.getWidget() 
     * @param widgetDictionary
     */
    public void setWidgetDictionary(COSDictionary widgetDictionary)
    {
        this.widgetDictionary = widgetDictionary;
    }

}
