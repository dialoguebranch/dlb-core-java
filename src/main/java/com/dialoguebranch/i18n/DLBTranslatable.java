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

package com.dialoguebranch.i18n;

import com.dialoguebranch.model.DLBNodeBody;
import com.dialoguebranch.model.command.DLBInputCommand;

import java.util.List;

/**
 * This class models a translatable segment from a {@link DLBNodeBody}. It basically
 * consists of plain text, variables and &lt;&lt;input&gt;&gt; commands.
 *
 * <p>The class contains {@link DLBNodeBody.TextSegment TextSegment}s (with
 * plain text and variables) and {@link DLBNodeBody.CommandSegment
 * CommandSegment}s where the command is a {@link DLBInputCommand
 * DLBInputCommand}.</p>
 *
 * <p>Instances of this class can be obtained from {@link
 * DLBTranslatableExtractor DLBTranslatableExtractor} or {@link
 * DLBTranslationParser WDLBTranslationParser}.</p>
 *
 * @author Dennis Hofs (RRD)
 */
public class DLBTranslatable {
	private DLBNodeBody parent;
	private List<DLBNodeBody.Segment> segments;

	/**
	 * Constructs a new {@link DLBTranslatable}.
	 *
	 * @param parent the parent (used in {@link DLBTranslator})
	 * @param segments the segments
	 */
	public DLBTranslatable(DLBNodeBody parent,
						   List<DLBNodeBody.Segment> segments) {
		this.parent = parent;
		this.segments = segments;
	}

	/**
	 * Returns the parent (used in {@link DLBTranslator}).
	 *
	 * @return the parent (used in {@link DLBTranslator})
	 */
	public DLBNodeBody getParent() {
		return parent;
	}

	/**
	 * Returns the translatable segments.
	 *
	 * @return the translatable segments
	 */
	public List<DLBNodeBody.Segment> getSegments() {
		return segments;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj.getClass() != getClass())
			return false;
		DLBTranslatable other = (DLBTranslatable)obj;
		return toString().equals(other.toString());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (DLBNodeBody.Segment segment : segments) {
			builder.append(segment);
		}
		return builder.toString();
	}

	public String toExportFriendlyString() {
		StringBuilder builder = new StringBuilder();
		for (DLBNodeBody.Segment segment : segments) {
			String sourceString = segment.toString();
			builder.append(sourceString.trim());
		}
		return builder.toString();
	}
}
