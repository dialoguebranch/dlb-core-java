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
import org.xml.sax.Attributes;

import java.util.List;

/**
 * A {@link DLBLanguage} defines a language used in a DialogueBranch project with a
 * given name and language code. The 'code' is preferably a specific ISO3 code, an ISO1 code,
 * or for languages that don't actually exist (e.g. Klingon, Orcish) a made up code
 * that is not assigned to any existing language. These codes must be unique within
 * a given DialogueBranch project.
 *
 * @author Harm op den Akker
 */
public class DLBLanguage {

	private String name;
	private String code;

	// ----- Constructors

	/**
	 * Creates an empty instance of a {@link DLBLanguage}.
	 */
	public DLBLanguage() { }

	/**
	 * Creates an instance of a {@link DLBLanguage} with given {@code name} and {@code code}.
	 * @param name the name of the language
	 * @param code the code (ISO3, ISO1, or made-up) for this {@link DLBLanguage}.
	 */
	public DLBLanguage(String name, String code) {
		this.name = name;
		this.code = code;
	}

	// ----- Getters

	/**
	 * Returns the name of this {@link DLBLanguage}.
	 * @return the name of this {@link DLBLanguage}.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the code of this {@link DLBLanguage}.
	 * @return the code of this {@link DLBLanguage}.
	 */
	public String getCode() {
		return code;
	}

	// ----- Setters

	/**
	 * Sets the name of this {@link DLBLanguage}.
	 * @param name the name of this {@link DLBLanguage}.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the code of this {@link DLBLanguage}.
	 * @param code the code of this {@link DLBLanguage}.
	 */
	public void setCode(String code) {
		this.code = code;
	}

	// ----- Methods

	public String toString() {
		return "[name:"+name+"] [code:"+code+"]";
	}

	// ----- XML Handling

	public static SimpleSAXHandler<DLBLanguage> getXMLHandler() {
		return new XMLHandler();
	}

	private static class XMLHandler extends AbstractSimpleSAXHandler<DLBLanguage> {

		private DLBLanguage result;

		@Override
		public void startElement(String name, Attributes atts, List<String> parents) throws ParseException {
			if(name.equals("source-language") || name.equals("translation-language")) {
				result = new DLBLanguage();
				result.setCode(atts.getValue("code"));
				result.setName(atts.getValue("name"));
			}
		}

		@Override
		public void endElement(String name, List<String> parents) throws ParseException {

		}

		@Override
		public void characters(String ch, List<String> parents) throws ParseException {

		}

		@Override
		public DLBLanguage getObject() {
			return result;
		}
	}
}
