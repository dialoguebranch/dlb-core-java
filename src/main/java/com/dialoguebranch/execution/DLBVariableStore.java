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

/**
 * A {@link DLBVariableStore} is an object that stores all DialogueBranch variable values for a given
 * user.
 * 
 * @author Harm op den Akker
 */
public class DLBVariableStore {

	// Contains the list of all DLBVariables in this store
	private final Map<String, DLBVariable> dlbVariables = new HashMap<>();

	// The DialogueBranch user associated with this DLBVariableStore
	private DLBUser DLBUser;

	// Contains the list of all DLBVariableChangeListeners that need to be notified for updates
	private final List<DLBVariableStoreOnChangeListener> onChangeListeners = new ArrayList<>();

	// --------------------------------------------------------
	// -------------------- Constructor(s) --------------------
	// --------------------------------------------------------

	/**
	 * Creates an instance of a new {@link DLBVariableStore} for a user in the given
	 * {@code timeZone}.
	 * @param DLBUser the {@link DLBUser} associated with this {@link DLBVariableStore}.
	 */
	public DLBVariableStore(DLBUser DLBUser) {
		this.DLBUser = DLBUser;
	}

	public DLBVariableStore(DLBUser DLBUser, DLBVariable[] DLBVariableArray) {
		this.DLBUser = DLBUser;
		synchronized(dlbVariables) {
			for (DLBVariable variable : DLBVariableArray) {
				dlbVariables.put(variable.getName(),variable);
			}
		}
	}

	// ----------------------------------------------------------
	// -------------------- Listener Methods --------------------
	// ----------------------------------------------------------

	/**
	 * Adds the given {@link DLBVariableStoreOnChangeListener} to the list of listeners
	 * for this {@link DLBVariableStore}.
	 *
	 * @param listener a {@link DLBVariableStoreOnChangeListener} that should be notified whenever
	 *                 this {@link DLBVariableStore} is changed
	 */
	public void addOnChangeListener(DLBVariableStoreOnChangeListener listener) {
		synchronized (onChangeListeners) {
			onChangeListeners.add(listener);
		}
	}

	/**
	 * Removes the given {@link DLBVariableStoreOnChangeListener} from the list of listeners
	 * for this {@link DLBVariableStore}.
	 *
	 * @param listener a {@link DLBVariableStoreOnChangeListener} that was previously registered
	 *                 to listen for changes.
	 * @return {@code true} if the given {@link DLBVariableStoreOnChangeListener} was removed, or
	 *         {@code false} otherwise.
	 * if it was not registered as a listener to begin with.
	 */
	public boolean removeOnChangeListener(DLBVariableStoreOnChangeListener listener) {
		synchronized (onChangeListeners) {
			return onChangeListeners.remove(listener);
		}
	}

	/**
	 * Notifies all {@link DLBVariableStoreOnChangeListener} that are listening for changes to this
	 * {@link DLBVariableStore} of one or more changes as represented by the list of
	 * {@link DLBVariableStoreChange} {@code changes}.
	 *
	 * @param changes one or multiple {@link DLBVariableStoreChange}s representing a modification
	 *                to this {@link DLBVariableStore}.
	 */
	private void notifyOnChange(DLBVariableStoreChange... changes) {
		List<DLBVariableStoreOnChangeListener> listeners;
		synchronized (onChangeListeners) {
			listeners = new ArrayList<>(onChangeListeners);
		}
		for (DLBVariableStoreOnChangeListener listener : listeners) {
			listener.onChange(this, Arrays.asList(changes));
		}
	}

	// -----------------------------------------------------------
	// -------------------- Retrieval Methods --------------------
	// -----------------------------------------------------------

	/**
	 * Retrieves the variable identified by the given {@code name}, or returns
	 * {@code null} if no such variable is known in this {@link DLBVariableStore}.
	 *
	 * @param name the name of the variable to retrieve.
	 * @return the {@link DLBVariable} with the given {@code name}, nor {@code null}.
	 */
	public DLBVariable getDLBVariable(String name) {
		synchronized (dlbVariables) {
			return dlbVariables.get(name);
		}
	}

	/**
	 * Returns the contents of this {@link DLBVariableStore} as an array of {@link DLBVariable}s.
	 * @return the contents of this {@link DLBVariableStore} as an array of {@link DLBVariable}s.
	 */
	public DLBVariable[] getDLBVariables() {
		synchronized (dlbVariables) {
			return dlbVariables.values().toArray(new DLBVariable[0]);
		}
	}

	/**
	 * Returns the value of the variable identified by the given {@code name}.
	 * If no such variable is known in this {@link DLBVariableStore}, then this
	 * method returns null.
	 *
	 * <p>Note: if this method returns null, it can mean that the variable does
	 * not exist, or that the variable has value {@code null}. If you need to
	 * distinguish these two cases, you should call {@link
	 * #getDLBVariable(String) getDLBVariable()} </p>
	 *
	 * @param variableName the name of the variable to retrieve.
	 * @return the value of the variable, null if the variable does not exist
	 * or the variable value is null
	 */
	public Object getValue(String variableName) {
		DLBVariable variable;
		synchronized (dlbVariables) {
			variable = dlbVariables.get(variableName);
		}
		if (variable == null)
			return null;
		return variable.getValue();
	}

	/**
	 * Returns the {@link DLBUser} associated with this {@link DLBVariableStore}.
	 * @return the {@link DLBUser} associated with this {@link DLBVariableStore}.
	 */
	public DLBUser getDLBUser() {
		return DLBUser;
	}

	/**
	 * Returns a set of all the names of {@link DLBVariable}s contained in this
	 * {@link DLBVariableStore}.
	 *
	 * @return a set of all the names of {@link DLBVariable}s contained in this
	 * {@link DLBVariableStore}.
	 */
	public Set<String> getDLBVariableNames() {
		return dlbVariables.keySet();
	}

	public List<String> getSortedDLBVariableNames() {
		List<String> nameList = new ArrayList<>(dlbVariables.keySet());
		Collections.sort(nameList);
		return nameList;
	}

	// --------------------------------------------------------------
	// -------------------- Modification Methods --------------------
	// --------------------------------------------------------------

	/**
	 * Stores the given {@code value} under the given variable-{@code name} in this
	 * {@link DLBVariableStore} and sets the updatedTime to {@code updatedTime}.
	 *
	 * @param name the name of the variable to store.
	 * @param value the value of the variable to store.
	 * @param notifyObservers true if observers of this {@link DLBVariableStore} should be
	 *                        notified about this update.
	 * @param eventTime the time (in the time zone of the user) of the event that triggered the
	 *                  update of this variable.
	 */
	public void setValue(String name, Object value, boolean notifyObservers,
						 ZonedDateTime eventTime) {
		setValue(name,value,notifyObservers,eventTime, DLBVariableStoreChange.Source.UNKNOWN);
	}

	/**
	 * Stores the given {@code value} under the given variable-{@code name} in this
	 * {@link DLBVariableStore} and sets the updatedTime to {@code updatedTime}.
	 *
	 * @param name the name of the variable to store.
	 * @param value the value of the variable to store.
	 * @param notifyObservers true if observers of this {@link DLBVariableStore} should be
	 *                        notified about this update.
	 * @param eventTime the time (in the time zone of the user) of the event that triggered the
	 *                  update of this variable.
	 * @param source the source of the update to this {@link DLBVariableStore}.
	 */
	public void setValue(String name, Object value, boolean notifyObservers,
						 ZonedDateTime eventTime, DLBVariableStoreChange.Source source) {
		synchronized (dlbVariables) {
			DLBVariable DLBVariable = new DLBVariable(name, value, eventTime);
			dlbVariables.put(name, DLBVariable);
			if (notifyObservers) {
				notifyOnChange(new DLBVariableStoreChange.Put(DLBVariable, eventTime, source));
			}
		}
	}

	/**
	 * Remove the {@link DLBVariable} with the given {@code name} from this
	 * {@link DLBVariableStore}. This method returns the {@link DLBVariable} object that has been
	 * deleted, or {@code null} if the element to be deleted was not found.
	 *
	 * @param name the name of the {@link DLBVariable} to remove.
	 * @param notifyObservers true if observers of this {@link DLBVariableStore} should be
	 *                        notified about this update.
	 * @param eventTime the time (in the time zone of the user) of the event that triggered the
	 *                  removal of this variable
	 * @return the {@link DLBVariable} that was removed, or {@code null}.
	 */
	public DLBVariable removeByName(String name, boolean notifyObservers,
									ZonedDateTime eventTime) {
		return removeByName(name,notifyObservers,eventTime, DLBVariableStoreChange.Source.UNKNOWN);
	}

	/**
	 * Remove the {@link DLBVariable} with the given {@code name} from this
	 * {@link DLBVariableStore}. This method returns the {@link DLBVariable} object that has been
	 * deleted, or {@code null} if the element to be deleted was not found.
	 *
	 * @param name the name of the {@link DLBVariable} to remove.
	 * @param notifyObservers true if observers of this {@link DLBVariableStore} should be
	 *                        notified about this update.
	 * @param eventTime the time (in the time zone of the user) of the event that triggered the
	 *                  removal of this variable
	 * @param source the source of the update to this {@link DLBVariableStore}.
	 * @return the {@link DLBVariable} that was removed, or {@code null}.
	 */
	public DLBVariable removeByName(String name, boolean notifyObservers,
									ZonedDateTime eventTime,
									DLBVariableStoreChange.Source source) {
		DLBVariable result;
		synchronized (dlbVariables) {
			result = dlbVariables.remove(name);
		}
		if(result == null) {
			return null;
		} else {
			if(notifyObservers) {
				notifyOnChange(new DLBVariableStoreChange.Remove(name, eventTime, source));
			}
			return result;
		}
	}

	/**
	 * Adds all the entries in the {@code variablesToAdd}-map as {@link DLBVariable}s to this
	 * {@link DLBVariableStore}. The {@code variablesToAdd}-map is treated as a mapping from
	 * variable names ({@link String}s) to variable values ({@link Object}s).
	 * @param variablesToAdd the {@link Map} of name-value pairs to add as {@link DLBVariable}s.
	 * @param notifyObservers true if observers of this {@link DLBVariableStore} should be
	 *                        notified about this update.
	 * @param eventTime the time of the event that triggered the addition of these variables
	 *                  (in the time zone of the user)
	 */
	public void addAll(Map<? extends String, ?> variablesToAdd, boolean notifyObservers,
					   ZonedDateTime eventTime) {
		addAll(variablesToAdd,notifyObservers,eventTime, DLBVariableStoreChange.Source.UNKNOWN);
	}

	/**
	 * Adds all the entries in the {@code variablesToAdd}-map as {@link DLBVariable}s to this
	 * {@link DLBVariableStore}. The {@code variablesToAdd}-map is treated as a mapping from
	 * variable names ({@link String}s) to variable values ({@link Object}s).
	 * @param variablesToAdd the {@link Map} of name-value pairs to add as {@link DLBVariable}s.
	 * @param notifyObservers true if observers of this {@link DLBVariableStore} should be
	 *                        notified about this update.
	 * @param eventTime the time of the event that triggered the addition of these variables
	 *                  (in the time zone of the user)
	 * @param source the source of the update to this {@link DLBVariableStore}.
	 */
	public void addAll(Map<? extends String, ?> variablesToAdd, boolean notifyObservers,
					   ZonedDateTime eventTime, DLBVariableStoreChange.Source source) {
		List<DLBVariable> DLBVariablesToAdd = new ArrayList<>();

		for (Map.Entry<? extends String, ?> entry : variablesToAdd.entrySet()) {
			String name = entry.getKey();
			Object value = entry.getValue();
			DLBVariable DLBVariable = new DLBVariable(name,value,eventTime);
			DLBVariablesToAdd.add(DLBVariable);
		}

		synchronized (dlbVariables) {
			for(DLBVariable DLBVariable : DLBVariablesToAdd) {
				dlbVariables.put(DLBVariable.getName(), DLBVariable);
			}
		}

		if (notifyObservers) {
			notifyOnChange(new DLBVariableStoreChange.Put(DLBVariablesToAdd, eventTime, source));
		}
	}

	/**
	 * Sets the {@link DLBUser} for this {@link DLBVariableStore}.
	 * @param DLBUser the {@link DLBUser} for this {@link DLBVariableStore}.
	 */
	public void setDLBUser(DLBUser DLBUser) {
		this.DLBUser = DLBUser;
	}

	/**
	 * Returns a modifiable mapping of {@link String}s to {@link Object}s that is linked to the
	 * contents of this {@link DLBVariableStore}. The {@link Object} values in this map are the
	 * values of the stored {@link DLBVariable}s, so not the {@link DLBVariable}s themselves!
	 * This {@code Map<String,Object>} can be used as a regular map, but is actually a specific
	 * implementation for this variable store. All basic map operations on the resulting map are
	 * observable by the {@link DLBVariableStoreOnChangeListener}s that are registered to listen to
	 * this {@link DLBVariableStore}.
	 *
	 * <p>This "modifiable map" is used in the execution of DialogueBranch Dialogues containing DialogueBranch
	 * Variables, as the implementation relies on the
	 * {@link nl.rrd.utils.expressions.Expression} interface.</p>
	 *
	 * <p>In other words, if you are thinking "Man, I wish DLBVariableStore was just a simple
	 * mapping of variable names to values", use this method, and you can pretend that that is the
	 * case.</p>
	 *
	 * If {@code notifyObservers} is {@code true}, then any action that modifies the content of this
	 * {@link Map} will result in all listeners being notified.
	 *
	 * @param notifyObservers true if observers of this {@link DLBVariableStore} should be notified
	 *                        about updates to the Map.
	 * @param eventTime the time of the event that is causing the changes to this
	 *                  {@link DLBVariableStore} in the time zone of the user.
	 * @return the modifiable map
	 */
	public Map<String, Object> getModifiableMap(boolean notifyObservers, ZonedDateTime eventTime) {
		return new DLBVariableMap(notifyObservers, eventTime,
				DLBVariableStoreChange.Source.UNKNOWN);
	}

	/**
	 * See {@link #getModifiableMap(boolean, ZonedDateTime)}.
	 * @param notifyObservers true if observers of this {@link DLBVariableStore} should be notified
	 *                        about updates to the Map.
	 * @param eventTime the time of the event that is causing the changes to this
	 * 	                {@link DLBVariableStore} in the time zone of the user.
	 * @param source the source of the changes to this {@link DLBVariableStore}.
	 * @return the modifiable map
	 */
	public Map<String, Object> getModifiableMap(boolean notifyObservers, ZonedDateTime eventTime,
												DLBVariableStoreChange.Source source) {
		return new DLBVariableMap(notifyObservers, eventTime, source);
	}

	/**
	 * A {@link DLBVariableMap} is a Mapping from variable name to variable value and can be used
	 * as an observable and modifiable "view" of the {@link DLBVariableStore} whose changes are
	 * maintained within this {@link DLBVariableStore} object.
	 */
	private class DLBVariableMap implements Map<String, Object> {

		private final boolean notifyObservers;
		private final ZonedDateTime eventTime;
		private final DLBVariableStoreChange.Source source;

		// --------------------------------------------------------
		// -------------------- Constructor(s) --------------------
		// --------------------------------------------------------

		/**
		 * Creates an instance of a {@link DLBVariableMap} which is a mapping of Strings to Objects
		 * representing the contents of this {@link DLBVariableStore}.
		 * @param notifyObservers whether {@link DLBVariableStoreOnChangeListener}s should be
		 *                        notified of updates made to this {@link DLBVariableMap}.
		 * @param eventTime the timestamp that is passed along to all changes that are made in this
		 *                  {@link DLBVariableMap}.
		 */
		public DLBVariableMap(boolean notifyObservers, ZonedDateTime eventTime,
							  DLBVariableStoreChange.Source source) {
			this.notifyObservers = notifyObservers;
			this.eventTime = eventTime;
			this.source = source;
		}

		// --------------------------------------------------------------
		// -------------------- Modification Methods --------------------
		// --------------------------------------------------------------

		@Override
		public Object put(String key, Object value) {
			Object result = get(key);
			setValue(key, value, notifyObservers, eventTime, source);
			return result;
		}

		@Override
		public Object remove(Object key) {
			DLBVariable result = removeByName((String)key, notifyObservers, eventTime, source);
			if(result != null) return result.getValue();
			else return null;
		}

		@Override
		public void putAll(Map<? extends String, ?> variablesToAdd) {
			addAll(variablesToAdd,notifyObservers,eventTime, source);
		}

		@Override
		public void clear() {
			synchronized (dlbVariables) {
				dlbVariables.clear();
			}
			if (notifyObservers)
				notifyOnChange(new DLBVariableStoreChange.Clear(eventTime, source));
		}

		// -----------------------------------------------------------
		// -------------------- Retrieval Methods --------------------
		// -----------------------------------------------------------

		@Override
		public int size() {
			synchronized (dlbVariables) {
				return dlbVariables.size();
			}
		}

		@Override
		public boolean isEmpty() {
			synchronized (dlbVariables) {
				return dlbVariables.isEmpty();
			}
		}

		@Override
		public boolean containsKey(Object key) {
			synchronized (dlbVariables) {
				return dlbVariables.containsKey(key);
			}
		}

		@Override
		public Object get(Object key) {
			synchronized (dlbVariables) {
				if(dlbVariables.get(key) != null) {
					return dlbVariables.get(key).getValue();
				} else return null;
			}
		}

		@Override
		public boolean containsValue(Object value) {
			synchronized (dlbVariables) {
				for (Map.Entry<String, DLBVariable> entry : dlbVariables.entrySet()) {
					if(entry.getValue().getValue().equals(value)) return true;
				}
				return false;
			}
		}

		@Override
		public Set<String> keySet() {
			synchronized (dlbVariables) {
				return dlbVariables.keySet();
			}
		}

		@Override
		public Collection<Object> values() {
			Collection<Object> objectCollection = new ArrayList<>();

			synchronized (dlbVariables) {
				Collection<DLBVariable> DLBVariableCollection = dlbVariables.values();
				for(DLBVariable DLBVariable : DLBVariableCollection) {
					objectCollection.add(DLBVariable.getValue());
				}
			}

			return objectCollection;
		}

		@Override
		public Set<Entry<String, Object>> entrySet() {
			Set<Entry<String,Object>> resultSet = new HashSet<>();

			synchronized (dlbVariables) {
				Set<Entry<String, DLBVariable>> entrySet = dlbVariables.entrySet();

				for(Entry<String, DLBVariable> entry : entrySet) {
					String key = entry.getKey();
					Object value = entry.getValue().getValue();
					Map.Entry<String,Object> newEntry = new AbstractMap.SimpleEntry<>(key, value);
					resultSet.add(newEntry);
				}
			}

			return resultSet;
		}
	}
}

