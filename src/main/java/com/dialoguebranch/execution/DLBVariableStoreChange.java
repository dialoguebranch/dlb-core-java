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

import java.time.ZonedDateTime;
import java.util.*;

public abstract class DLBVariableStoreChange {

	/**
	 * Defines a set of possible source of changes to the DLBVariableStore.
	 */
	public enum Source {
		UNKNOWN,
		DLB_SCRIPT,
		INPUT_REPLY,
		WEB_SERVICE,
		EXTERNAL_VARIABLE_SERVICE
	}

	private final ZonedDateTime time;
	private final Source source;

	/**
	 * Creates an instance of a {@link DLBVariableStoreChange} with a given {@code time} and
	 * {@code source}.
	 * @param time the time that this change took place (in the time zone of the user).
	 * @param source the source of the {@link DLBVariableStoreChange}, as one of {@link Source}.
	 */
	public DLBVariableStoreChange(ZonedDateTime time, Source source) {
		this.time = time;
		this.source = source;
	}

	/**
	 * Returns the {@link Source} of this change to the DLBVariableStore.
	 * @return the {@link Source} of this change to the DLBVariableStore.
	 */
	public Source getSource() {
		return source;
	}

	/**
	 * Returns the time that this change took place (in the time zone of the user).
	 * @return the time that this change took place (in the time zone of the user).
	 */
	public ZonedDateTime getTime() {
		return time;
	}

	/**
	 * An implementation of {@link DLBVariableStoreChange} representing a set of added
	 * {@link DLBVariable}s.
	 *
	 * @author Dennis Hofs
	 * @author Harm op den Akker
	 */
	public static class Put extends DLBVariableStoreChange {
		private final Map<String,Object> variables;

		/**
		 * Creates an instance of a {@link Put} {@link DLBVariableStoreChange} with a given map of
		 * {@link DLBVariable}s.
		 * @param dlbVariablesMap the mapping from variable name to {@link DLBVariable} that was
		 *                         added in this {@link DLBVariableStoreChange}.
		 * @param time the time that this change took place (in the time zone of the user).
		 */
		public Put(Map<String, DLBVariable> dlbVariablesMap, ZonedDateTime time) {
			super(time,Source.UNKNOWN);
			variables = new LinkedHashMap<>();
			for(DLBVariable DLBVariable : dlbVariablesMap.values()) {
				variables.put(DLBVariable.getName(), DLBVariable.getValue());
			}
		}

		/**
		 * Creates an instance of a {@link Put} {@link DLBVariableStoreChange} with a given map of
		 * {@link DLBVariable}s.
		 * @param dlbVariablesMap the mapping from variable name to {@link DLBVariable} that was
		 *                         added in this {@link DLBVariableStoreChange}.
		 * @param time the time that this change took place (in the time zone of the user).
		 * @param source the source of the change to the variable store.
		 */
		public Put(Map<String, DLBVariable> dlbVariablesMap, ZonedDateTime time, Source source) {
			super(time,source);
			variables = new LinkedHashMap<>();
			for(DLBVariable DLBVariable : dlbVariablesMap.values()) {
				variables.put(DLBVariable.getName(), DLBVariable.getValue());
			}
		}

		/**
		 * Creates an instance of a {@link Put} {@link DLBVariableStoreChange} with a given
		 * {@code variableName}, {@code variableValues}, and {@code lastUpdated} timestamp in the
		 * timezone of the DialogueBranch user.
		 * @param variableName the name of the {@link DLBVariable} representing this Put change.
		 * @param variableValue the value of the {@link DLBVariable} representing this Put change.
		 * @param time the time that this change took place (in the time zone of the user).
		 */
		public Put(String variableName, Object variableValue, ZonedDateTime time) {
			super(time, Source.UNKNOWN);
			variables = new LinkedHashMap<>();
			variables.put(variableName, variableValue);
		}

		/**
		 * Creates an instance of a {@link Put} {@link DLBVariableStoreChange} with a given
		 * {@code variableName}, {@code variableValues}, and {@code lastUpdated} timestamp in the
		 * timezone of the DialogueBranch user.
		 * @param variableName the name of the {@link DLBVariable} representing this Put change.
		 * @param variableValue the value of the {@link DLBVariable} representing this Put change.
		 * @param time the time that this change took place (in the time zone of the user).
		 * @param source the source of the change to the variable store.
		 */
		public Put(String variableName, Object variableValue, ZonedDateTime time, Source source) {
			super(time, source);
			variables = new LinkedHashMap<>();
			variables.put(variableName, variableValue);
		}

		/**
		 * Creates an instance of a {@link Put} {@link DLBVariableStoreChange} with a single given
		 * {@link DLBVariable}.
		 * @param DLBVariable the one and only {@link DLBVariable} that was added in this
		 *                     {@link DLBVariableStoreChange}.
		 * @param time the time that this change took place (in the time zone of the user).
		 */
		public Put(DLBVariable DLBVariable, ZonedDateTime time) {
			super(time, Source.UNKNOWN);
			variables = new LinkedHashMap<>();
			variables.put(DLBVariable.getName(), DLBVariable.getValue());
		}

		/**
		 * Creates an instance of a {@link Put} {@link DLBVariableStoreChange} with a single given
		 * {@link DLBVariable}.
		 * @param DLBVariable the one and only {@link DLBVariable} that was added in this
		 *                     {@link DLBVariableStoreChange}.
		 * @param time the time that this change took place (in the time zone of the user).
		 * @param source the source of the change to the variable store.
		 */
		public Put(DLBVariable DLBVariable, ZonedDateTime time, Source source) {
			super(time, source);
			variables = new LinkedHashMap<>();
			variables.put(DLBVariable.getName(), DLBVariable.getValue());
		}

		/**
		 * Creates an instance of a {@link Put} {@link DLBVariableStoreChange} with a list of given
		 * {@link DLBVariable}s.
		 * @param DLBVariablesList the list of {@link DLBVariable}s that were added in this
		 *                          {@link DLBVariableStoreChange}.
		 * @param time the time that this change took place (in the time zone of the user).
		 */
		public Put(List<DLBVariable> DLBVariablesList, ZonedDateTime time) {
			super(time, Source.UNKNOWN);
			variables = new LinkedHashMap<>();
			for(DLBVariable DLBVariable : DLBVariablesList) {
				variables.put(DLBVariable.getName(), DLBVariable.getValue());
			}
		}

		/**
		 * Creates an instance of a {@link Put} {@link DLBVariableStoreChange} with a list of given
		 * {@link DLBVariable}s.
		 * @param DLBVariablesList the list of {@link DLBVariable}s that were added in this
		 *                          {@link DLBVariableStoreChange}.
		 * @param time the time that this change took place (in the time zone of the user).
		 * @param source the source of the change to the variable store.
		 */
		public Put(List<DLBVariable> DLBVariablesList, ZonedDateTime time, Source source) {
			super(time, source);
			variables = new LinkedHashMap<>();
			for(DLBVariable DLBVariable : DLBVariablesList) {
				variables.put(DLBVariable.getName(), DLBVariable.getValue());
			}
		}

		/**
		 * Returns the mapping of variable name to value ({@link Object}) representing all the
		 * variables that have been added in this {@link DLBVariableStoreChange}.
		 * @return the added DialogueBranch Variables.
		 */
		public Map<String,Object> getVariables() {
			return variables;
		}

	}

	/**
	 * An implementation of {@link DLBVariableStoreChange} representing a set of removed DialogueBranch
	 * Variables, identified by their variable names.
	 *
	 * @author Dennis Hofs
	 * @author Harm op den Akker
	 */
	public static class Remove extends DLBVariableStoreChange {
		private final Collection<String> removedVariableNames;

		/**
		 * Creates an instance of a {@link Remove} {@link DLBVariableStoreChange} with a given
		 * collection of variableNames.
		 * @param variableNames the names of the variables that have been removed in this change.
		 * @param time the time that this change took place (in the time zone of the user).
		 */
		public Remove(Collection<String> variableNames, ZonedDateTime time) {
			super(time, Source.UNKNOWN);
			this.removedVariableNames = variableNames;
		}

		/**
		 * Creates an instance of a {@link Remove} {@link DLBVariableStoreChange} with a given
		 * collection of variableNames.
		 * @param variableNames the names of the variables that have been removed in this change.
		 * @param time the time that this change took place (in the time zone of the user).
		 * @param source the source of the change to the variable store.
		 */
		public Remove(Collection<String> variableNames, ZonedDateTime time, Source source) {
			super(time, source);
			this.removedVariableNames = variableNames;
		}

		/**
		 * Creates an instance of a {@link Remove} {@link DLBVariableStoreChange} with a given
		 * single variable name, representing the variable that was removed with this change.
		 * @param variableName the name of the variable that was removed with this change.
		 * @param time the time that this change took place (in the time zone of the user).
		 */
		public Remove(String variableName, ZonedDateTime time) {
			super(time, Source.UNKNOWN);
			removedVariableNames = Collections.singletonList(variableName);
		}

		/**
		 * Creates an instance of a {@link Remove} {@link DLBVariableStoreChange} with a given
		 * single variable name, representing the variable that was removed with this change.
		 * @param variableName the name of the variable that was removed with this change.
		 * @param time the time that this change took place (in the time zone of the user).
		 * @param source the source of the change to the variable store.
		 */
		public Remove(String variableName, ZonedDateTime time, Source source) {
			super(time, source);
			removedVariableNames = Collections.singletonList(variableName);
		}

		/**
		 * Returns the collection of variable names that are associated with this {@link Remove}
		 * {@link DLBVariableStoreChange}.
		 * @return the collection of variable names of variables that have been removed.
		 */
		public Collection<String> getVariableNames() {
			return removedVariableNames;
		}

	}

	/**
	 * An implementation of {@link DLBVariableStoreChange} representing a full clear of the
	 * {@link DLBVariableStore}.
	 *
	 * @author Dennis Hofs
	 * @author Harm op den Akker
	 */
	public static class Clear extends DLBVariableStoreChange {

		/**
		 * Creates an instance of a {@link Clear} {@link DLBVariableStoreChange} indicating a full
		 * clear (removed all variables) of the DialogueBranch Variable Store.
		 * @param time the time that this change took place (in the time zone of the user).
		 */
		public Clear(ZonedDateTime time) {
			super(time,Source.UNKNOWN);
		}

		/**
		 * Creates an instance of a {@link Clear} {@link DLBVariableStoreChange} indicating a full
		 * clear (removed all variables) of the DialogueBranch Variable Store.
		 * @param time the time that this change took place (in the time zone of the user).
		 * @param source the source of the change to the variable store.
		 */
		public Clear(ZonedDateTime time, Source source) {
			super(time,source);
		}

	}
}
