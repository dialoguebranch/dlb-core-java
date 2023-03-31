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

import com.dialoguebranch.model.DLBNodeBody;
import com.dialoguebranch.model.DLBReply;
import com.dialoguebranch.model.command.DLBCommand;
import nl.rrd.utils.CurrentIterator;
import nl.rrd.utils.exception.LineNumberParseException;
import com.dialoguebranch.model.DLBVariableString;

import java.util.ArrayList;
import java.util.List;

public class DLBBodyParser {
	private DLBNodeState nodeState;
	
	public DLBBodyParser(DLBNodeState nodeState) {
		this.nodeState = nodeState;
	}
	
	public DLBNodeBody parse(List<DLBBodyToken> tokens,
							 List<String> validCommands) throws LineNumberParseException {
		CurrentIterator<DLBBodyToken> it = new CurrentIterator<>(
				tokens.iterator());
		it.moveNext();
		ParseUntilCommandClauseResult result = parseUntilCommandClause(it,
				validCommands, new ArrayList<>());
		return result.body;
	}

	/**
	 * Parse the specified tokens until a subclause of a command is found. There
	 * are two commands that can have subclauses:
	 *
	 * <p><ul>
	 * <li>if: has subclauses "elseif", "else" and "endif"</li>
	 * <li>random: has subclauses "or" and "endrandom"</li>
	 * </ul></p>
	 *
	 * <p>If any command token is found that is not in "validCommands" or in
	 * "validCommandClauses", then this method throws a parse exception.</p>
	 *
	 * @param tokens the tokens
	 * @param validCommands valid commands
	 * @param validCommandClauses valid command clauses
	 * @return the result
	 * @throws LineNumberParseException if a parse error occurs
	 */
	public ParseUntilCommandClauseResult parseUntilCommandClause(
			CurrentIterator<DLBBodyToken> tokens, List<String> validCommands,
			List<String> validCommandClauses) throws LineNumberParseException {
		ParseUntilCommandClauseResult result =
				new ParseUntilCommandClauseResult();
		result.body = new DLBNodeBody();
		while (result.cmdClauseStartToken == null &&
				tokens.getCurrent() != null) {
			DLBBodyToken token = tokens.getCurrent();
			switch (token.getType()) {
			case TEXT:
			case VARIABLE:
				DLBVariableString text = parseTextSegment(tokens);
				if (result.body.getReplies().isEmpty()) {
					result.body.addSegment(new DLBNodeBody.TextSegment(text));
				} else if (!text.isWhitespace()) {
					throw new LineNumberParseException(
							"Found content after reply", token.getLineNum(),
							token.getColNum());
				}
				break;
			case COMMAND_START:
				DLBCommandParser cmdParser = new DLBCommandParser(
						validCommands, nodeState);
				String name = cmdParser.readCommandName(tokens);
				if (validCommandClauses.contains(name)) {
					result.cmdClauseStartToken = token;
					result.cmdClauseName = name;
				} else if (!name.equals("if") && !name.equals("random") &&
						!result.body.getReplies().isEmpty()) {
					throw new LineNumberParseException(
							"Found << after reply", token.getLineNum(),
							token.getColNum());
				} else {
					DLBCommand command = cmdParser.parseFromName(token,
							tokens);
					result.body.addSegment(new DLBNodeBody.CommandSegment(
							command));
				}
				break;
			case REPLY_START:
				if (nodeState == null) {
					throw new LineNumberParseException(
							"Unexpected start of reply [[", token.getLineNum(),
							token.getColNum());
				}
				DLBReplyParser replyParser = new DLBReplyParser(nodeState);
				DLBReply reply = replyParser.parse(tokens);
				if (reply.getStatement() == null &&
						hasAutoForwardReply(result.body)) {
					throw new LineNumberParseException(
							"Found more than one autoforward reply",
							token.getLineNum(), token.getColNum());
				}
				result.body.addReply(reply);
				break;
			default:
				// If we get here, there must be a bug
				throw new LineNumberParseException("Unexpected token type: " +
						token.getType(), token.getLineNum(), token.getColNum());
			}
		}
		result.body.trimWhitespace();
		return result;
	}
	
	private boolean hasAutoForwardReply(DLBNodeBody body) {
		for (DLBReply reply : body.getReplies()) {
			if (reply.getStatement() == null)
				return true;
		}
		return false;
	}

	public static class ParseUntilCommandClauseResult {
		public DLBNodeBody body;
		public DLBBodyToken cmdClauseStartToken = null;
		public String cmdClauseName = null;
	}
	
	private DLBVariableString parseTextSegment(
			CurrentIterator<DLBBodyToken> tokens) {
		DLBVariableString string = new DLBVariableString();
		boolean foundEnd = false;
		while (!foundEnd && tokens.getCurrent() != null) {
			DLBBodyToken token = tokens.getCurrent();
			switch (token.getType()) {
			case TEXT:
				string.addSegment(new DLBVariableString.TextSegment(
						(String)token.getValue()));
				break;
			case VARIABLE:
				string.addSegment(new DLBVariableString.VariableSegment(
						(String)token.getValue()));
				break;
			default:
				foundEnd = true;
			}
			if (!foundEnd)
				tokens.moveNext();
		}
		return string;
	}
}
