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

import com.dialoguebranch.execution.ActiveDialogue;

public class DialogueState {
	private DLBFileDescription dialogueDescription;
	private DLBDialogue dialogueDefinition;
	private DLBLoggedDialogue loggedDialogue;
	private int loggedInteractionIndex;
	private ActiveDialogue activeDialogue;

	public DialogueState(DLBFileDescription dialogueDescription,
						 DLBDialogue dialogueDefinition, DLBLoggedDialogue loggedDialogue,
						 int loggedInteractionIndex, ActiveDialogue activeDialogue) {
		this.dialogueDescription = dialogueDescription;
		this.dialogueDefinition = dialogueDefinition;
		this.loggedDialogue = loggedDialogue;
		this.loggedInteractionIndex = loggedInteractionIndex;
		this.activeDialogue = activeDialogue;
	}

	public DLBFileDescription getDialogueDescription() {
		return dialogueDescription;
	}

	public DLBDialogue getDialogueDefinition() {
		return dialogueDefinition;
	}

	public DLBLoggedDialogue getLoggedDialogue() {
		return loggedDialogue;
	}

	public int getLoggedInteractionIndex() {
		return loggedInteractionIndex;
	}

	public ActiveDialogue getActiveDialogue() {
		return activeDialogue;
	}
}
