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

package com.dialoguebranch.execution;

import com.dialoguebranch.model.*;
import com.dialoguebranch.model.command.DLBCommand;
import com.dialoguebranch.model.command.DLBInputCommand;
import com.dialoguebranch.model.command.DLBSetCommand;
import com.dialoguebranch.model.nodepointer.DLBNodePointer;
import com.dialoguebranch.model.nodepointer.DLBNodePointerInternal;
import nl.rrd.utils.expressions.EvaluationException;
import com.dialoguebranch.exception.DLBException;

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An {@link ActiveDialogue} is a wrapper around a {@link DLBDialogue}, which contains
 * a static definition of a dialogue (referred to as the {@code dialogueDefinition}). 
 * The {@link ActiveDialogue} also contains utility functions to keep track of the state during
 * "execution" of the dialogue.
 * 
 * @author Harm op den Akker
 * @author Tessa Beinema
 */
public class ActiveDialogue {

	private DLBDialogueDescription dialogueDescription;
	private DLBDialogue dialogueDefinition;
	private DLBNode currentNode;
	private DLBVariableStore dlbVariableStore;
		
	// ----------- Constructors:

	/**
	 * Creates an instance of an {@link ActiveDialogue} with a given {@link
	 * DLBDialogueDescription} and {@link DLBDialogue}.
	 *
	 * @param dialogueDescription the dialogue description
	 * @param dialogueDefinition the dialogue definition
	 */
	public ActiveDialogue(DLBDialogueDescription dialogueDescription,
						  DLBDialogue dialogueDefinition) {
		this.dialogueDescription = dialogueDescription;
		this.dialogueDefinition = dialogueDefinition;
	}
	
	// ---------- Getters:

	public DLBDialogueDescription getDialogueDescription() {
		return dialogueDescription;
	}

	public DLBDialogue getDialogueDefinition() {
		return dialogueDefinition;
	}
	
	public DLBNode getCurrentNode() {
		return currentNode;
	}

	/**
	 * Returns the {@link DLBVariableStore} associated with this {@link ActiveDialogue}.
	 * @return the {@link DLBVariableStore} associated with this {@link ActiveDialogue}.
	 */
	public DLBVariableStore getDLBVariableStore() {
		return dlbVariableStore;
	}
	
	// ---------- Setters:

	/**
	 * Sets the {@link DLBVariableStore} used to store/retrieve parameters for this {@link ActiveDialogue}.
	 * @param dlbVariableStore the {@link DLBVariableStore} used to store/retrieve parameters for this {@link ActiveDialogue}.
	 */
	public void setDLBVariableStore(DLBVariableStore dlbVariableStore) {
		this.dlbVariableStore = dlbVariableStore;
	}

	public void setCurrentNode(DLBNode currentNode) {
		this.currentNode = currentNode;
	}

	// ---------- Convenience:
	
	/**
	 * Returns the name of this {@link ActiveDialogue} as defined in the associated {@link DLBDialogue}.
	 * @return the name of this {@link ActiveDialogue} as defined in the associated {@link DLBDialogue}.
	 */
	public String getDialogueName() {
		return dialogueDefinition.getDialogueName();
	}
	
	// ---------- Functions:
	
	/**
	 * "Starts" this {@link ActiveDialogue}, returning the start node and
	 * updating its internal state.
	 *
	 * @param eventTime the timestamp (in the time zone of the user) of the event that triggered
	 *                  this start of the DialogueBranch dialogue.
	 * @return the initial {@link DLBNode}.
	 * @throws DLBException if the request is invalid
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	public DLBNode startDialogue(ZonedDateTime eventTime) throws DLBException,
			EvaluationException {
		return startDialogue(null, eventTime);
	}
	
	/**
	 * "Starts" this {@link ActiveDialogue} at the {@link DLBNode} represented by
	 * the provided {@code nodeId}, or at the "Start" node of the dialogue if the given
	 * {@code nodeId} is {@code null}, returning that node and updating the dialogue's internal
	 * state. If you set the nodeId to null, it will return the start node.
	 *
	 * @param nodeId the node ID or {@code null} (to start from the "Start" node).
	 * @param eventTime the timestamp (in the time zone of the user) that triggered this start of
	 *                  the DialogueBranch dialogue
	 * @return the {@link DLBNode}
	 * @throws DLBException if the request is invalid
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	public DLBNode startDialogue(String nodeId, ZonedDateTime eventTime)
			throws DLBException, EvaluationException {
		DLBNode nextNode;
		if (nodeId == null) {
			nextNode = dialogueDefinition.getStartNode();
		} else {
			nextNode = dialogueDefinition.getNodeById(nodeId);
			if (nextNode == null) {
				throw new DLBException(DLBException.Type.NODE_NOT_FOUND,
						String.format("Node \"%s\" not found in dialogue \"%s\"",
								nodeId, dialogueDefinition.getDialogueName()));
			}
		}
		this.currentNode = executeDLBNode(nextNode,eventTime);
		return currentNode;
	}
	
	/**
	 * Retrieves the pointer to the next node based on the provided reply id.
	 * This might be a pointer to the end node. This method also performs any
	 * "set" actions associated with the reply.
	 * 
	 * @param replyId the reply ID
	 * @param eventTime the time (in the user's timezone) of the event that triggered this
	 *                  processing of the reply.
	 * @return The {@link DLBNodePointer} pointing to the next DialogueBranch Node
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	public DLBNodePointer processReplyAndGetNodePointer(int replyId, ZonedDateTime eventTime)
			throws EvaluationException {
		DLBReply selectedDLBReply = currentNode.getBody().findReplyById(replyId);
		Map<String,Object> variableMap =
				dlbVariableStore.getModifiableMap(true, eventTime,
						DLBVariableStoreChange.Source.DLB_SCRIPT);
		for (DLBCommand command : selectedDLBReply.getCommands()) {
			if (command instanceof DLBSetCommand) {
				DLBSetCommand setCommand = (DLBSetCommand)command;
				setCommand.getExpression().evaluate(variableMap);
			}
		}
		return selectedDLBReply.getNodePointer();
	}
	
	/**
	 * Takes the next node pointer from the selected reply and determines the
	 * next node. The pointer might point to the end note, which means that
	 * there is no next node. If there is no next node, or the next node has no
	 * reply options, then the dialogue is considered finished.
	 * 
	 * <p>If there is a next node, then it returns the executed version of that
	 * next {@link DLBNode}.</p>
	 *  
	 * @param nodePointer the next node pointer from the selected reply
	 * @param eventTime the timestamp (in the time zone of the user) of the event that triggered the
	 *                  progressing of the dialogue
	 * @return the next {@link DLBNode} that follows on the selected reply or
	 * null  
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	public DLBNode progressDialogue(DLBNodePointerInternal nodePointer, ZonedDateTime eventTime)
			throws EvaluationException {
		DLBNode nextNode = null;
		if (!nodePointer.getNodeId().equalsIgnoreCase("end"))
			nextNode = dialogueDefinition.getNodeById(nodePointer.getNodeId());
		this.currentNode = nextNode;
		if (nextNode != null)
			this.currentNode = executeDLBNode(nextNode, eventTime);
		return currentNode;
	}

	/**
	 * Stores the specified variables in the variable store.
	 *
	 * @param eventTime the time (in the time zone of the user) of the event that triggered this
	 *                  change in DialogueBranch Variables.
	 * @param variables the variables
	 * // TODO: It's not exactly clear how this method is supposed to be used. The assumption is now
	 *                  that this method stores a set of variables that are the direct result from
	 *                  a user input reply and therefore the chosen INPUT_REPLY source is used.
	 */
	public void storeReplyInput(Map<String,?> variables, ZonedDateTime eventTime) {
		dlbVariableStore.addAll(variables,true,eventTime,
				DLBVariableStoreChange.Source.INPUT_REPLY);
	}

	/**
	 * The user's client returned the given {@code replyId} - what was the
	 * statement that was uttered by the user?
	 *
	 * @param replyId the reply ID
	 * @return the statement
	 * @throws DLBException if no reply with the specified ID is found
	 */
	public String getUserStatementFromReplyId(int replyId) throws DLBException {
		DLBReply selectedReply = currentNode.getBody().findReplyById(replyId);
		if (selectedReply == null) {
			throw new DLBException(DLBException.Type.REPLY_NOT_FOUND,
					String.format("Reply with ID %s not found in dialogue \"%s\", node \"%s\"",
					replyId, dialogueDefinition.getDialogueName(),
					currentNode.getTitle()));
		}
		if (selectedReply.getStatement() == null)
			return "AUTOFORWARD";
		StringBuilder result = new StringBuilder();
		List<DLBNodeBody.Segment> segments = selectedReply.getStatement()
				.getSegments();
		for (DLBNodeBody.Segment segment : segments) {
			if (segment instanceof DLBNodeBody.TextSegment) {
				DLBNodeBody.TextSegment textSegment =
						(DLBNodeBody.TextSegment)segment;
				result.append(textSegment.getText().evaluate(null));
			} else {
				DLBNodeBody.CommandSegment cmdSegment =
						(DLBNodeBody.CommandSegment)segment;
				// a reply statement can only contain an "input" command
				DLBInputCommand command =
						(DLBInputCommand)cmdSegment.getCommand();
				result.append(command.getStatementLog(dlbVariableStore));
			}
		}
		return result.toString();
	}

	/**
	 * Executes the agent statement and reply statements in the specified node.
	 * It executes ("if", "random" and "set") commands and resolves variables.
	 * Any resulting body content that should be sent to the client, is added to
	 * the (agent or reply) statement body in the resulting node. This content
	 * can be text or client commands, with all variables resolved.
	 *
	 * @param DLBNode a node to execute
	 * @param eventTime the time stamp (in the time zone of the user) of the event that triggered
	 *                  the execution of this DialogueBranch Node
	 * @return the executed {@link DLBNode}.
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	public DLBNode executeDLBNode(DLBNode DLBNode, ZonedDateTime eventTime)
			throws EvaluationException {
		DLBNode processedNode = new DLBNode();
		processedNode.setHeader(DLBNode.getHeader());
		DLBNodeBody processedBody = new DLBNodeBody();
		Map<String,Object> variables =
				dlbVariableStore.getModifiableMap(true, eventTime,
						DLBVariableStoreChange.Source.DLB_SCRIPT);
		DLBNode.getBody().execute(variables, true, processedBody);
		processedNode.setBody(processedBody);
		return processedNode;
	}

	/**
	 * Executes the agent statement and reply statements in the specified node.
	 * It executes "if" and "random" commands and resolves variables. Any
	 * resulting body content that should be sent to the client, is added to the
	 * (agent or reply) statement body in the resulting node. This content can
	 * be text or client commands, with all variables resolved.
	 *
	 * <p>This method does not change the dialogue state and does not change
	 * any variables. Any "set" commands have no effect.</p>
	 *
	 * @param DLBNode a node to execute.
	 * @param eventTime the timestamp (in the time zone of the user) of the event that triggered the
	 *                  execution of the DialogueBranch Node
	 * @return the executed {@link DLBNode}
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	public DLBNode executeDLBNodeStateless(DLBNode DLBNode, ZonedDateTime eventTime)
			throws EvaluationException {
		DLBNode processedNode = new DLBNode();
		processedNode.setHeader(DLBNode.getHeader());
		DLBNodeBody processedBody = new DLBNodeBody();
		Map<String,Object> variables = new LinkedHashMap<>(
				dlbVariableStore.getModifiableMap(false,eventTime));
		DLBNode.getBody().execute(variables, true, processedBody);
		processedNode.setBody(processedBody);
		return processedNode;
	}
}
