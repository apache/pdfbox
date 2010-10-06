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
package org.apache.pdfbox.pdfviewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.Image;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.PDShading;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.text.PDTextState;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFStreamEngine;
import org.apache.pdfbox.util.ResourceLoader;
import org.apache.pdfbox.util.TextPosition;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSDictionary;


/**
 * This will paint a page in a PDF document to a graphics context.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.22 $
 */
public class PageDrawer extends PDFStreamEngine
{

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(PageDrawer.class);

    private Graphics2D graphics;
    protected Dimension pageSize;
    protected PDPage page;

    private GeneralPath linePath = new GeneralPath();

    /**
     * Default constructor, loads properties from file.
     *
     * @throws IOException If there is an error loading properties from the file.
     */
    public PageDrawer() throws IOException
    {
        super( ResourceLoader.loadProperties(
                "org/apache/pdfbox/resources/PageDrawer.properties", true ) );
    }

    /**
     * This will draw the page to the requested context.
     *
     * @param g The graphics context to draw onto.
     * @param p The page to draw.
     * @param pageDimension The size of the page to draw.
     *
     * @throws IOException If there is an IO error while drawing the page.
     */
    public void drawPage( Graphics g, PDPage p, Dimension pageDimension ) throws IOException
    {
        graphics = (Graphics2D)g;
        page = p;
        pageSize = pageDimension;
        graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        graphics.setRenderingHint( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON );
        // Only if there is some content, we have to process it. 
        // Otherwise we are done here and we will produce an empty page
        if ( page.getContents() != null) 
        {
            PDResources resources = page.findResources();
            processStream( page, resources, page.getContents().getStream() );
        }
        List annotations = page.getAnnotations();
        for( int i=0; i<annotations.size(); i++ )
        {
            PDAnnotation annot = (PDAnnotation)annotations.get( i );
            PDRectangle rect = annot.getRectangle();
            String appearanceName = annot.getAppearanceStream();
            PDAppearanceDictionary appearDictionary = annot.getAppearance();
            if( appearDictionary != null )
            {
                if( appearanceName == null )
                {
                    appearanceName = "default";
                }
                Map appearanceMap = appearDictionary.getNormalAppearance();
                if (appearanceMap != null) { 
                    PDAppearanceStream appearance = 
                        (PDAppearanceStream)appearanceMap.get( appearanceName ); 
                    if( appearance != null ) 
                    { 
                        g.translate( (int)rect.getLowerLeftX(), (int)-rect.getLowerLeftY() ); 
                        processSubStream( page, appearance.getResources(), appearance.getStream() ); 
                        g.translate( (int)-rect.getLowerLeftX(), (int)+rect.getLowerLeftY() ); 
                    }
                }
            }
        }

    }

    /**
     * You should override this method if you want to perform an action when a
     * text is being processed.
     *
     * @param text The text to process
     */
    protected void processTextPosition( TextPosition text )
    {
        try
        {
            switch(this.getGraphicsState().getTextState().getRenderingMode()) {
                case PDTextState.RENDERING_MODE_FILL_TEXT:
                    graphics.setColor( this.getGraphicsState().getNonStrokingColor().getJavaColor() );
                    break;
                case PDTextState.RENDERING_MODE_STROKE_TEXT:
                    graphics.setColor( this.getGraphicsState().getStrokingColor().getJavaColor() );
                    break;
                case PDTextState.RENDERING_MODE_NEITHER_FILL_NOR_STROKE_TEXT:
                    //basic support for text rendering mode "invisible"
                    Color nsc = this.getGraphicsState().getStrokingColor().getJavaColor();
                    float[] components = {Color.black.getRed(),Color.black.getGreen(),Color.black.getBlue()};
                    Color  c = new Color(nsc.getColorSpace(),components,0f);
                    graphics.setColor(c);
                    break;
                default:
                    // TODO : need to implement....
                    log.debug("Unsupported RenderingMode "
                            + this.getGraphicsState().getTextState().getRenderingMode()
                            + " in PageDrawer.processTextPosition()."
                            + " Using RenderingMode "
                            + PDTextState.RENDERING_MODE_FILL_TEXT
                            + " instead");
                    graphics.setColor( this.getGraphicsState().getNonStrokingColor().getJavaColor() );
            }

            PDFont font = text.getFont();
            Matrix textPos = text.getTextPos().copy();
            float x = textPos.getXPosition();
            // the 0,0-reference has to be moved from the lower left (PDF) to the upper left (AWT-graphics)
            float y = pageSize.height - textPos.getYPosition();
            // Set translation to 0,0. We only need the scaling and shearing
            textPos.setValue(2, 0, 0);
            textPos.setValue(2, 1, 0);
            // because of the moved 0,0-reference, we have to shear in the opposite direction
            textPos.setValue(0, 1, (-1)*textPos.getValue(0, 1));
            textPos.setValue(1, 0, (-1)*textPos.getValue(1, 0));
            AffineTransform at = textPos.createAffineTransform();
            PDMatrix fontMatrix = font.getFontMatrix();
            at.scale(fontMatrix.getValue(0, 0) * 1000f, fontMatrix.getValue(1, 0) * 1000f);
            graphics.setClip(getGraphicsState().getCurrentClippingPath());
            // the fontSize is no longer needed as it is already part of the transformation
            // we should remove it from the parameter list in the long run
            font.drawString( text.getCharacter(), graphics, 1, at, x, y );
        }
        catch( IOException io )
        {
            io.printStackTrace();
        }
    }

    /**
     * Get the graphics that we are currently drawing on.
     *
     * @return The graphics we are drawing on.
     */
    public Graphics2D getGraphics()
    {
        return graphics;
    }

    /**
     * Get the page that is currently being drawn.
     *
     * @return The page that is being drawn.
     */
    public PDPage getPage()
    {
        return page;
    }

    /**
     * Get the size of the page that is currently being drawn.
     *
     * @return The size of the page that is being drawn.
     */
    public Dimension getPageSize()
    {
        return pageSize;
    }

    /**
     * Fix the y coordinate.
     *
     * @param y The y coordinate.
     * @return The updated y coordinate.
     */
    public double fixY( double y )
    {
        return pageSize.getHeight() - y;
    }

    /**
     * Get the current line path to be drawn.
     *
     * @return The current line path to be drawn.
     */
    public GeneralPath getLinePath()
    {
        return linePath;
    }

    /**
     * Set the line path to draw.
     *
     * @param newLinePath Set the line path to draw.
     */
    public void setLinePath(GeneralPath newLinePath)
    {
        if (linePath == null || linePath.getCurrentPoint() == null)
        {
            linePath = newLinePath;
        }
        else
        {
            linePath.append(newLinePath, false);
        }
    }


    /**
     * Fill the path.
     *
     * @param windingRule The winding rule this path will use.
     * 
     * @throws IOException If there is an IO error while filling the path.
     */
    public void fillPath(int windingRule) throws IOException
    {
        graphics.setColor( getGraphicsState().getNonStrokingColor().getJavaColor() );
        getLinePath().setWindingRule(windingRule);
        graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
        graphics.setClip(getGraphicsState().getCurrentClippingPath());
        graphics.fill( getLinePath() );
        getLinePath().reset();
    }


    /**
     * This will set the current stroke.
     *
     * @param newStroke The current stroke.
     * 
     */
    public void setStroke(BasicStroke newStroke)
    {
        getGraphics().setStroke( newStroke );
    }

    /**
     * This will return the current stroke.
     *
     * @return The current stroke.
     * 
     */
    public BasicStroke getStroke()
    {
        return (BasicStroke)getGraphics().getStroke();
    }
    
    /**
     * Stroke the path.
     *
     * @throws IOException If there is an IO error while stroking the path.
     */
    public void strokePath() throws IOException
    {
        graphics.setColor( getGraphicsState().getStrokingColor().getJavaColor() ); 
        graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
        graphics.setClip(getGraphicsState().getCurrentClippingPath());
        GeneralPath path = getLinePath();
        graphics.draw( path );
        path.reset();
    }

    /**
     * Called when the color changed.
     * @param bStroking true for the stroking color, false for the non-stroking color
     * @throws IOException if an I/O error occurs
     */
    @Deprecated
    public void colorChanged(boolean bStroking) throws IOException
    {
        //logger().info("changing " + (bStroking ? "" : "non") + "stroking color");
    }

    //This code generalizes the code Jim Lynch wrote for AppendRectangleToPath
    /**
     * use the current transformation matrix to transform a single point.
     * @param x x-coordinate of the point to be transform
     * @param y y-coordinate of the point to be transform
     * @return the transformed coordinates as Point2D.Double
     */
    public java.awt.geom.Point2D.Double transformedPoint(double x, double y)
    {
        double[] position = {x,y}; 
        getGraphicsState().getCurrentTransformationMatrix().createAffineTransform().transform(
                position, 0, position, 0, 1);
        position[1] = fixY(position[1]);
        return new Point2D.Double(position[0],position[1]);
    }

    /**
     * Set the clipping Path.
     *
     * @param windingRule The winding rule this path will use.
     * 
     */
    public void setClippingPath(int windingRule)
    {
        PDGraphicsState graphicsState = getGraphicsState();
        GeneralPath clippingPath = (GeneralPath)getLinePath().clone();
        clippingPath.setWindingRule(windingRule);
        // If there is already set a clipping path, we have to intersect the new with the existing one
        if (graphicsState.getCurrentClippingPath() != null) 
        {
            Area currentArea = new Area(getGraphicsState().getCurrentClippingPath());
            Area newArea = new Area(clippingPath);
            currentArea.intersect(newArea);
            graphicsState.setCurrentClippingPath(currentArea);
        }
        else 
        {
            graphicsState.setCurrentClippingPath(clippingPath);
        }
        getLinePath().reset();
    }
    
    /**
     * Draw the AWT image. Called by Invoke.
     * Moved into PageDrawer so that Invoke doesn't have to reach in here for Graphics as that breaks extensibility.
     *
     * @param awtImage The image to draw.
     * @param at The transformation to use when drawing.
     * 
     */
    public void drawImage(Image awtImage, AffineTransform at){        
                graphics.setClip(getGraphicsState().getCurrentClippingPath());
                graphics.drawImage( awtImage, at, null );
    }
    
    /**
     * Fill with Shading.  Called by SHFill operator.
     *
     * @param ShadingName  The name of the Shading Dictionary to use for this fill instruction.
     *
     * @throws IOException If there is an IO error while shade-filling the path/clipping area.
     */
    public void SHFill(COSName ShadingName) throws IOException
    {
        PDShading Shading =FindShadingDictionary(ShadingName);
        log.info("Shading = " + Shading.toString());
        
        switch (Shading.getShadingType()){
            case 1:
                SHFill_Function(Shading);
            break;
            case 2:
                SHFill_Axial(Shading);
                break;
            case 3:
                SHFill_Radial(Shading);
                break;
            case 4:
                SHFill_FreeGourad(Shading);
                break;
            case 5:
                SHFill_LatticeGourad(Shading);
                break;
            case 6:
                SHFill_CoonsPatch(Shading);
                break;
            case 7:
                SHFill_TensorPatch(Shading);
                break;
            
            default:
                throw new IOException("Invalid ShadingType " + Shading.getShadingType() + " for Shading " + ShadingName);
        }
    }
    
    /**
     * Find the appropriate Shading Dictionary. This is its own private function as it is really not appropriate to override when deriving from PageDrawer.
     *
     * @param ShadingName  The name of the Shading Dictionary to use for this fill instruction.
     *
     * @returns The PDShading object
     * @throws IOException If there is an IO error while attempting to find the appropriate PDShading object.
     */
    private PDShading FindShadingDictionary(COSName ShadingName) throws IOException
    {
        
        PDResources resources = (PDResources)page.getResources();
                
        COSDictionary AllShadings = (COSDictionary)(resources.getCOSDictionary().getDictionaryObject(COSName.SHADING));
        
        PDShading Shading = new PDShading(ShadingName, (COSDictionary)(AllShadings.getDictionaryObject(ShadingName)));
        
        return Shading;
        
    }
    
    /**
     * Fill with a Function-based gradient / shading.  
     * If extending the class, override this and its siblings, not the public SHFill method.
     *
     * @param Shading  The Shading Dictionary to use for this fill instruction.
     *
     * @throws IOException If there is an IO error while shade-filling the path/clipping area.
     */
    protected void SHFill_Function(PDShading Shading) throws IOException
    {
        throw new IOException("Not Implemented");
    }

    /**
     * Fill with an Axial Shading.  
     * If extending the class, override this and its siblings, not the public SHFill method.
     *
     * @param Shading  The Shading Dictionary to use for this fill instruction.
     *
     * @throws IOException If there is an IO error while shade-filling the path/clipping area.
     */
    protected void SHFill_Axial(PDShading Shading) throws IOException
    {
        throw new IOException("Not Implemented");
        
    }

    /**
     * Fill with a Radial gradient / shading.  
     * If extending the class, override this and its siblings, not the public SHFill method.
     *
     * @param Shading  The Shading Dictionary to use for this fill instruction.
     *
     * @throws IOException If there is an IO error while shade-filling the path/clipping area.
     */
    protected void SHFill_Radial(PDShading Shading) throws IOException
    {
        throw new IOException("Not Implemented");
    }
    
    /**
     * Fill with a Free-form Gourad-shaded triangle mesh.
     * If extending the class, override this and its siblings, not the public SHFill method.
     *
     * @param Shading  The Shading Dictionary to use for this fill instruction.
     *
     * @throws IOException If there is an IO error while shade-filling the path/clipping area.
     */
    protected void SHFill_FreeGourad(PDShading Shading) throws IOException
    {
        throw new IOException("Not Implemented");
    }
    
    /**
     * Fill with a Lattice-form Gourad-shaded triangle mesh.
     * If extending the class, override this and its siblings, not the public SHFill method.
     *
     * @param Shading  The Shading Dictionary to use for this fill instruction.
     *
     * @throws IOException If there is an IO error while shade-filling the path/clipping area.
     */
    protected void SHFill_LatticeGourad(PDShading Shading) throws IOException
    {
        throw new IOException("Not Implemented");
    }
    
    /**
     * Fill with a Coons patch mesh
     * If extending the class, override this and its siblings, not the public SHFill method.
     *
     * @param Shading  The Shading Dictionary to use for this fill instruction.
     *
     * @throws IOException If there is an IO error while shade-filling the path/clipping area.
     */
    protected void SHFill_CoonsPatch(PDShading Shading) throws IOException
    {
        throw new IOException("Not Implemented");
    }
    
    /**
     * Fill with a Tensor-product patch mesh.
     * If extending the class, override this and its siblings, not the public SHFill method.
     *
     * @param Shading  The Shading Dictionary to use for this fill instruction.
     *
     * @throws IOException If there is an IO error while shade-filling the path/clipping area.
     */
    protected void SHFill_TensorPatch(PDShading Shading) throws IOException
    {
        throw new IOException("Not Implemented");
    }
}
