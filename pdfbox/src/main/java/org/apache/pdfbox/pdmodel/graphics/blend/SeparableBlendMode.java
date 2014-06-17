package org.apache.pdfbox.pdmodel.graphics.blend;

/**
 * Separable blend mode (support blendChannel)
 *
 * @author Kühn & Weyh Software, GmbH
 */
public abstract class SeparableBlendMode extends BlendMode
{
    SeparableBlendMode()
    {
    }

    public abstract float blendChannel(float srcValue, float dstValue);
}
