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
 * A {@link DLBLanguageMap} is a wrapper object containing a {@link List} of {@link DLBLanguageSet}s,
 * as well as some convenience methods for manipulating {@link DLBLanguageSet}s.
 *
 * @author Harm op den Akker
 */
public class DLBLanguageMap {

	private List<DLBLanguageSet> languageSets;

	// ----- Constructors

	/**
	 * Creates an instance of an empty {@link DLBLanguageMap}.
	 */
	public DLBLanguageMap() {
		languageSets = new ArrayList<>();
	}

	/**
	 * Creates an instance of a {@link DLBLanguageMap} with a given {@link DLBLanguageSet}.
	 * @param languageSets a list of {@link DLBLanguageSet}s contained in this {@link DLBLanguageMap}.
	 */
	public DLBLanguageMap(List<DLBLanguageSet> languageSets) {
		this.languageSets = languageSets;
	}

	// ----- Getters

	/**
	 * Returns the {@link List} of {@link DLBLanguageSet}s in this {@link DLBLanguageMap}.
	 * @return the {@link List} of {@link DLBLanguageSet}s in this {@link DLBLanguageMap}.
	 */
	public List<DLBLanguageSet> getLanguageSets() {
		return languageSets;
	}

	// ----- Setters

	/**
	 * Sets the {@link List} of {@link DLBLanguageSet}s for this {@link DLBLanguageMap}.
	 * @param languageSets the {@link List} of {@link DLBLanguageSet}s for this {@link DLBLanguageMap}.
	 */
	public void setLanguageSets(List<DLBLanguageSet> languageSets) {
		this.languageSets = languageSets;
	}

	// ----- Methods

	/**
	 * Adds the given {@link DLBLanguageSet} to this {@link DLBLanguageMap}.
	 * @param DLBLanguageSet the {@link DLBLanguageSet} to add to this {@link DLBLanguageMap}.
	 */
	public void addLanguageSet(DLBLanguageSet DLBLanguageSet) {
		languageSets.add(DLBLanguageSet);
	}

	public String toString() {
		String result = "DLBLanguageMap: \n";
		for(DLBLanguageSet wls : languageSets) {
			result += wls.toString();
		}
		return result;
	}

	// ----- XML Handling

	public void writeXML(XMLWriter writer) throws IOException {
		writer.writeStartElement("language-map");

		for(DLBLanguageSet languageSet : languageSets) {
			languageSet.writeXML(writer);
		}

		writer.writeEndElement(); // language-map
	}

	public static SimpleSAXHandler<DLBLanguageMap> getXMLHandler() {
		return new XMLHandler();
	}

	private static class XMLHandler extends AbstractSimpleSAXHandler<DLBLanguageMap> {

		private DLBLanguageMap result = null;
		private SimpleSAXHandler<DLBLanguageSet> languageSetHandler = null;

		@Override
		public void startElement(String name, Attributes atts, List<String> parents) throws ParseException {
			if(name.equals("language-map")) {
				result = new DLBLanguageMap();
			} else if(name.equals("language-set")) {
				languageSetHandler = DLBLanguageSet.getXMLHandler();
				languageSetHandler.startElement(name,atts,parents);
			} else {
				if(languageSetHandler != null) languageSetHandler.startElement(name,atts,parents);
			}
		}

		@Override
		public void endElement(String name, List<String> parents) throws ParseException {
			if(languageSetHandler != null) languageSetHandler.endElement(name,parents);
			if(name.equals("language-set")) {
				DLBLanguageSet languageSet = languageSetHandler.getObject();
				result.addLanguageSet(languageSet);
				languageSetHandler = null;
			}
		}

		@Override
		public void characters(String ch, List<String> parents) throws ParseException {

		}

		@Override
		public DLBLanguageMap getObject() {
			return result;
		}
	}
}
