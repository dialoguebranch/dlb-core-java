/*
 *
 *                Copyright (c) 2023-2024 Fruit Tree Labs (www.fruittreelabs.com)
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

package com.dialoguebranch.model.nodepointer;

import com.dialoguebranch.model.Node;

/**
 * An abstract representation of a pointer to a next node, that can either be a {@link
 * NodePointerInternal} that links to a node withing the same dialogue, or a {@link
 * NodePointerExternal} linking to a node in another Dialogue Branch script.
 * 
 * @author Tessa Beinema (Roessingh Research and Development)
 * @author Harm op den Akker (Fruit Tree Labs)
 *
 * @see NodePointerInternal
 * @see NodePointerExternal
 */
public abstract class NodePointer implements Cloneable {
	
	protected String nodeId;

	// --------------------------------------------------------
	// -------------------- Constructor(s) --------------------
	// --------------------------------------------------------
	
	/**
	 * Creates an instance of a {@link NodePointer} with given {@code nodeId}.
	 *
	 * @param nodeId the unique identifier of the {@link Node} that this NodePointer refers to.
	 */
	public NodePointer(String nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * Creates an instance of a {@link NodePointer}, instantiated with the information of the {@code
	 * other} {@link NodePointer}.
	 *
	 * @param other the other {@link NodePointer} from which to instantiate this.
	 */
	public NodePointer(NodePointer other) {
		this.nodeId = other.nodeId;
	}

	// -----------------------------------------------------------
	// -------------------- Getters & Setters --------------------
	// -----------------------------------------------------------

	/**
	 * Returns the identifier of the {@link Node} that this pointer refers to.
	 *
	 * @return the identifier of the {@link Node} that this pointer refers to.
	 */
	public String getNodeId() {
		return this.nodeId;
	}
	
	/**
	 * Sets the identifier of the {@link Node} that this pointer refers to.
	 *
	 * @param nodeId the identifier of the {@link Node} that this pointer refers to.
	 */
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	// -------------------------------------------------------
	// -------------------- Other Methods --------------------
	// -------------------------------------------------------

	@Override
	public int hashCode() {
		return nodeId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodePointer other = (NodePointer)obj;
		if (!nodeId.equals(other.nodeId))
			return false;
		return true;
	}

	@Override
	public abstract NodePointer clone();

}
