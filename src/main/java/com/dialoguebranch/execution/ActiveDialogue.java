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

	private final DLBFileDescription dialogueFileDescription;
	private final DLBDialogue dialogueDefinition;
	private DLBNode currentNode;
	private DLBVariableStore dlbVariableStore;

	// --------------------------------------------------------
	// -------------------- Constructor(s) --------------------
	// --------------------------------------------------------

	/**
	 * Creates an instance of an {@link ActiveDialogue} with a given {@link
	 * DLBFileDescription} and {@link DLBDialogue}.
	 *
	 * @param dialogueFileDescription the {@link DLBFileDescription} containing metadata of the
	 *                                dialogue file used in this {@link ActiveDialogue}.
	 * @param dialogueDefinition the dialogue definition
	 */
	public ActiveDialogue(DLBFileDescription dialogueFileDescription,
						  DLBDialogue dialogueDefinition) {
		this.dialogueFileDescription = dialogueFileDescription;
		this.dialogueDefinition = dialogueDefinition;
	}

	// -----------------------------------------------------------
	// -------------------- Getters & Setters --------------------
	// -----------------------------------------------------------

	/**
	 * Returns the {@link DLBFileDescription} of the dialogue file corresponding to this
	 * {@link ActiveDialogue} containing metadata for the file.
	 * @return the dialogue file description as a {@link DLBFileDescription}
	 */
	public DLBFileDescription getDialogueFileDescription() {
		return dialogueFileDescription;
	}

	/**
	 * Returns the {@link DLBDialogue} containing the definition of the dialogue being run through
	 * this {@link ActiveDialogue} object.
	 * @return the dialogue definition as a {@link DLBDialogue}
	 */
	public DLBDialogue getDialogueDefinition() {
		return dialogueDefinition;
	}

	/**
	 * Returns the "current node" (the current step in the active dialogue) as a {@link DLBNode}.
	 * @return the current step in the active dialogue as a {@link DLBNode}
	 */
	public DLBNode getCurrentNode() {
		return currentNode;
	}

	/**
	 * Sets in which node the current active dialogue is.
	 * @param currentNode the {@link DLBNode} currently being executed.
	 */
	public void setCurrentNode(DLBNode currentNode) {
		this.currentNode = currentNode;
	}

	/**
	 * Returns the {@link DLBVariableStore} associated with this {@link ActiveDialogue}.
	 * @return the {@link DLBVariableStore} associated with this {@link ActiveDialogue}.
	 */
	public DLBVariableStore getDLBVariableStore() {
		return dlbVariableStore;
	}

	/**
	 * Sets the {@link DLBVariableStore} used to store/retrieve parameters for this
	 * {@link ActiveDialogue}.
	 * @param dlbVariableStore the {@link DLBVariableStore} used to store/retrieve parameters for
	 *                         this {@link ActiveDialogue}.
	 */
	public void setDLBVariableStore(DLBVariableStore dlbVariableStore) {
		this.dlbVariableStore = dlbVariableStore;
	}

	// -------------------------------------------------------
	// -------------------- Other Methods --------------------
	// -------------------------------------------------------
	
	/**
	 * "Starts" this {@link ActiveDialogue}, returning the start node and updating its internal
	 * state.
	 *
	 * @param eventTime the timestamp (in the time zone of the user) of the event that triggered
	 *                  this start of the DialogueBranch dialogue.
	 * @return the initial {@link DLBNode}.
	 * @throws DLBException if the request is invalid
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	public DLBNode startDialogue(ZonedDateTime eventTime) throws DLBException, EvaluationException {
		return startDialogue(null, eventTime);
	}
	
	/**
	 * "Starts" this {@link ActiveDialogue} at the {@link DLBNode} represented by the provided
	 * {@code startNodeId}, or at the "Start" node of the dialogue if the given {@code startNodeId}
	 * is {@code null}, returning that node and updating the dialogue's internal state. If you set
	 * the {@code startNodeId} to null, it will return the start node.
	 *
	 * <p>If executed successfully, this method also sets {@code this.currentNode} and thus updates
	 * the value of {@link #getCurrentNode()}.</p>
	 *
	 * @param startNodeId the node ID of the node to start from, or {@code null} to start from the
	 *                    default "Start" node.
	 * @param eventTime the timestamp (in the time zone of the user) that triggered this start of
	 *                  execution of the dialogue
	 * @return the {@link DLBNode} object representing the given "starting node" after it has been
	 * 		   executed by the DialogueBranch parser (e.g. after control statements have been
	 * 		   resolved)
	 * @throws DLBException if the request is invalid
	 * @throws EvaluationException if an expression cannot be evaluated during execution of the node
	 */
	public DLBNode startDialogue(String startNodeId, ZonedDateTime eventTime)
			throws DLBException, EvaluationException {
		DLBNode nextNode;
		if (startNodeId == null) {
			nextNode = dialogueDefinition.getStartNode();
		} else {
			nextNode = dialogueDefinition.getNodeById(startNodeId);
			if (nextNode == null) {
				throw new DLBException(DLBException.Type.NODE_NOT_FOUND,
						String.format("Node \"%s\" not found in dialogue \"%s\"",
								startNodeId, dialogueDefinition.getDialogueName()));
			}
		}
		this.currentNode = executeDLBNode(nextNode,eventTime);
		return currentNode;
	}
	
	/**
	 * Retrieves the pointer to the next node based on the provided {@code replyId}. This might be a
	 * pointer to the end node. This method also performs any "set" actions associated with the
	 * reply.
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
			if (command instanceof DLBSetCommand setCommand) {
				setCommand.getExpression().evaluate(variableMap);
			}
		}
		return selectedDLBReply.getNodePointer();
	}
	
	/**
	 * Takes the next node pointer from the selected reply and determines the next node. The pointer
	 * might point to the end note, which means that there is no next node. If there is no next
	 * node, or the next node has no reply options, then the dialogue is considered finished.
	 * 
	 * <p>If there is a next node, then it returns the executed version of that next
	 * {@link DLBNode}.</p>
	 *  
	 * @param nodePointer the next node pointer from the selected reply
	 * @param eventTime the timestamp (in the time zone of the user) of the event that triggered the
	 *                  progressing of the dialogue
	 * @return the next {@link DLBNode} that follows on the selected reply or {@code null}
	 * @throws EvaluationException if an expression cannot be evaluated
	 */
	public DLBNode progressDialogue(DLBNodePointerInternal nodePointer, ZonedDateTime eventTime)
			throws EvaluationException {
		DLBNode nextNode = null;
		if (!nodePointer.getNodeId().equalsIgnoreCase(DLBConstants.DLB_NODE_END_ID))
			nextNode = dialogueDefinition.getNodeById(nodePointer.getNodeId());
		this.currentNode = nextNode;
		if (nextNode != null) this.currentNode = executeDLBNode(nextNode, eventTime);
		return this.currentNode;
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
	 * Returns the statement corresponding to a given {@code replyId}. In any given state of the
	 * conversation, the user's client may return a replyId corresponding to a specific reply option
	 * in the current node. This method retrieves the corresponding statement to that reply, which
	 * may be the statement as defined in the {@link DLBDialogue}, or it may be a constant defining
	 * that this was an "Auto Forward" reply without a specified statement (see
	 * {@link DLBConstants#DLB_REPLY_STATEMENT_AUTOFORWARD}).
	 *
	 * @param replyId the reply id as provided e.g. by a client application.
	 * @return the statement {@link String} corresponding to the reply identified by {@code replyId}
	 * @throws DLBException if no reply with the specified {@code replyId} is found
	 */
	public String getUserStatementFromReplyId(int replyId) throws DLBException {
		DLBReply selectedReply = currentNode.getBody().findReplyById(replyId);
		if (selectedReply == null) {
			throw new DLBException(DLBException.Type.REPLY_NOT_FOUND,
					String.format("Reply with ID %s not found in dialogue \"%s\", node \"%s\"",
					replyId, dialogueDefinition.getDialogueName(), currentNode.getTitle()));
		}
		if (selectedReply.getStatement() == null)
			return DLBConstants.DLB_REPLY_STATEMENT_AUTOFORWARD;
		StringBuilder result = new StringBuilder();
		List<DLBNodeBody.Segment> segments = selectedReply.getStatement()
				.getSegments();
		for (DLBNodeBody.Segment segment : segments) {
			if (segment instanceof DLBNodeBody.TextSegment textSegment) {
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
	 * Executes the agent statement and reply statements in the specified node. It executes "if",
	 * "random" and "set" commands and resolves variables. Any resulting body content that should be
	 * sent to the client, is added to the (agent or reply) statement body in the resulting node.
	 * This content can be text or client commands, with all variables resolved.
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
	 * Executes the agent statement and reply statements in the specified node. It executes "if" and
	 * "random" commands and resolves variables. Any resulting body content that should be sent to
	 * the client, is added to the (agent or reply) statement body in the resulting node. This
	 * content can be text or client commands, with all variables resolved.
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
