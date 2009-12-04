/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.bxx.bxd;

import sf.blacksun.util.struct.IIntStack;

public interface IBxdWalker extends IBxdScanner {

	/** @return The tag stack. */
	IIntStack tagStack();

	/** @return Target for the last doctype, declaration or pi. */
	CharSequence target();

	/** @return true if the given tag is a builtin value tag. */
	boolean isValue(int tag);

	/**
	 * @return value returned by the last next(), 0 if next() is never called.
	 */
	int current();

	/**
	 * Move to next node, skip over any attributes.
	 * Note that next() expose EndAttr and EndTag for state tracking in client application.
	 * @return	tagId, which may be an element (including doctype, declaration and pi),
	 * a value node (comment, cdata or text) or EndAttr or EndTag;
	 * @throws BxdException
	 */
	int next() throws BxdException;

	/**
	 * Move to next element, skipping over any attributes or value nodes of current element,
	 * An element is either doctype, declaration, pi or a user node.
	 * @return The tagId of the next element. TagTrailer if there are no more elements.
	 * @throws BxdException
	 */
	int nextElement() throws BxdException;

	/**
	 * Read header.
	 * @return The attributes.
	 * @throws BxdException
	 */
	IBxdHeader readHeader() throws BxdException;

	/**
	 * Read trailer.
	 * @return The attributes.
	 * @throws BxdException
	 */
	IBxdTrailer readTrailer() throws BxdException;

	/**
	 * Read all attributes of the current node.
	 * @return A transient IBxdAttributes object, that would be invalidated on next call.
	 * @throws BxdException
	 */
	IBxdAttributes readAttributes() throws BxdException;

	/**
	 * Read the next attributes of the current node.
	 * @return A transient IBxdAttribute object that would be invalidated on next call,
	 * 	null if no more attribute.
	 * @throws BxdException
	 */
	IBxdAttribute readAttribute() throws BxdException;

	/**
	 * Read the value of the next CDATA or Text node, skip over StringDef, BlobDef and Comments.
	 * @return A transient IBxdValue object that would be invalidated on next call,
	 * 	null if no more value.
	 * @throws BxdException
	 */
	IBxdValue readValue() throws BxdException;
}
