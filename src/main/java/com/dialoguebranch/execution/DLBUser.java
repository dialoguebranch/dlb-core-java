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

import java.time.ZoneId;

/**
 * A {@link DLBUser} represents the person that is interacting with a DialogueBranch dialogue.
 * The {@link DLBUser} has an identifier and a timezone in which he currently resides that
 * can be used to log events (dialogue history and the stored update times for DialogueBranch
 * variables).
 *
 * @author Harm op den Akker
 */
public class DLBUser {

	private String id;
	private ZoneId timeZone;

	/**
	 * Creates an instance of a {@link DLBUser} in the system's default time zone.
	 * @param id the username or identifier of the {@link DLBUser}.
	 */
	public DLBUser(String id) {
		this.id = id;
		this.timeZone = ZoneId.systemDefault();
	}

	public DLBUser(String id, ZoneId timeZone) {
		this.id = id;
		this.timeZone = timeZone;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the latest known time zone for this user as one of {@code TimeZone.getAvailableIDs()} (IANA Codes).
	 * @return the latest known time zone for this user as one of {@code TimeZone.getAvailableIDs()} (IANA Codes).
	 */
	public ZoneId getTimeZone() {
		return timeZone;
	}

	/**
	 * Sets the latest known time zone for this user as one of {@code TimeZone.getAvailableIDs()} (IANA Codes).
	 * @param timeZone the latest known time zone for this user as one of {@code TimeZone.getAvailableIDs()} (IANA Codes).
	 */
	public void setTimeZone(ZoneId timeZone) {
		this.timeZone = timeZone;
	}
}
