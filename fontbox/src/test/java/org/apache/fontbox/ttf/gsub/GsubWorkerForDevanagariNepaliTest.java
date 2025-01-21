package org.apache.fontbox.ttf.gsub;

import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.ttf.model.GsubData;
import org.apache.fontbox.ttf.model.ScriptFeature;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GsubWorkerForDevanagariNepaliTest {

    private static final String LOHIT_DEVANAGARI_TTF =
            "src/test/resources/ttf/Lohit-Devanagari.ttf";

    private CmapLookup cmapLookup;
    private GsubWorker gsubWorkerForDevanagariNepali;
    private GsubData gsubData;

    @BeforeEach
    public void init() throws IOException
    {
        try (TrueTypeFont ttf = new TTFParser().parse(new RandomAccessReadBufferedFile(LOHIT_DEVANAGARI_TTF)))
        {
            cmapLookup = ttf.getUnicodeCmapLookup();
            gsubData = ttf.getGsubData();
            gsubWorkerForDevanagariNepali = new GsubWorkerFactory().getGsubWorker(cmapLookup, ttf.getGsubData());
        }
    }

    @Test
    void testApplyTransforms_locl()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(642);

        // when
        List<Integer> result = gsubWorkerForDevanagariNepali.applyTransforms(getGlyphIds("प्त"));
        System.out.println("result: " + result);

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_nukt()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(400,396,393);

        // when
        List<Integer> result = gsubWorkerForDevanagariNepali.applyTransforms(getGlyphIds("य़ज़क़"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_akhn()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(520,521);

        // when
        List<Integer> result = gsubWorkerForDevanagariNepali.applyTransforms(getGlyphIds("क्षज्ञ"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }


    @Test
    void testApplyTransform_rephReposition() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // get the private method by name
        Method privateMethod = gsubWorkerForDevanagariNepali.getClass().getDeclaredMethod("adjustRephPosition",List.class );
        privateMethod.setAccessible(true);
        List<Integer> result = (List<Integer>) privateMethod.invoke(gsubWorkerForDevanagariNepali,Arrays.asList(353,382,342,382,352,380));
        // then
        assertEquals(Arrays.asList(342,382,352,380,353,382), result);
    }

    @Test
    void test_ApplyTransforms_rphf()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(538, 352, 673);

        // when
        List<Integer> result = gsubWorkerForDevanagariNepali.applyTransforms(getGlyphIds("र्थ्यो"));
        // र्थ्यो -> र ् थ ् य ो

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Disabled
    @Test
    void testApplyTransforms_rkrf()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(588,597,595,602);

        // when
        List<Integer> result = gsubWorkerForDevanagariNepali.applyTransforms(getGlyphIds("क्रब्रप्रह्र"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_blwf()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(602,336,516);

        // when
        List<Integer> result = gsubWorkerForDevanagariNepali.applyTransforms(getGlyphIds("ह्रट्र"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_half()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(558,557,546,537);

        // when
        List<Integer> result = gsubWorkerForDevanagariNepali.applyTransforms(getGlyphIds("ह्स्भ्त्"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_private_half_exception() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method privateMethod = gsubWorkerForDevanagariNepali.getClass().getDeclaredMethod("applyGsubFeature", ScriptFeature.class,List.class );
        privateMethod.setAccessible(true);
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(332,345,382);// छन्

        // when
        List<Integer> result = (List<Integer>) privateMethod.invoke(gsubWorkerForDevanagariNepali,gsubData.getFeature("half"),Arrays.asList(332,345,382));

        // then
        assertEquals(glyphsAfterGsub, result);
    }


    @Test
    void testApplyTransforms_vatu()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(517,593,601,665);

        // when
        List<Integer> result = gsubWorkerForDevanagariNepali.applyTransforms(getGlyphIds("श्रत्रस्रघ्र"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Disabled
    @Test
    void testApplyTransforms_cjct()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(638,688,636,640,639);

        // when
        List<Integer> result = gsubWorkerForDevanagariNepali.applyTransforms(getGlyphIds("द्मद्ध्र्यब्दद्वद्य"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_pres()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(603,605,617,652);

        // when
        List<Integer> result = gsubWorkerForDevanagariNepali.applyTransforms(getGlyphIds("शृक्तज्जह्ण"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Disabled
    @Test
    void testApplyTransforms_abvs()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(353,512,353,675,353,673);

        // when
        List<Integer> result = gsubWorkerForDevanagariNepali.applyTransforms(getGlyphIds("र्रैंरौंर्रो"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Disabled
    @Test
    void testApplyTransforms_blws()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(660,663,336,584,336,583);

        // when
        List<Integer> result = gsubWorkerForDevanagariNepali.applyTransforms(getGlyphIds("दृहृट्रूट्रु"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Disabled
    @Test
    void testApplyTransforms_psts()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(326,704,326,582,661,662);

        // when
        List<Integer> result = gsubWorkerForDevanagariNepali.applyTransforms(getGlyphIds("किंर्कींरुरू"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Test
    void testApplyTransforms_haln()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList(539);

        // when
        List<Integer> result = gsubWorkerForDevanagariNepali.applyTransforms(getGlyphIds("द्"));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    @Disabled
    void testApplyTransforms_calt()
    {
        // given
        List<Integer> glyphsAfterGsub = Arrays.asList();

        // when
        List<Integer> result = gsubWorkerForDevanagariNepali.applyTransforms(getGlyphIds(""));

        // then
        assertEquals(glyphsAfterGsub, result);
    }

    private List<Integer> getGlyphIds(String word)
    {
        List<Integer> originalGlyphIds = new ArrayList<>();

        for (char unicodeChar : word.toCharArray())
        {
            int glyphId = cmapLookup.getGlyphId(unicodeChar);
            assertTrue(glyphId > 0);
            originalGlyphIds.add(glyphId);
        }

        return originalGlyphIds;
    }
}