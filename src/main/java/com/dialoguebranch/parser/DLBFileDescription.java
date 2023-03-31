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

/**
 * This class describes a DialogueBranch file. This can be a ".dlb" dialogue file or a
 * ".json" translation file.
 *
 * @author Dennis Hofs (RRD)
 */
public class DLBFileDescription {
	
	private String language;
	private String filePath;
	
	// -------------------- Constructors

	public DLBFileDescription() {	}

	/**
	 * Constructs a new description. The file can be a ".dlb" dialogue file or
	 * a ".json" translation file.
	 *
	 * @param language the language code (for example en_GB)
	 * @param filePath file path (.dlb or .json)
	 */
	public DLBFileDescription(String language, String filePath) {
		this.setLanguage(language);
		this.setFilePath(filePath);
	}
	
	// -------------------- Getters

	/**
	 * Return the language code (for example en_GB).
	 *
	 * @return the language code (for example en_GB)
	 */
	public String getLanguage() {
		return this.language;
	}

	/**
	 * Returns the file path. This can be a ".dlb" dialogue file or a ".json"
	 * translation file.
	 *
	 * @return the file path (.dlb or .json)
	 */
	public String getFilePath() {
		return this.filePath;
	}
	
	// -------------------- Setters

	/**
	 * Sets the language code (for example en_GB).
	 *
	 * @param language the language code (for example en_GB)
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * Sets the file path. This can be a ".dlb" dialogue file or a ".json"
	 * translation file.
	 *
	 * @param filePath the file path (.dlb or .json)
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		DLBFileDescription other = (DLBFileDescription)obj;
		if (!language.equals(other.language))
			return false;
		if (!filePath.equals(other.filePath))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		int result = language.hashCode();
		result = 31 * result + filePath.hashCode();
		return result;
	}

	public String toString() {
		String fileType;
		if (filePath.endsWith(".dlb"))
			fileType = "Dialogue file";
		else if (filePath.endsWith(".json"))
			fileType = "Translation file";
		else
			fileType = "Unknown file";
		return String.format("%s \"%s\" in language \"%s\"",
				fileType, filePath, language);
	}
}
