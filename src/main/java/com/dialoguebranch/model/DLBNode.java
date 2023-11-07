/*
 *
 *                   Copyright (c) 2023 Fruit Tree Labs (www.fruittreelabs.com)
 *
 *     This material is part of the DialogueBranch Platform, and is covered by the MIT License
 *      as outlined below. Based on original source code licensed under the following terms:
 *
 *                                            ----------
 *
 * Copyright 2019-2022 WOOL Foundation - Licensed under the MIT License:
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dialoguebranch.model;

/**
 * A {@link DLBNode} represents a single step in a {@link Dialogue} definition.
 *
 * @author Harm op den Akker (Roessingh Research and Development)
 */
public class DLBNode {
	
	private DLBNodeHeader header;
	private DLBNodeBody body;
	
	// ---------- Constructors:

	/**
	 * Creates an instance of an empty {@link DLBNode}.
	 */
	public DLBNode() { }

	/**
	 * Creates an instance of a {@link DLBNode} with the given {@code header}.
	 *
	 * @param header the {@link DLBNodeHeader} for this {@link DLBNode}
	 */
	public DLBNode(DLBNodeHeader header) {
		this.header = header;
	}

	/**
	 * Creates an instance of a {@link DLBNode} with the given {@code header} and {@code body}.
	 *
	 * @param header the {@link DLBNodeHeader} for this {@link DLBNode}
	 * @param body the {@link DLBNodeBody} for this {@link DLBNode}
	 */
	public DLBNode(DLBNodeHeader header, DLBNodeBody body) {
		this.header = header;
		this.body = body;
	}

	/**
	 * Creates an instance of a {@link DLBNode} instantiated with the contents from the given {@code other}
	 * {@link DLBNode}.
	 *
	 * @param other the {@link DLBNode} from which to copy its contents into this {@link DLBNode}
	 */
	public DLBNode(DLBNode other) {
		header = new DLBNodeHeader(other.header);
		body = new DLBNodeBody(other.body);
	}
	
	// ---------- Getters:

	/**
	 * Returns the {@link DLBNodeHeader} of this {@link DLBNode}.
	 *
	 * @return the {@link DLBNodeHeader} of this {@link DLBNode}.
	 */
	public DLBNodeHeader getHeader() {
		return header;
	}

	/**
	 * Returns the {@link DLBNodeBody} of this {@link DLBNode}.
	 *
	 * @return the {@link DLBNodeBody} of this {@link DLBNode}.
	 */
	public DLBNodeBody getBody() {
		return body;
	}

	// ---------- Setters;

	/**
	 * Sets the {@link DLBNodeHeader} for this {@link DLBNode}.
	 *
	 * @param header the {@link DLBNodeHeader} for this {@link DLBNode}.
	 */
	public void setHeader(DLBNodeHeader header) {
		this.header = header;
	}

	/**
	 * Sets the {@link DLBNodeBody} for this {@link DLBNode}.
	 *
	 * @param body the {@link DLBNodeBody} for this {@link DLBNode}.
	 */
	public void setBody(DLBNodeBody body) {
		this.body = body;
	}
	
	// ---------- Utility:
	
	/**
	 * Returns the title of this {@link DLBNode} as defined in its
	 * corresponding {@link DLBNodeHeader}. Returns the same as {@code
	 * this.getHeader().getTitle()} or {@code null} if no {@link DLBNodeHeader}
	 * has been set, or its title attribute is {@code null}.
	 *
	 * @return the title of this {@link DLBNode} as defined in its
	 * corresponding {@link DLBNodeHeader}.
	 */
	public String getTitle() {
		if(header != null)
			return header.getTitle();
		else return null;
	}
	
	@Override
	public String toString() {
		String newline = System.getProperty("line.separator");
		return header + newline + "---" + newline + body;
	}
}
