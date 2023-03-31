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

package com.dialoguebranch.model.language;

import nl.rrd.utils.exception.ParseException;
import nl.rrd.utils.xml.AbstractSimpleSAXHandler;
import nl.rrd.utils.xml.SimpleSAXHandler;
import nl.rrd.utils.xml.XMLWriter;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Harm op den Akker
 */
public class DLBLanguageSet {

	private DLBLanguage sourceLanguage;
	private List<DLBLanguage> translationLanguages;

	// ----- Constructors

	public DLBLanguageSet() {
		this.translationLanguages = new ArrayList<DLBLanguage>();
	}

	public DLBLanguageSet(DLBLanguage sourceLanguage) {
		this.sourceLanguage = sourceLanguage;
		this.translationLanguages = new ArrayList<DLBLanguage>();
	}

	// ----- Getters

	public DLBLanguage getSourceLanguage() {
		return sourceLanguage;
	}

	public List<DLBLanguage> getTranslationLanguages() {
		return translationLanguages;
	}

	// ----- Setters

	public void setSourceLanguage(DLBLanguage sourceLanguage) {
		this.sourceLanguage = sourceLanguage;
	}

	public void setTranslationLanguages(List<DLBLanguage> translationLanguages) {
		this.translationLanguages = translationLanguages;
	}

	// ----- Methods

	public void addTranslationLanguage(DLBLanguage translationLanguage) {
		translationLanguages.add(translationLanguage);
	}

	public String toString() {
		String result = "DLBLanguageSet: \n";
		result += "[SourceLanguage:"+sourceLanguage.toString()+"]\n";
		for(DLBLanguage DLBLanguage : translationLanguages) {
			result += DLBLanguage.toString()+"\n";
		}
		return result;
	}

	// ----- XML Handling

	public void writeXML(XMLWriter writer) throws IOException {
		writer.writeStartElement("language-set");

		writer.writeStartElement("source-language");
		writer.writeAttribute("name",sourceLanguage.getName());
		writer.writeAttribute("code",sourceLanguage.getCode());
		writer.writeEndElement(); // source-language

		for(DLBLanguage language : translationLanguages) {
			writer.writeStartElement("translation-language");
			writer.writeAttribute("name",language.getName());
			writer.writeAttribute("code",language.getCode());
			writer.writeEndElement();
		}

		writer.writeEndElement(); // language-set
	}

	public static SimpleSAXHandler<DLBLanguageSet> getXMLHandler() {
		return new XMLHandler();
	}

	private static class XMLHandler extends AbstractSimpleSAXHandler<DLBLanguageSet> {

		private DLBLanguageSet result;
		private SimpleSAXHandler<DLBLanguage> languageHandler = null;

		@Override
		public void startElement(String name, Attributes atts, List<String> parents) throws ParseException {
			if(name.equals("language-set")) {
				result = new DLBLanguageSet();
			} else if(name.equals("source-language") || name.equals("translation-language")) {
				languageHandler = DLBLanguage.getXMLHandler();
				languageHandler.startElement(name,atts,parents);
			} else {
				if(languageHandler != null) languageHandler.startElement(name,atts,parents);
			}
		}

		@Override
		public void endElement(String name, List<String> parents) throws ParseException {
			if(languageHandler != null) languageHandler.endElement(name,parents);
			if(name.equals("source-language")) {
				DLBLanguage sourceLanguage = languageHandler.getObject();
				result.setSourceLanguage(sourceLanguage);
				languageHandler = null;
			} else if(name.equals("translation-language")) {
				DLBLanguage translationLanguage = languageHandler.getObject();
				result.addTranslationLanguage(translationLanguage);
				languageHandler = null;
			}
		}

		@Override
		public void characters(String ch, List<String> parents) throws ParseException {

		}

		@Override
		public DLBLanguageSet getObject() {
			return result;
		}
	}
}
