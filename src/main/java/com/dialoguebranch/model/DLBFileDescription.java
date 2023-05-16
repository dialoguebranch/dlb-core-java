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

import java.io.File;
import java.util.Objects;

/**
 * A {@link DLBFileDescription}
 */
public class DLBFileDescription {
	
	private String language;
	private String filePath;
	private DLBFileType fileType;
	
	// -------------------- Constructors

	public DLBFileDescription() {	}

	public DLBFileDescription(String language, String filePath, DLBFileType fileType) {
		this.setLanguage(language);
		this.setFilePath(filePath);
		this.fileType = fileType;
	}
	
	// -------------------- Getters

	public String getLanguage() {
		return this.language;
	}
	
	public String getFilePath() {
		return this.filePath;
	}

	public DLBFileType getFileType() {
		return fileType;
	}

	/**
	 * Returns the "Dialogue Name" associated with this {@link DLBFileDescription}, which is the
	 * relative path of the dialogue file (relative to the language folder), including the file
	 * name, without the extension. For example, for a .dlb script file located at /project-folder/
	 * en/subfolder/script.dlb, this method will return "subfolder/script".
	 * @return the uniquely identifying dialogue name.
	 */
	public String getDialogueName() {
		if (filePath.endsWith(".dlb")) {
			return filePath.substring(0,filePath.length() - 4);
		} else if(filePath.endsWith(".json")) {
			return filePath.substring(0, filePath.length() - 5);
		}
		else
			return filePath;
	}
	
	// -------------------- Setters

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void setFileType(DLBFileType fileType) {
		this.fileType = fileType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(language, filePath, fileType);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || obj.getClass() != getClass())
			return false;
		DLBFileDescription other = (DLBFileDescription)obj;
		if (!language.equals(other.language))
			return false;
		if (!filePath.equals(other.filePath))
			return false;
		if(!fileType.equals(fileType))
			return false;
		return true;
	}

	public String toString() {
		return "DialogueBranch File '" + this.getDialogueName() + "' in language '"
				+ this.getLanguage() + "' (" + this.getLanguage() + File.separator
				+ this.getFilePath() +").";
	}
}
