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
 * A {@link DLBFileDescription} contains metadata for a Dialogue Branch file that can either be a
 * Script file (.dlb) or a Translation file (.json). It has three properties:
 * <ul>
 *     <li>{@code language} - the name of the language folder in which the file was found</li>
 *     <li>{@code filePath} - the complete path to the file, relative to the language folder,
 *     including the file extension</li>
 *     <li>{@code fileType} - either {@link DLBFileType#SCRIPT} or
 *     {@link DLBFileType#TRANSLATION}</li> indicating the type of the file
 * </ul>
 *
 * Additionally, one can obtain the unique "dialogue identifier" by calling
 * {@link DLBFileDescription#getDialogueName()} which will return the name of the dialogue without
 * its file extension (but including any subdirectories under the language directory in which it
 * resides).
 *
 * @author Harm op den Akker (Fruit Tree Labs - www.fruittreelabs.com).
 */
public class DLBFileDescription {
	
	private String language;
	private String filePath;
	private DLBFileType fileType;

	// --------------------------------------------------------
	// -------------------- Constructor(s) --------------------
	// --------------------------------------------------------

	/**
	 * Creates an empty instance of a {@link DLBFileDescription}.
	 */
	public DLBFileDescription() {	}

	/**
	 * Creates an instance of a {@link DLBFileDescription} with a given {@code language},
	 * {@code filePath} and {@code fileType}.
	 * @param language the name of the "language directory", which is the direct subdirectory of the
	 *                 project's root directory (e.g. "en" - for English).
	 * @param filePath the path to the file, relative to the language directory and including the
	 *                 file extension (e.g. "subdirectory/basic.dlb").
	 * @param fileType the type of the file as either {@link DLBFileType#SCRIPT} or
	 *                 {@link DLBFileType#TRANSLATION}.
	 */
	public DLBFileDescription(String language, String filePath, DLBFileType fileType) {
		this.setLanguage(language);
		this.setFilePath(filePath);
		this.fileType = fileType;
	}

	// -----------------------------------------------------------
	// -------------------- Getters & Setters --------------------
	// -----------------------------------------------------------

	/**
	 * Returns the language of this {@link DLBFileDescription}, which is the name of the "language
	 * directory", which is the direct subdirectory of the project's root directory (e.g. "en" - for
	 * English).
	 * @return the language of this {@link DLBFileDescription}.
	 */
	public String getLanguage() {
		return this.language;
	}

	/**
	 * Sets the language of this {@link DLBFileDescription}, which is the name of the "language
	 * directory", which is the direct subdirectory of the project's root directory (e.g. "en" - for
	 * English).
	 * @param language the language of this {@link DLBFileDescription}.
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * Returns the file path of this {@link DLBFileDescription} which is the path to the file,
	 * relative to the language directory and including the file extension
	 * (e.g. "subdirectory/basic.dlb").
	 * @return the file path of this {@link DLBFileDescription}.
	 */
	public String getFilePath() {
		return this.filePath;
	}

	/**
	 * Sets the file path of this {@link DLBFileDescription} which is the path to the file,
	 * relative to the language directory and including the file extension
	 * (e.g. "subdirectory/basic.dlb").
	 * @param filePath the file path of this {@link DLBFileDescription}.
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * Returns the type of this {@link DLBFileDescription} as either {@link DLBFileType#SCRIPT} or
	 * {@link DLBFileType#TRANSLATION}.
	 * @return the type of this {@link DLBFileDescription}.
	 */
	public DLBFileType getFileType() {
		return fileType;
	}

	/**
	 * Sets the type of this {@link DLBFileDescription} as either {@link DLBFileType#SCRIPT} or
	 * {@link DLBFileType#TRANSLATION}.
	 * @param fileType the type of this {@link DLBFileDescription}.
	 */
	public void setFileType(DLBFileType fileType) {
		this.fileType = fileType;
	}

	// -------------------------------------------------------
	// -------------------- Other Methods --------------------
	// -------------------------------------------------------

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
		return fileType.equals(other.fileType);
	}

	@Override
	public String toString() {
		return "DialogueBranch File '" + this.getDialogueName() + "' in language '"
			+ this.getLanguage() + "' (" + this.getLanguage() + File.separator
			+ this.getFilePath() +").";
	}
}
