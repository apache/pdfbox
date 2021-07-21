package org.apache.pdfbox.pdfwriter;

import org.apache.pdfbox.cos.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

class COSWriterObjectStorageTest {

    private COSWriterObjectStorage storage;
    private COSObjectKey key1;
    private COSObjectKey key2;
    private COSObjectKey key3;

    private COSDocument document;

    private COSBase base1;

    private COSObject object1;
    private COSObject object2;
    private COSObject object3;

    @BeforeEach
    void setUp() {
        storage = new COSWriterObjectStorage();
        document = Mockito.mock(COSDocument.class);

        key1 = new COSObjectKey(1L, 1);
        key2 = new COSObjectKey(2L, 2);
        key3 = new COSObjectKey(3L, 3);

        object1 = Mockito.mock(COSObject.class);
        object2 = Mockito.mock(COSObject.class);
        object3 = Mockito.mock(COSObject.class);

        base1 = Mockito.mock(COSBase.class);
    }

    @Test
    void testObjectStorage_noDocumentSet_storesKeysAndObjectsBidirectionally() {
        storage.put(key1, object1);
        storage.put(key2, object2);

        assertAll(
            () -> assertEquals(key1, storage.getKey(object1)),
            () -> assertEquals(key2, storage.getKey(object2)),
            () -> assertEquals(object1, storage.getObject(key1)),
            () -> assertEquals(object2, storage.getObject(key2))
        );
    }

    @Test
    void testGetObject_documentWasSet_returnsActualObjectFromDocumentObjectPoolIfExistent() {
        storage.setDocument(document);
        storage.put(key1, object2);
        storage.put(key2, object2);

        given(object1.getObjectWithoutCaching()).willReturn(base1);
        given(document.getXrefTable()).willReturn(Collections.singletonMap(key1, null));
        given(document.getObjectFromPool(key1)).willReturn(object1);

        assertAll(
            // Key1 exists in document.
            () -> assertEquals(base1, storage.getObject(key1)),

            // Key2 exists in bidirectional map
            () -> assertEquals(object2, storage.getObject(key2)),

            // Key 3 does not exist.
            () -> assertNull(storage.getObject(key3))
        );
    }

    @Test
    void testGetKey_documentWasSet_returnsKeyStoredInMapOrDocument() {
        storage.setDocument(document);

        COSObject referencedObject1 = Mockito.mock(COSObject.class);
        storage.put(key1, referencedObject1);

        given(object1.getReferencedObject()).willReturn(referencedObject1);
        given(referencedObject1.getObjectNumber()).willReturn(1L);
        given(referencedObject1.getGenerationNumber()).willReturn(1);

        COSObject referencedObject2 = Mockito.mock(COSObject.class);
        given(object2.getReferencedObject()).willReturn(referencedObject2);
        given(referencedObject2.getObjectNumber()).willReturn(2L);
        given(referencedObject2.getGenerationNumber()).willReturn(2);

        given(document.getXrefTable()).willReturn(Collections.singletonMap(key2, null));
        given(document.getObjectFromPool(key2)).willReturn(referencedObject2);

        given(object3.getReferencedObject()).willReturn(Mockito.mock(COSObject.class));

        assertAll(
            // Referenced object exists in bidirectional map.
            () -> assertEquals(key1, storage.getKey(object1)),

            // Referenced object exists in document.
            () -> assertEquals(key2, storage.getKey(object2)),

            // Referenced was de-referenced, but neither exists in map nor document object pool.
            () -> assertNull(storage.getKey(object3))
        );
    }

    @Test
    void testPut_documentWasSet_putsReferencedObjectIfExistent() {
        storage.setDocument(document);
        given(object2.getReferencedObject()).willReturn(object3);

        storage.put(key1, object1);
        storage.put(key2, object2);

        assertAll(
            () -> assertEquals(object1, storage.getObject(key1)),
            () -> assertEquals(object3, storage.getObject(key2)),
            () -> assertEquals(key1, storage.getKey(object1)),
            () -> assertNull(storage.getKey(object2)),
            () -> assertEquals(key2, storage.getKey(object3))
        );
    }

    @Test
    void testConvertToActual() {
        given(object1.getObjectWithoutCaching()).willReturn(base1);

        assertAll(
            () -> assertEquals(base1, storage.convertToActual(object1)),
            () -> assertEquals(base1, storage.convertToActual(base1))
        );
    }

    @Test
    void testGetObject_throwExceptionForNullKey() {
        assertThrows(IllegalArgumentException.class, () -> storage.getObject(null));
    }

    @Test
    void testGetKey_throwExceptionForNullObject() {
        assertThrows(IllegalArgumentException.class, () -> storage.getKey(null));
    }

    @Test
    void testPut_throwExceptionForNullObjectOrKey() {
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> storage.put(key1,null)),
            () -> assertThrows(IllegalArgumentException.class, () -> storage.put(null,object1)),
            () -> assertThrows(IllegalArgumentException.class, () -> storage.put(null,null))
        );
    }
}