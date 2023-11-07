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

import com.dialoguebranch.model.command.*;
import com.dialoguebranch.model.nodepointer.NodePointer;
import nl.rrd.utils.expressions.EvaluationException;
import com.dialoguebranch.model.nodepointer.NodePointerExternal;
import com.dialoguebranch.model.nodepointer.NodePointerInternal;

import java.util.*;

/**
 * A node body can occur in three different contexts inside a {@link DLBNode}.
 * 
 * <ul>
 *   <li>Directly in the node. In this case it specifies the agent statement with possible commands
 *   and user replies.</li>
 *   <li>As part of a clause in a {@link IfCommand} or {@link RandomCommand}. The content is
 *   the same as directly in the node. The only difference is that it is performed
 *   conditionally.</li>
 *   <li>As part of a {@link DLBReply}. In this case it specifies the user statement with possible
 *   commands, but no replies. Note that the UI shows these statements as options immediately along
 *   with the agent statement. This {@link DLBNodeBody} does not contain commands that are to be
 *   performed when the reply is chosen. Such commands are specified separately in a
 *   {@link DLBReply}.</li>
 * </ul>
 * 
 * <p>The body contains a statement as a list of segments where each segment is one of:</p>
 * 
 * <ul>
 *   <li>{@link DLBNodeBody.TextSegment TextSegment}: a {@link DLBVariableString} with text and
 *   variables</li>
 *   <li>{@link DLBNodeBody.CommandSegment CommandSegment}: a command (see below)</li>
 * </ul>
 * 
 * <p>The segments are always normalized so that subsequent text segments are
 * automatically merged into one.</p>
 * 
 * <p>The type of commands depend on the context. Directly in the node or in a
 * {@link IfCommand} or {@link RandomCommand}, it can be:</p>
 * 
 * <ul>
 *   <li>{@link ActionCommand}: Actions to perform along with the agent's text statement.</li>
 *   <li>{@link IfCommand}: Contains clauses, each with a {@link DLBNodeBody} specifying
 *   conditional statements, replies and commands.</li>
 *   <li>{@link RandomCommand}: Contains clauses, each with a {@link DLBNodeBody} specifying
 *   statements, replies and commands.</li>
 *   <li>{@link SetCommand}: Sets a variable value.</li>
 * </ul>
 * 
 * <p>As part of a reply (remember the earlier remarks about commands in a
 * reply), it can be:</p>
 * 
 * <ul>
 *   <li>{@link InputCommand}: Allow user to provide input other than just clicking the reply
 *   option.</li>
 * </ul>
 * 
 * @author Dennis Hofs (RRD)
 */
public class DLBNodeBody {
	private List<Segment> segments = new ArrayList<>();
	private List<DLBReply> replies = new ArrayList<>();

	public DLBNodeBody() {
	}

	public DLBNodeBody(DLBNodeBody other) {
		for (Segment segment : other.segments) {
			this.segments.add(segment.clone());
		}
		for (DLBReply reply : other.replies) {
			this.replies.add(new DLBReply(reply));
		}
	}

	/**
	 * Returns the segments as an unmodifiable list.
	 * 
	 * @return the segments as an unmodifiable list
	 */
	public List<Segment> getSegments() {
		return Collections.unmodifiableList(segments);
	}

	public void addSegment(Segment segment) {
		Segment lastSegment = null;
		if (!segments.isEmpty())
			lastSegment = segments.get(segments.size() - 1);
		if (lastSegment instanceof TextSegment &&
				segment instanceof TextSegment) {
			TextSegment lastTextSegment = (TextSegment)lastSegment;
			TextSegment textSegment = (TextSegment)segment;
			DLBVariableString text = new DLBVariableString();
			text.addSegments(lastTextSegment.text.getSegments());
			text.addSegments(textSegment.text.getSegments());
			TextSegment mergedSegment = new TextSegment(text);
			segments.remove(segments.size() - 1);
			segments.add(mergedSegment);
		} else {
			segments.add(segment);
		}
	}

	public void clearSegments() {
		segments.clear();
	}

	private void trimText() {
		if (!segments.isEmpty() && segments.get(0) instanceof TextSegment) {
			TextSegment segment = (TextSegment)segments.get(0);
			String text = segment.text.evaluate(null).replaceAll("^\\s+", "");
			segment.text = new DLBVariableString(text);
		}
		if (!segments.isEmpty() && segments.get(segments.size() - 1)
				instanceof TextSegment) {
			TextSegment segment = (TextSegment)segments.get(
					segments.size() - 1);
			String text = segment.text.evaluate(null).replaceAll("\\s+$", "");
			segment.text = new DLBVariableString(text);
		}
	}

	public List<DLBReply> getReplies() {
		return replies;
	}
	
	public DLBReply findReplyById(int replyId) {
		for (DLBReply reply : replies) {
			if (reply.getReplyId() == replyId)
				return reply;
		}
		for (Segment segment : segments) {
			DLBReply reply = segment.findReplyById(replyId);
			if (reply != null)
				return reply;
		}
		return null;
	}

	public void addReply(DLBReply reply) {
		replies.add(reply);
	}

	/**
	 * Retrieves all variable names that are read in this body.
	 * 
	 * @return the variable names that are read in this body
	 */
	public List<String> getReadVariableNames() {
		Set<String> set = new HashSet<>();
		getReadVariableNames(set);
		List<String> result = new ArrayList<>(set);
		Collections.sort(result);
		return result;
	}
	
	/**
	 * Retrieves all variable names that are read in this body and adds them to
	 * the specified set.
	 * 
	 * @param varNames the set to which the variable names are added
	 */
	public void getReadVariableNames(Set<String> varNames) {
		for (Segment segment : segments) {
			segment.getReadVariableNames(varNames);
		}
		for (DLBReply reply : replies) {
			reply.getReadVariableNames(varNames);
		}
	}

	/**
	 * Retrieves all variable names that are written in this body.
	 * 
	 * @return the variable names that are written in this body
	 */
	public List<String> getWriteVariableNames() {
		Set<String> set = new HashSet<>();
		getWriteVariableNames(set);
		List<String> result = new ArrayList<>(set);
		Collections.sort(result);
		return result;
	}

	/**
	 * Retrieves all variable names that are written in this body and adds them
	 * to the specified set.
	 * 
	 * @param varNames the set to which the variable names are added
	 */
	public void getWriteVariableNames(Set<String> varNames) {
		for (Segment segment : segments) {
			segment.getWriteVariableNames(varNames);
		}
		for (DLBReply reply : replies) {
			reply.getWriteVariableNames(varNames);
		}
	}
	
	public List<NodePointer> getNodePointers() {
		Set<NodePointer> set = new HashSet<>();
		getNodePointers(set);
		List<NodePointer> result = new ArrayList<>(set);
		Collections.sort(result, this::compareNodePointers);
		return result;
	}
	
	private int compareNodePointers(NodePointer o1, NodePointer o2) {
		if (o1 instanceof NodePointerInternal) {
			if (o2 instanceof NodePointerExternal)
				return -1;
			NodePointerInternal p1 = (NodePointerInternal)o1;
			NodePointerInternal p2 = (NodePointerInternal)o2;
			return p1.getNodeId().compareTo(p2.getNodeId());
		} else {
			if (o2 instanceof NodePointerInternal)
				return -1;
			NodePointerExternal p1 = (NodePointerExternal)o1;
			NodePointerExternal p2 = (NodePointerExternal)o2;
			int result = p1.getDialogueId().compareTo(p2.getDialogueId());
			if (result != 0)
				return result;
			return p1.getNodeId().compareTo(p2.getNodeId());
		}
	}
	
	public void getNodePointers(Set<NodePointer> pointers) {
		for (Segment segment : segments) {
			if (!(segment instanceof CommandSegment))
				continue;
			Command command = ((CommandSegment)segment).command;
			command.getNodePointers(pointers);
		}
		for (DLBReply reply : replies) {
			pointers.add(reply.getNodePointer());
		}
	}
	
	/**
	 * Executes the agent statement and reply statements in this body with
	 * respect to the specified variable map. It executes ("if" and "set")
	 * commands and resolves variables. Any resulting body content that should
	 * be sent to the client, is added to agent and reply statements in
	 * "processedBody". This content can be text or client commands, with all
	 * variables resolved.
	 * 
	 * <p>This method also normalizes whitespace in the text segments. It
	 * removes empty lines and makes sure that lines end with "\n". Within each
	 * line, it trims whitespace from the start and end, and it replaces any
	 * sequence of spaces and tabs with one space.</p>
	 * 
	 * <p>This method should only be called if all variables in the text
	 * segments have been resolved.</p>
	 *  
	 * @param variables the variable map
	 * @param trimText true if trailing new lines should be trimmed, false if
	 * they should be preserved. This should be set to true for the body that is
	 * directly in the node. If the body is in an "if" clause or in a reply, it
	 * should be set to false.
	 * @param processedBody the processed body
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	public void execute(Map<String,Object> variables, boolean trimText,
			DLBNodeBody processedBody) throws EvaluationException {
		for (Segment segment : segments) {
			if (segment instanceof TextSegment) {
				executeTextSegment((TextSegment)segment, variables,
						processedBody);
			} else {
				executeCommandSegment((CommandSegment)segment, variables,
						processedBody);
			}
		}
		for (DLBReply reply : replies) {
			processedBody.addReply(reply.execute(variables));
		}
		if (trimText)
			processedBody.trimText();
	}
	
	private void executeTextSegment(TextSegment segment,
			Map<String,Object> variables, DLBNodeBody processedBody) {
		TextSegment processedText = new TextSegment(
				segment.text.execute(variables));
		processedBody.addSegment(processedText);
	}
	
	private void executeCommandSegment(CommandSegment segment,
			Map<String,Object> variables, DLBNodeBody processedBody)
			throws EvaluationException {
		segment.command.executeBodyCommand(variables, processedBody);
	}

	public void trimWhitespace() {
		trimWhitespace(segments);
	}

	public static void trimWhitespace(List<DLBNodeBody.Segment> segments) {
		removeLeadingWhitespace(segments);
		removeTrailingWhitespace(segments);
	}

	public void removeLeadingWhitespace() {
		removeLeadingWhitespace(segments);
	}

	public static void removeLeadingWhitespace(List<DLBNodeBody.Segment> segments) {
		while (!segments.isEmpty()) {
			Segment segment = segments.get(0);
			if (!(segment instanceof TextSegment))
				return;
			TextSegment textSegment = (TextSegment)segment;
			DLBVariableString text = textSegment.getText();
			text.removeLeadingWhitespace();
			if (!text.getSegments().isEmpty())
				return;
			segments.remove(0);
		}
	}

	public void removeTrailingWhitespace() {
		removeTrailingWhitespace(segments);
	}

	public static void removeTrailingWhitespace(List<DLBNodeBody.Segment> segments) {
		while (!segments.isEmpty()) {
			Segment segment = segments.get(segments.size() - 1);
			if (!(segment instanceof TextSegment))
				return;
			TextSegment textSegment = (TextSegment)segment;
			DLBVariableString text = textSegment.getText();
			text.removeTrailingWhitespace();
			if (!text.getSegments().isEmpty())
				return;
			segments.remove(segments.size() - 1);
		}
	}
	
	@Override
	public String toString() {
		String newline = System.getProperty("line.separator");
		StringBuilder builder = new StringBuilder();
		for (Segment segment : segments) {
			builder.append(segment.toString());
		}
		for (DLBReply reply : replies) {
			builder.append(newline);
			builder.append(reply);
		}
		return builder.toString();
	}

	public static abstract class Segment implements Cloneable {
		/**
		 * Tries to find a reply with the specified ID within this segment. If
		 * no such reply is found, this method returns null.
		 * 
		 * @param replyId the reply ID
		 * @return the reply or null
		 */
		public abstract DLBReply findReplyById(int replyId);

		/**
		 * Retrieves all variable names that are read in this segment and adds
		 * them to the specified set.
		 * 
		 * @param varNames the set to which the variable names are added
		 */
		public abstract void getReadVariableNames(Set<String> varNames);
		
		/**
		 * Retrieves all variable names that are written in this segment and
		 * adds them to the specified set.
		 * 
		 * @param varNames the set to which the variable names are added
		 */
		public abstract void getWriteVariableNames(Set<String> varNames);

		/**
		 * Returns a deep copy of this segment.
		 *
		 * @return a deep copy of this segment
		 */
		@Override
		public abstract Segment clone();
	}
	
	public static class TextSegment extends Segment {
		private DLBVariableString text;
		
		public TextSegment(DLBVariableString text) {
			this.text = text;
		}

		public TextSegment(TextSegment other) {
			this.text = new DLBVariableString(other.text);
		}

		public DLBVariableString getText() {
			return text;
		}

		public void setText(DLBVariableString text) {
			this.text = text;
		}
		
		@Override
		public DLBReply findReplyById(int replyId) {
			return null;
		}

		@Override
		public void getReadVariableNames(Set<String> varNames) {
			text.getReadVariableNames(varNames);
		}

		@Override
		public void getWriteVariableNames(Set<String> varNames) {
		}

		@Override
		public String toString() {
			return text.toString();
		}

		@Override
		public TextSegment clone() {
			return new TextSegment(this);
		}
	}
	
	public static class CommandSegment extends Segment {
		private Command command;
		
		public CommandSegment(Command command) {
			this.command = command;
		}

		public CommandSegment(CommandSegment other) {
			this.command = other.command.clone();
		}

		public Command getCommand() {
			return command;
		}
		
		@Override
		public DLBReply findReplyById(int replyId) {
			return command.findReplyById(replyId);
		}

		@Override
		public void getReadVariableNames(Set<String> varNames) {
			command.getReadVariableNames(varNames);
		}

		@Override
		public void getWriteVariableNames(Set<String> varNames) {
			command.getWriteVariableNames(varNames);
		}
		
		@Override
		public String toString() {
			return command.toString();
		}

		@Override
		public CommandSegment clone() {
			return new CommandSegment(this);
		}
	}
}
