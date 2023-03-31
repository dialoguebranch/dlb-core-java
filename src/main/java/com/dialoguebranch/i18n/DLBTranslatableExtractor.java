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

import com.dialoguebranch.model.command.DLBCommand;
import com.dialoguebranch.model.DLBNode;
import com.dialoguebranch.model.DLBNodeBody;
import com.dialoguebranch.model.DLBReply;
import com.dialoguebranch.model.DLBVariableString;
import com.dialoguebranch.model.command.DLBIfCommand;
import com.dialoguebranch.model.command.DLBInputCommand;
import com.dialoguebranch.model.command.DLBRandomCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * This class can extract all translatable segments (plain text, variables and
 * &lt;&lt;input&gt;&gt; commands) from a {@link DLBNode} or {@link
 * DLBNodeBody}. This includes translatables within "if" and
 * "random" commands and replies.
 *
 * @author Dennis Hofs (RRD)
 */
public class DLBTranslatableExtractor {
	public List<DLBSourceTranslatable> extractFromNode(DLBNode node) {
		return extractFromBody(node.getHeader().getSpeaker(),
				DLBSourceTranslatable.USER, node.getBody());
	}

	public List<DLBSourceTranslatable> extractFromBody(String speaker,
													   String addressee, DLBNodeBody body) {
		List<DLBSourceTranslatable> result = new ArrayList<>();
		List<DLBNodeBody.Segment> current = new ArrayList<>();
		for (int i = 0; i < body.getSegments().size(); i++) {
			DLBNodeBody.Segment segment = body.getSegments().get(i);
			if (segment instanceof DLBNodeBody.TextSegment) {
				DLBNodeBody.TextSegment textSegment =
						(DLBNodeBody.TextSegment)segment;
				current.add(textSegment);
			} else if (segment instanceof DLBNodeBody.CommandSegment) {
				DLBNodeBody.CommandSegment cmdSegment =
						(DLBNodeBody.CommandSegment)segment;
				DLBCommand cmd = cmdSegment.getCommand();
				if (cmd instanceof DLBIfCommand) {
					DLBIfCommand ifCmd = (DLBIfCommand)cmd;
					finishCurrentTranslatableSegment(speaker, addressee, body,
							current, result);
					result.addAll(getTranslatableSegmentsFromIfCommand(speaker,
							addressee, ifCmd));
				} else if (cmd instanceof DLBRandomCommand) {
					DLBRandomCommand rndCmd = (DLBRandomCommand)cmd;
					finishCurrentTranslatableSegment(speaker, addressee, body,
							current, result);
					result.addAll(getTranslatableSegmentsFromRandomCommand(
							speaker, addressee, rndCmd));
				} else if (cmd instanceof DLBInputCommand) {
					current.add(segment);
				}
			}
		}
		finishCurrentTranslatableSegment(speaker, addressee, body, current,
				result);
		for (DLBReply reply : body.getReplies()) {
			if (reply.getStatement() != null) {
				result.addAll(extractFromBody(addressee, speaker,
						reply.getStatement()));
			}
		}
		return result;
	}

	private List<DLBSourceTranslatable> getTranslatableSegmentsFromIfCommand(
			String speaker, String addressee, DLBIfCommand ifCmd) {
		List<DLBSourceTranslatable> result = new ArrayList<>();
		for (DLBIfCommand.Clause clause : ifCmd.getIfClauses()) {
			result.addAll(extractFromBody(speaker, addressee,
					clause.getStatement()));
		}
		if (ifCmd.getElseClause() != null) {
			result.addAll(extractFromBody(speaker, addressee,
					ifCmd.getElseClause()));
		}
		return result;
	}

	private List<DLBSourceTranslatable> getTranslatableSegmentsFromRandomCommand(
			String speaker, String addressee, DLBRandomCommand rndCmd) {
		List<DLBSourceTranslatable> result = new ArrayList<>();
		for (DLBRandomCommand.Clause clause : rndCmd.getClauses()) {
			result.addAll(extractFromBody(speaker, addressee,
					clause.getStatement()));
		}
		return result;
	}

	private void finishCurrentTranslatableSegment(String speaker,
			String addressee, DLBNodeBody parent,
			List<DLBNodeBody.Segment> current,
			List<DLBSourceTranslatable> translatables) {
		if (hasContent(current)) {
			List<DLBNodeBody.Segment> segments = new ArrayList<>(current);
			DLBSourceTranslatable translatable = new DLBSourceTranslatable(
					speaker, addressee, new DLBTranslatable(parent, segments));
			translatables.add(translatable);
		}
		current.clear();
	}

	private boolean hasContent(List<DLBNodeBody.Segment> segments) {
		for (DLBNodeBody.Segment segment : segments) {
			if (segment instanceof DLBNodeBody.TextSegment) {
				DLBNodeBody.TextSegment textSegment =
						(DLBNodeBody.TextSegment)segment;
				DLBVariableString string = textSegment.getText();
				if (hasContent(string))
					return true;
			} else if (segment instanceof DLBNodeBody.CommandSegment) {
				DLBNodeBody.CommandSegment cmdSegment =
						(DLBNodeBody.CommandSegment)segment;
				if (cmdSegment.getCommand() instanceof DLBInputCommand)
					return true;
			}
		}
		return false;
	}

	private boolean hasContent(DLBVariableString string) {
		return !string.getSegments().isEmpty() && !string.isWhitespace();
	}
}
