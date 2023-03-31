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

import com.dialoguebranch.model.DLBNodeBody;
import nl.rrd.utils.exception.LineNumberParseException;
import nl.rrd.utils.expressions.EvaluationException;
import nl.rrd.utils.expressions.Value;
import com.dialoguebranch.execution.DLBVariable;
import com.dialoguebranch.execution.DLBVariableStore;
import com.dialoguebranch.model.DLBVariableString;
import com.dialoguebranch.parser.DLBBodyToken;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DLBInputTimeCommand extends DLBInputCommand {
	public static final String TIME_NOW = "now";

	private String variableName;
	private int granularityMinutes = 1;
	private DLBVariableString startTime = null;
	private DLBVariableString minTime = null;
	private DLBVariableString maxTime = null;

	public DLBInputTimeCommand(String variableName) {
		super(TYPE_TIME);
		this.variableName = variableName;
	}

	public DLBInputTimeCommand(DLBInputTimeCommand other) {
		super(other);
		this.variableName = other.variableName;
		this.granularityMinutes = other.granularityMinutes;
		if (other.startTime != null)
			this.startTime = new DLBVariableString(other.startTime);
		if (other.minTime != null)
			this.minTime = new DLBVariableString(other.minTime);
		if (other.maxTime != null)
			this.maxTime = new DLBVariableString(other.maxTime);
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public int getGranularityMinutes() {
		return granularityMinutes;
	}

	public void setGranularityMinutes(int granularityMinutes) {
		this.granularityMinutes = granularityMinutes;
	}

	public DLBVariableString getStartTime() {
		return startTime;
	}

	public void setStartTime(DLBVariableString startTime) {
		this.startTime = startTime;
	}

	public DLBVariableString getMinTime() {
		return minTime;
	}

	public void setMinTime(DLBVariableString minTime) {
		this.minTime = minTime;
	}

	public DLBVariableString getMaxTime() {
		return maxTime;
	}

	public void setMaxTime(DLBVariableString maxTime) {
		this.maxTime = maxTime;
	}

	@Override
	public Map<String, ?> getParameters() {
		Map<String,Object> result = new LinkedHashMap<>();
		result.put("variableName", variableName);
		result.put("granularityMinutes", granularityMinutes);
		if (startTime != null)
			result.put("startTime", startTime.evaluate(null));
		if (minTime != null)
			result.put("minTime", minTime.evaluate(null));
		if (maxTime != null)
			result.put("maxTime", maxTime.evaluate(null));
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
		if (startTime != null)
			startTime.getReadVariableNames(varNames);
		if (minTime != null)
			minTime.getReadVariableNames(varNames);
		if (maxTime != null)
			maxTime.getReadVariableNames(varNames);
	}

	@Override
	public void getWriteVariableNames(Set<String> varNames) {
		varNames.add(variableName);
	}

	@Override
	public void executeBodyCommand(Map<String, Object> variables,
			DLBNodeBody processedBody) throws EvaluationException {
		DLBInputTimeCommand processedCmd = new DLBInputTimeCommand(
				variableName);
		processedCmd.granularityMinutes = granularityMinutes;
		if (startTime != null) {
			processedCmd.startTime = evaluateTime(startTime.evaluate(
					variables));
		}
		if (minTime != null) {
			processedCmd.minTime = evaluateTime(minTime.evaluate(variables));
		}
		if (maxTime != null) {
			processedCmd.maxTime = evaluateTime(maxTime.evaluate(variables));
		}
		processedBody.addSegment(new DLBNodeBody.CommandSegment(processedCmd));
	}

	@Override
	public DLBInputTimeCommand clone() {
		return new DLBInputTimeCommand(this);
	}

	private static DLBVariableString evaluateTime(String text)
			throws EvaluationException {
		if (text.equalsIgnoreCase(TIME_NOW))
			return new DLBVariableString(TIME_NOW);
		DateTimeFormatter parser = DateTimeFormatter.ISO_LOCAL_TIME;
		LocalTime time;
		try {
			time = parser.parse(text, LocalTime::from);
		} catch (DateTimeParseException ex) {
			throw new EvaluationException("Invalid local time value: " + text);
		}
		return new DLBVariableString(time.format(
				DateTimeFormatter.ofPattern("HH:mm")));
	}

	@Override
	public String toString() {
		char[] escapes = new char[] { '"' };
		StringBuilder builder = new StringBuilder(toStringStart());
		builder.append(" value=\"$" + variableName + "\"");
		builder.append(" granularityMinutes=\"" + granularityMinutes + "\"");
		if (startTime != null) {
			builder.append(" startTime=\"" + startTime.toString(escapes) +
					"\"");
		}
		if (minTime != null)
			builder.append(" minTime=\"" + minTime.toString(escapes) + "\"");
		if (maxTime != null)
			builder.append(" maxTime=\"" + maxTime.toString(escapes) + "\"");
		builder.append(">>");
		return builder.toString();
	}

	public static DLBInputTimeCommand parse(DLBBodyToken cmdStartToken,
											Map<String, DLBBodyToken> attrs) throws LineNumberParseException {
		String variableName = readVariableAttr("value", attrs, cmdStartToken,
				true);
		DLBInputTimeCommand command = new DLBInputTimeCommand(variableName);
		Integer granularityMinutes = readIntAttr("granularityMinutes", attrs,
				cmdStartToken, false, 1, null);
		if (granularityMinutes != null)
			command.granularityMinutes = granularityMinutes;
		command.startTime = readTimeAttribute("startTime", attrs,
				cmdStartToken);
		command.minTime = readTimeAttribute("minTime", attrs, cmdStartToken);
		command.maxTime = readTimeAttribute("maxTime", attrs, cmdStartToken);
		return command;
	}

	private static DLBVariableString readTimeAttribute(String attrName,
													   Map<String, DLBBodyToken> attrs, DLBBodyToken cmdStartToken)
			throws LineNumberParseException {
		DLBVariableString result = readAttr(attrName, attrs, cmdStartToken,
				false);
		if (result == null || !result.isPlainText())
			return result;
		DLBBodyToken token = attrs.get(attrName);
		String value = result.evaluate(null);
		try {
			return evaluateTime(value);
		} catch (EvaluationException ex) {
			throw new LineNumberParseException(String.format(
					"Invalid value for attribute \"%s\"", attrName) + ": " +
					ex.getMessage(), token.getLineNum(), token.getColNum(), ex);
		}
	}
}
