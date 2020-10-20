package org.apache.pdfbox.pdmodel.graphics.state;
import org.junit.Assert;
import org.junit.Test;

public class TestRenderingMode {
    @Test
    public void testIsFill() {
        RenderingMode fillMode = RenderingMode.FILL;
        Assert.assertEquals(true, fillMode.isFill());
    }
}
