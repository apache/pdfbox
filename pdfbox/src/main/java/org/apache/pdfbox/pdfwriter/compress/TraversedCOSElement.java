package org.apache.pdfbox.pdfwriter.compress;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;

import java.util.ArrayList;
import java.util.List;

/**
 * An instance of this class represents a traversed element of a COS tree. It allows to determine the position of a
 * {@link COSBase} in a hierarchical COS structure and provides the means to further traverse and evaluate it's
 * descendants.
 *
 * @author Christian Appl
 */
public class TraversedCOSElement {

	private TraversedCOSElement parent;
	private COSBase currentObject;
	private final List<TraversedCOSElement> traversedChildren = new ArrayList<TraversedCOSElement>();
	private boolean partOfStreamDictionary = false;
	private final List<COSBase> allObjects;

	/**
	 * Construct a fresh entrypoint for the traversal of a hierarchical COS structure, beginning with the given
	 * {@link COSBase}.
	 *
	 * @param currentObject The initial {@link COSBase}, with which the structure traversal shall begin.
	 */
	public TraversedCOSElement(COSBase currentObject) {
		this.currentObject = currentObject;
		this.allObjects = new ArrayList<COSBase>();
	}

	/**
	 * Construct a traversal node for the traversal of a hierarchical COS structure, located at the given
	 * {@link COSBase}, preceded by this given list of ancestors and contained in the given parent structure.
	 *
	 * @param allObjects    The list of nodes, that have been traversed to reach the current object.
	 * @param parent        The parent node, that does contain this node.
	 * @param currentObject The initial {@link COSBase}, with which the structure traversal shall begin.
	 */
	private TraversedCOSElement(
			List<COSBase> allObjects, TraversedCOSElement parent, COSBase currentObject
	) {
		this.parent = parent;
		this.currentObject = currentObject;
		this.allObjects = allObjects;
	}

	/**
	 * Construct a new traversal node for the given element and append it as a child to the current node.
	 *
	 * @param element The element, that shall be traversed.
	 * @return The resulting traversal node, that has been created.
	 */
	public TraversedCOSElement appendTraversedElement(COSBase element) {
		if (element == null) {
			return this;
		}
		allObjects.add(element);
		TraversedCOSElement traversedElement = new TraversedCOSElement(allObjects, this, element);
		traversedElement.setPartOfStreamDictionary(isPartOfStreamDictionary() || getCurrentBaseObject() instanceof COSStream);
		this.traversedChildren.add(traversedElement);
		return traversedElement;
	}

	/**
	 * Returns the current {@link COSBase} of this traversal node.
	 *
	 * @return The current {@link COSBase} of this traversal node.
	 */
	public COSBase getCurrentObject() {
		return currentObject;
	}

	/**
	 * Returns the actual current {@link COSBase} of this traversal node. Meaning: If the current traversal node
	 * contains a reference to a {@link COSObject}, it's actual base object will be returned instead.
	 *
	 * @return The actual current {@link COSBase} of this traversal node.
	 */
	public COSBase getCurrentBaseObject() {
		return currentObject instanceof COSObject ? ((COSObject) currentObject).getObject() : currentObject;
	}

	/**
	 * Set the current {@link COSBase} of this traversal node. (null values shall be ignored.)
	 *
	 * @param object The current {@link COSBase} of this traversal node.
	 */
	public void setCurrentObject(COSBase object) {
		if (object == null) {
			return;
		}
		this.allObjects.set(allObjects.indexOf(currentObject), object);
		this.currentObject = object;
	}

	/**
	 * Returns the parent node of the current traversal node.
	 *
	 * @return The parent node of the current traversal node.
	 */
	public TraversedCOSElement getParent() {
		return this.parent;
	}

	/**
	 * Returns all known traversable/traversed children contained by the current traversal node.
	 *
	 * @return All known traversable/traversed children contained by the current traversal node.
	 */
	public List<TraversedCOSElement> getTraversedChildren() {
		return traversedChildren;
	}

	public List<TraversedCOSElement> getTraversedElements() {
		List<TraversedCOSElement> ancestry = this.parent == null ?
				new ArrayList<TraversedCOSElement>() :
				this.parent.getTraversedElements();
		ancestry.add(this);
		return ancestry;
	}

	/**
	 * Returns true, if the given {@link COSBase} is equal to the object wrapped by this traversal node.
	 *
	 * @param object The object, that shall be compared.
	 * @return True, if the given {@link COSBase} is equal to the object wrapped by this traversal node.
	 */
	public boolean equals(COSBase object) {
		return this.currentObject == object;
	}

	/**
	 * Searches all known traversed child nodes of the current traversal node for the given {@link COSBase}.
	 *
	 * @param object The {@link COSBase}, that shall be found.
	 * @return The traversal node representing the searched {@link COSBase} or null, if such a node can not be found.
	 */
	public TraversedCOSElement findAtCurrentPosition(COSBase object) {
		for (TraversedCOSElement child : traversedChildren) {
			if (child.equals(object)) {
				return child;
			}
		}

		return null;
	}

	/**
	 * Returns a list of all objects, that have been traversed in the created traversal tree.
	 *
	 * @return A list of all objects, that have been traversed in the created traversal tree.
	 */
	public List<COSBase> getAllTraversedObjects() {
		return allObjects;
	}

	/**
	 * Returns true, if the given traversal node has been marked as a part of a {@link COSStream}.
	 *
	 * @return True, if the given traversal node has been marked as a part of a {@link COSStream}
	 */
	public boolean isPartOfStreamDictionary() {
		return partOfStreamDictionary;
	}

	/**
	 * Set to true, if the given traversal node shall be marked as a part of a {@link COSStream}.
	 *
	 * @param partOfStreamDictionary True, if the given traversal node shall be marked as a part of a {@link COSStream}
	 */
	public void setPartOfStreamDictionary(boolean partOfStreamDictionary) {
		this.partOfStreamDictionary = partOfStreamDictionary;
	}

}
