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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dialoguebranch.model.DLBNodeBody;
import com.dialoguebranch.model.DLBReply;
import com.dialoguebranch.model.nodepointer.DLBNodePointer;
import com.dialoguebranch.model.nodepointer.DLBNodePointerExternal;
import com.dialoguebranch.model.nodepointer.DLBNodePointerInternal;
import nl.rrd.utils.CurrentIterator;
import nl.rrd.utils.exception.LineNumberParseException;
import nl.rrd.utils.exception.ParseException;

public class DLBReplyParser {
	private DLBNodeState nodeState;
	
	private ReplySection statementSection;
	private ReplySection nodePointerSection;
	private ReplySection commandSection;
	
	public DLBReplyParser(DLBNodeState nodeState) {
		this.nodeState = nodeState;
	}
	
	public DLBReply parse(CurrentIterator<DLBBodyToken> tokens)
			throws LineNumberParseException {
		readSections(tokens);
		DLBNodeBody statement = parseStatement();
		DLBNodePointer nodePointer = parseNodePointer();
		DLBReply reply = new DLBReply(nodeState.createNextReplyId(),
				statement, nodePointer);
		if (commandSection != null)
			parseCommands(reply);
		return reply;
	}
	
	private void readSections(CurrentIterator<DLBBodyToken> tokens)
			throws LineNumberParseException {
		int maxSections = 3;
		DLBBodyToken startToken = tokens.getCurrent();
		tokens.moveNext();
		List<ReplySection> sections = new ArrayList<>();
		ReplySection currSection = new ReplySection();
		sections.add(currSection);
		boolean foundEnd = false;
		while (!foundEnd && tokens.getCurrent() != null) {
			DLBBodyToken token = tokens.getCurrent();
			switch (token.getType()) {
			case REPLY_SEPARATOR:
				if (sections.size() == maxSections) {
					throw new LineNumberParseException(String.format(
							"Exceeded maximum number of %s sections",
							maxSections), token.getLineNum(),
							token.getColNum());
				}
				currSection.endLineNum = token.getLineNum();
				currSection.endColNum = token.getColNum();
				currSection = new ReplySection();
				sections.add(currSection);
				break;
			case REPLY_END:
				currSection.endLineNum = token.getLineNum();
				currSection.endColNum = token.getColNum();
				foundEnd = true;
				break;
			default:
				currSection.tokens.add(token);
			}
			tokens.moveNext();
		}
		if (!foundEnd) {
			throw new LineNumberParseException("Reply not terminated",
					startToken.getLineNum(), startToken.getColNum());
		}
		statementSection = null;
		nodePointerSection = null;
		commandSection = null;
		if (sections.size() == 1) {
			nodePointerSection = sections.get(0);
		} else if (sections.size() == 2) {
			statementSection = sections.get(0);
			nodePointerSection = sections.get(1);
		} else {
			statementSection = sections.get(0);
			nodePointerSection = sections.get(1);
			commandSection = sections.get(2);
		}
	}
	
	private DLBNodeBody parseStatement() throws LineNumberParseException {
		if (statementSection == null)
			return null;
		DLBBodyParser bodyParser = new DLBBodyParser(nodeState);
		DLBNodeBody result = bodyParser.parse(statementSection.tokens,
				Arrays.asList("input"));
		if (result.getSegments().isEmpty())
			return null;
		else
			return result;
	}
	
	private DLBNodePointer parseNodePointer() throws LineNumberParseException {
		DLBBodyToken.trimWhitespace(nodePointerSection.tokens);
		if (nodePointerSection.tokens.size() == 0) {
			throw new LineNumberParseException("Empty node pointer in reply",
					nodePointerSection.endLineNum,
					nodePointerSection.endColNum);
		}
		DLBBodyToken nodePointerToken = nodePointerSection.tokens.get(0);
		if (nodePointerSection.tokens.size() != 1 ||
				nodePointerToken.getType() != DLBBodyToken.Type.TEXT) {
			throw new LineNumberParseException(
					"Invalid node pointer in reply",
					nodePointerToken.getLineNum(),
					nodePointerToken.getColNum());
		}
		String nodePointerStr = (String)nodePointerToken.getValue();
		DLBNodePointer result;
		if (nodePointerStr.matches(DLBParser.NODE_NAME_REGEX)) {
			result = new DLBNodePointerInternal(nodePointerStr);
		} else if (nodePointerStr.matches(
				DLBParser.EXTERNAL_NODE_POINTER_REGEX)) {
			int sep = nodePointerStr.lastIndexOf('.');
			try {
				result = new DLBNodePointerExternal(
						nodeState.getDialogueName(),
						nodePointerStr.substring(0, sep),
						nodePointerStr.substring(sep + 1));
			} catch (ParseException ex) {
				throw new LineNumberParseException(
						"Invalid node pointer in reply: " + ex.getMessage(),
						nodePointerToken.getLineNum(),
						nodePointerToken.getColNum(), ex);
			}
		} else {
			throw new LineNumberParseException(
					"Invalid node pointer in reply: " + nodePointerStr,
					nodePointerToken.getLineNum(),
					nodePointerToken.getColNum());
		}
		nodeState.addNodePointerToken(result, nodePointerToken);
		return result;
	}
	
	private void parseCommands(DLBReply reply)
			throws LineNumberParseException {
		CurrentIterator<DLBBodyToken> it = new CurrentIterator<>(
				commandSection.tokens.iterator());
		it.moveNext();
		DLBBodyToken.skipWhitespace(it);
		while (it.getCurrent() != null) {
			DLBBodyToken token = it.getCurrent();
			if (token.getType() != DLBBodyToken.Type.COMMAND_START) {
				throw new LineNumberParseException(
						"Expected <<, found token: " + token.getType(),
						token.getLineNum(), token.getColNum());
			}
			DLBCommandParser cmdParser = new DLBCommandParser(
					Arrays.asList("action", "set"), nodeState);
			reply.addCommand(cmdParser.parseFromStart(it));
			DLBBodyToken.skipWhitespace(it);
		}
	}

	private class ReplySection {
		private List<DLBBodyToken> tokens = new ArrayList<>();
		private int endLineNum;
		private int endColNum;
	}
}
