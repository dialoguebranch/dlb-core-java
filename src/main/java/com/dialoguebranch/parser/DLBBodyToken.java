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

package com.dialoguebranch.parser;

import nl.rrd.utils.CurrentIterator;
import nl.rrd.utils.DataFormatter;

import java.util.ArrayList;
import java.util.List;

public class DLBBodyToken {
	public enum Type {
		/**
		 * Value: text with escaped characters resolved
		 */
		TEXT,
	
		COMMAND_START,
		COMMAND_END,
		REPLY_START,
		REPLY_END,
		REPLY_SEPARATOR,
		
		/**
		 * Value: DLBVariableString
		 */
		QUOTED_STRING,
		
		/**
		 * Value: variable name
		 */
		VARIABLE
	}

	private Type type;
	private int lineNum;
	private int colNum;
	private String text;
	private Object value = null;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getLineNum() {
		return lineNum;
	}

	public void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}

	public int getColNum() {
		return colNum;
	}

	public void setColNum(int colNum) {
		this.colNum = colNum;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		DataFormatter formatter = new DataFormatter();
		return formatter.format(this, true, true);
	}

	public static void trimWhitespace(List<DLBBodyToken> tokens) {
		removeLeadingWhitespace(tokens);
		removeTrailingWhitespace(tokens);
	}
	
	public static void removeLeadingWhitespace(List<DLBBodyToken> tokens) {
		while (!tokens.isEmpty()) {
			DLBBodyToken token = tokens.get(0);
			if (token.getType() != DLBBodyToken.Type.TEXT)
				return;
			String text = (String)token.getValue();
			text = text.replaceAll("^\\s+", "");
			token.setValue(text);
			if (text.length() > 0)
				return;
			tokens.remove(0);
		}
	}
	
	public static void removeTrailingWhitespace(List<DLBBodyToken> tokens) {
		while (!tokens.isEmpty()) {
			DLBBodyToken token = tokens.get(tokens.size() - 1);
			if (token.getType() != DLBBodyToken.Type.TEXT)
				return;
			String text = (String)token.getValue();
			text = text.replaceAll("\\s+$", "");
			token.setValue(text);
			if (text.length() > 0)
				return;
			tokens.remove(tokens.size() - 1);
		}
	}

	/**
	 * Moves to the next token that is not a text token with only whitespace.
	 * 
	 * @param tokens the tokens
	 * @return the skipped tokens
	 */
	public static List<DLBBodyToken> skipWhitespace(
			CurrentIterator<DLBBodyToken> tokens) {
		List<DLBBodyToken> result = new ArrayList<>();
		while (tokens.getCurrent() != null) {
			DLBBodyToken token = tokens.getCurrent();
			if (token.getType() != DLBBodyToken.Type.TEXT)
				return result;
			String text = (String)token.getValue();
			if (!text.trim().isEmpty())
				return result;
			result.add(token);
			tokens.moveNext();
		}
		return result;
	}
}
