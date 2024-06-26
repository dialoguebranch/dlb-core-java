/*
 *
 *                Copyright (c) 2023-2024 Fruit Tree Labs (www.fruittreelabs.com)
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

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link LanguageMap} is a wrapper object containing a {@link List} of
 * {@link LanguageSet}s, as well as some convenience methods for manipulating
 * {@link LanguageSet}s.
 *
 * @author Harm op den Akker (Fruit Tree Labs)
 */
public class LanguageMap {

	private List<LanguageSet> languageSets;

	// -------------------------------------------------------
	// --------------------Constructor(s) --------------------
	// -------------------------------------------------------

	/**
	 * Creates an instance of an empty {@link LanguageMap}.
	 */
	public LanguageMap() {
		languageSets = new ArrayList<>();
	}

	/**
	 * Creates an instance of a {@link LanguageMap} with a given list of {@link LanguageSet}s.
	 * @param languageSets a list of {@link LanguageSet}s contained in this {@link LanguageMap}.
	 */
	public LanguageMap(List<LanguageSet> languageSets) {
		this.languageSets = languageSets;
	}

	// -----------------------------------------------------------
	// -------------------- Getters & Setters --------------------
	// -----------------------------------------------------------

	/**
	 * Returns the {@link List} of {@link LanguageSet}s in this {@link LanguageMap}.
	 * @return the {@link List} of {@link LanguageSet}s in this {@link LanguageMap}.
	 */
	public List<LanguageSet> getLanguageSets() {
		return languageSets;
	}

	/**
	 * Sets the {@link List} of {@link LanguageSet}s for this {@link LanguageMap}.
	 * @param languageSets the {@link List} of {@link LanguageSet}s for this
	 *                     {@link LanguageMap}.
	 */
	public void setLanguageSets(List<LanguageSet> languageSets) {
		this.languageSets = languageSets;
	}

	// -------------------------------------------------------
	// -------------------- Other Methods --------------------
	// -------------------------------------------------------

	/**
	 * Adds the given {@link LanguageSet} to this {@link LanguageMap}.
	 * @param languageSet the {@link LanguageSet} to add to this {@link LanguageMap}.
	 */
	public void addLanguageSet(LanguageSet languageSet) {
		languageSets.add(languageSet);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("LanguageMap: \n");
		for(LanguageSet languageSet : languageSets) {
			result.append(languageSet.toString());
		}
		return result.toString();
	}

}
