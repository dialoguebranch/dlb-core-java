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

import nl.rrd.utils.exception.LineNumberParseException;
import nl.rrd.utils.expressions.EvaluationException;
import nl.rrd.utils.expressions.Value;
import com.dialoguebranch.execution.DLBVariable;
import com.dialoguebranch.execution.DLBVariableStore;
import com.dialoguebranch.model.DLBNodeBody;
import com.dialoguebranch.parser.DLBBodyToken;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DLBInputEmailCommand extends DLBInputCommand {
	private String variableName;

	public DLBInputEmailCommand(String variableName) {
		super(TYPE_EMAIL);
		this.variableName = variableName;
	}

	public DLBInputEmailCommand(DLBInputEmailCommand other) {
		super(other);
		this.variableName = other.variableName;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	@Override
	public Map<String, ?> getParameters() {
		Map<String,Object> result = new LinkedHashMap<>();
		result.put("variableName", variableName);
		return result;
	}

	@Override
	public String getStatementLog(DLBVariableStore varStore) {
		DLBVariable DLBVariable = varStore.getDLBVariable(variableName);
		Value value = new Value(DLBVariable.getValue());
		return value.toString();
	}

	@Override
	public void getReadVariableNames(Set<String> varNames) {
	}

	@Override
	public void getWriteVariableNames(Set<String> varNames) {
		varNames.add(variableName);
	}

	@Override
	public void executeBodyCommand(Map<String, Object> variables,
			DLBNodeBody processedBody) throws EvaluationException {
		processedBody.addSegment(new DLBNodeBody.CommandSegment(this));
	}

	@Override
	public DLBInputEmailCommand clone() {
		return new DLBInputEmailCommand(this);
	}

	@Override
	public String toString() {
		String result = toStringStart();
		result += " value=\"$" + variableName + "\">>";
		return result;
	}

	public static DLBInputCommand parse(DLBBodyToken cmdStartToken,
										Map<String, DLBBodyToken> attrs) throws LineNumberParseException {
		String variableName = readVariableAttr("value", attrs, cmdStartToken,
				true);
		return new DLBInputEmailCommand(variableName);
	}
}
