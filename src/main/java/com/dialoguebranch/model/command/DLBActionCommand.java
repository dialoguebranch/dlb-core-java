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

package com.dialoguebranch.model.command;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dialoguebranch.model.DLBNodeBody;
import com.dialoguebranch.model.DLBReply;
import com.dialoguebranch.model.nodepointer.DLBNodePointer;
import com.dialoguebranch.parser.DLBBodyToken;
import com.dialoguebranch.parser.DLBNodeState;
import nl.rrd.utils.CurrentIterator;
import nl.rrd.utils.exception.LineNumberParseException;
import nl.rrd.utils.expressions.EvaluationException;
import com.dialoguebranch.model.DLBVariableString;

/**
 * This command models the &lt;&lt;action ...&gt;&gt; command in DialogueBranch. It
 * specifies an action that should be performed along with a statement. It can
 * be part of a {@link DLBNodeBody} (along with an agent
 * statement) or a {@link DLBReply} (to be performed when the user
 * chooses the reply).
 *
 * Three different action commands are supported:
 * <ul>
 *     <li>image</li>
 *     <li>video</li>
 *     <li>link</li>
 *     <li>generic</li>
 * </ul>
 * 
 * @author Dennis Hofs (RRD)
 */
public class DLBActionCommand extends DLBAttributesCommand {
	public static final String TYPE_IMAGE = "image";
	public static final String TYPE_VIDEO = "video";
	public static final String TYPE_LINK = "link";
	public static final String TYPE_GENERIC = "generic";
	
	private static final List<String> VALID_TYPES = Arrays.asList(
			TYPE_IMAGE, TYPE_VIDEO, TYPE_LINK, TYPE_GENERIC);
	
	private String type;
	private DLBVariableString value;
	private Map<String, DLBVariableString> parameters = new LinkedHashMap<>();

	/**
	 * Creates an instance of a {@link DLBActionCommand} with given {@code type} and
	 * {@code value}.
	 * @param type the type of this {@link DLBActionCommand} as a String, which should be
	 *                one of "image", "video", or "generic".
	 * @param value the value of this command
	 */
	public DLBActionCommand(String type, DLBVariableString value) {
		this.type = type;
		this.value = value;
	}

	public DLBActionCommand(DLBActionCommand other) {
		this.type = other.type;
		this.value = new DLBVariableString(other.value);
		for (String key : other.parameters.keySet()) {
			this.parameters.put(key, new DLBVariableString(
					other.parameters.get(key)));
		}
	}

	/**
	 * Returns the type of this {@link DLBActionCommand} as a String.
	 * @return the type of this {@link DLBActionCommand} as a String.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type of this {@link DLBActionCommand}, which should be one of "image",
	 * "video", or "generic".
	 * @param type the type of this {@link DLBActionCommand}.
	 */
	public void setType(String type) {
		this.type = type;
	}

	public DLBVariableString getValue() {
		return value;
	}

	public void setValue(DLBVariableString value) {
		this.value = value;
	}

	public Map<String, DLBVariableString> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, DLBVariableString> parameters) {
		this.parameters = parameters;
	}
	
	public void addParameter(String name, DLBVariableString value) {
		parameters.put(name, value);
	}
	
	@Override
	public DLBReply findReplyById(int replyId) {
		return null;
	}

	@Override
	public void getReadVariableNames(Set<String> varNames) {
		value.getReadVariableNames(varNames);
		for (DLBVariableString paramVals : parameters.values()) {
			paramVals.getReadVariableNames(varNames);
		}
	}

	@Override
	public void getWriteVariableNames(Set<String> varNames) {
	}

	@Override
	public void getNodePointers(Set<DLBNodePointer> pointers) {
	}

	@Override
	public void executeBodyCommand(Map<String, Object> variables,
			DLBNodeBody processedBody) throws EvaluationException {
		DLBActionCommand processedCommand = executeReplyCommand(variables);
		processedBody.addSegment(new DLBNodeBody.CommandSegment(
				processedCommand));
	}

	public DLBActionCommand executeReplyCommand(Map<String,Object> variables)
			throws EvaluationException {
		DLBActionCommand processedCommand = new DLBActionCommand(type,
				value.execute(variables));
		for (String param : parameters.keySet()) {
			DLBVariableString value = parameters.get(param);
			processedCommand.addParameter(param, value.execute(variables));
		}
		return processedCommand;
	}

	@Override
	public String toString() {
		char[] escapes = new char[] { '"' };
		StringBuilder result = new StringBuilder(
				"<<action type=\"" + type +
				"\" value=\"" + value.toString(escapes) + "\"");
		for (String key : parameters.keySet()) {
			result.append(" " + key + "=\"" +
					parameters.get(key).toString(escapes) + "\"");
		}
		result.append(">>");
		return result.toString();
	}
	
	public static DLBActionCommand parse(DLBBodyToken cmdStartToken,
										 CurrentIterator<DLBBodyToken> tokens, DLBNodeState nodeState)
			throws LineNumberParseException {
		Map<String, DLBBodyToken> attrs = parseAttributesCommand(cmdStartToken,
				tokens);
		String type = readPlainTextAttr("type", attrs, cmdStartToken, true);
		DLBBodyToken token = attrs.get("type");
		if (!VALID_TYPES.contains(type)) {
			throw new LineNumberParseException(
					"Invalid value for attribute \"type\": " + type,
					token.getLineNum(), token.getColNum());
		}
		attrs.remove("type");
		DLBVariableString value = readAttr("value", attrs, cmdStartToken,
				true);
		attrs.remove("value");
		DLBActionCommand command = new DLBActionCommand(type, value);
		for (String attr : attrs.keySet()) {
			token = attrs.get(attr);
			command.addParameter(attr, (DLBVariableString)token.getValue());
		}
		return command;
	}

	@Override
	public DLBActionCommand clone() {
		return new DLBActionCommand(this);
	}
}
