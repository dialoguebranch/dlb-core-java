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

import nl.rrd.utils.exception.ParseException;
import nl.rrd.utils.xml.AbstractSimpleSAXHandler;
import nl.rrd.utils.xml.SimpleSAXHandler;
import nl.rrd.utils.xml.XMLWriter;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link LanguageSet} is a mapping from a single source {@link Language} to a list of translation
 * {@link Language}s.
 *
 * @author Harm op den Akker (Fruit Tree Labs)
 */
public class LanguageSet {

	private Language sourceLanguage;
	private List<Language> translationLanguages;

	// --------------------------------------------------------
	// -------------------- Constructor(s) --------------------
	// --------------------------------------------------------

	public LanguageSet() {
		this.translationLanguages = new ArrayList<>();
	}

	public LanguageSet(Language sourceLanguage) {
		this.sourceLanguage = sourceLanguage;
		this.translationLanguages = new ArrayList<>();
	}

	// -----------------------------------------------------------
	// -------------------- Getters & Setters --------------------
	// -----------------------------------------------------------

	public Language getSourceLanguage() {
		return sourceLanguage;
	}

	public void setSourceLanguage(Language sourceLanguage) {
		this.sourceLanguage = sourceLanguage;
	}

	public List<Language> getTranslationLanguages() {
		return translationLanguages;
	}

	public void setTranslationLanguages(List<Language> translationLanguages) {
		this.translationLanguages = translationLanguages;
	}

	// -------------------------------------------------------
	// -------------------- Other Methods --------------------
	// -------------------------------------------------------

	public void addTranslationLanguage(Language translationLanguage) {
		translationLanguages.add(translationLanguage);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("LanguageSet: \n");
		result.append("[SourceLanguage:").append(sourceLanguage.toString()).append("]\n");
		for(Language language : translationLanguages) {
			result.append(language.toString()).append("\n");
		}
		return result.toString();
	}

	// ------------------------------------------------------
	// -------------------- XML Handling --------------------
	// ------------------------------------------------------

	public void writeXML(XMLWriter writer) throws IOException {
		writer.writeStartElement("language-set");

		writer.writeStartElement("source-language");
		writer.writeAttribute("name",sourceLanguage.getName());
		writer.writeAttribute("code",sourceLanguage.getCode());
		writer.writeEndElement(); // source-language

		for(Language language : translationLanguages) {
			writer.writeStartElement("translation-language");
			writer.writeAttribute("name",language.getName());
			writer.writeAttribute("code",language.getCode());
			writer.writeEndElement();
		}

		writer.writeEndElement(); // language-set
	}

	public static SimpleSAXHandler<LanguageSet> getXMLHandler() {
		return new XMLHandler();
	}

	private static class XMLHandler extends AbstractSimpleSAXHandler<LanguageSet> {

		private LanguageSet result;
		private SimpleSAXHandler<Language> languageHandler = null;

		@Override
		public void startElement(String name, Attributes attributes, List<String> parents)
				throws ParseException {
			if(name.equals("language-set")) {
				result = new LanguageSet();
			} else if(name.equals("source-language") || name.equals("translation-language")) {
				languageHandler = Language.getXMLHandler();
				languageHandler.startElement(name,attributes,parents);
			} else {
				if(languageHandler != null) languageHandler.startElement(name,attributes,parents);
			}
		}

		@Override
		public void endElement(String name, List<String> parents) throws ParseException {
			if(languageHandler != null) languageHandler.endElement(name,parents);
			if(name.equals("source-language") && languageHandler != null) {
				Language sourceLanguage = languageHandler.getObject();
				result.setSourceLanguage(sourceLanguage);
				languageHandler = null;
			} else if(name.equals("translation-language") && languageHandler != null) {
				Language translationLanguage = languageHandler.getObject();
				result.addTranslationLanguage(translationLanguage);
				languageHandler = null;
			}
		}

		@Override
		public void characters(String ch, List<String> parents) throws ParseException {

		}

		@Override
		public LanguageSet getObject() {
			return result;
		}
	}
}