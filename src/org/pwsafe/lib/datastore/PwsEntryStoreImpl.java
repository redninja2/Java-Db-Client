/*
 * $Id: PwsEntryStoreImpl.java 499 2014-02-03 19:52:29Z roxon $
 *
 * Copyright (c) 2008-2014 David Muller <roxon@users.sourceforge.net>.
 * All rights reserved. Use of the code is allowed under the
 * Artistic License 2.0 terms, as specified in the LICENSE file
 * distributed with this code, or available from
 * http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pwsafe.lib.datastore;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.pwsafe.lib.Log;
import org.pwsafe.lib.exception.PasswordSafeException;
import org.pwsafe.lib.file.PwsFieldType;
import org.pwsafe.lib.file.PwsFieldTypeV1;
import org.pwsafe.lib.file.PwsFieldTypeV2;
import org.pwsafe.lib.file.PwsFieldTypeV3;
import org.pwsafe.lib.file.PwsFile;
import org.pwsafe.lib.file.PwsFileV1;
import org.pwsafe.lib.file.PwsFileV2;
import org.pwsafe.lib.file.PwsLoadListener;
import org.pwsafe.lib.file.PwsRecord;

public class PwsEntryStoreImpl implements PwsEntryStore, PwsLoadListener {

	private final static Log LOGGER = Log.getInstance(PwsEntryStoreImpl.class);
	private static final EnumSet<PwsFieldTypeV1> DEFAULT_V1_SPARSE_FIELDS = EnumSet.of(
			PwsFieldTypeV1.TITLE, PwsFieldTypeV1.USERNAME);
	private static final EnumSet<PwsFieldTypeV2> DEFAULT_V2_SPARSE_FIELDS = EnumSet.of(
			PwsFieldTypeV2.TITLE, PwsFieldTypeV2.GROUP, PwsFieldTypeV2.USERNAME,
			PwsFieldTypeV2.NOTES);
	private static final EnumSet<PwsFieldTypeV3> DEFAULT_V3_SPARSE_FIELDS = EnumSet.of(
			PwsFieldTypeV3.TITLE, PwsFieldTypeV3.GROUP, PwsFieldTypeV3.USERNAME,
			PwsFieldTypeV3.NOTES, PwsFieldTypeV3.URL, PwsFieldTypeV3.PASSWORD_LIFETIME, PwsFieldTypeV3.LAST_MOD_TIME);

	PwsFile pwsFile;

	/**
	 * List of partly filled Entries. Used for overviews etc.
	 */
	protected List<PwsEntryBean> sparseEntries;

	private Set<? extends PwsFieldType> sparseFields;


	public PwsEntryStoreImpl(final PwsFile aPwsFile) {
		this(aPwsFile, false);
	}

	public PwsEntryStoreImpl(final PwsFile aPwsFile, final boolean isListenerAdded) {
		super();
		pwsFile = aPwsFile;
		setDefaultSparseFields();
		if (sparseEntries == null) {
			sparseEntries = new ArrayList<PwsEntryBean>();
		}

		// Backward compatibility - if the store is not loaded via the listener,
		// fill it here
		if (! isListenerAdded) {
			load();
		}
	}

	public PwsEntryStoreImpl(final PwsFile aPwsFile, final Set<PwsFieldType> someSparseFields) {
		this(aPwsFile, someSparseFields, false);
	}

	public PwsEntryStoreImpl(final PwsFile aPwsFile, final Set<PwsFieldType> someSparseFields, final boolean isListenerAdded) {
		super();
		pwsFile = aPwsFile;
		sparseFields = someSparseFields;

		// Backward compatibility - if the store is not loaded via the listener,
		// fill it here
		if (! isListenerAdded) {
			load();
		}
	}
	private void setDefaultSparseFields() {
		if (pwsFile instanceof PwsFileV1) {
			sparseFields = DEFAULT_V1_SPARSE_FIELDS.clone();
		} else if (pwsFile instanceof PwsFileV2) {
			sparseFields = DEFAULT_V2_SPARSE_FIELDS.clone();
		} else {
			sparseFields = DEFAULT_V3_SPARSE_FIELDS.clone();
		}
	}

	private void load() {

		if (sparseEntries == null) {
			sparseEntries = new ArrayList<PwsEntryBean>();
		}
		refresh();
	}

	private void refresh() {
		sparseEntries.clear();

		if (pwsFile == null) {
			return;
		}
		final Iterator<? extends PwsRecord> it = pwsFile.getRecords();
		while(it.hasNext()) {
			final PwsRecord r = it.next();
			// TODO: more effective: only fill sparse fields
			addRecord(r);
		}
	}

	private void addRecord(final PwsRecord r) {
		final PwsEntryBean theBean = sparsify(PwsEntryBean.fromPwsRecord(r, sparseFields));
		theBean.setStoreIndex(sparseEntries.size());
		sparseEntries.add(theBean);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.pwsafe.lib.datastore.PwsEntryStore#addEntry(org.pwsafe.lib.datastore
	 * .PwsEntryBean)
	 */
	public boolean addEntry(PwsEntryBean anEntry) throws PasswordSafeException {
		if (anEntry.isSparse()) {
			throw new IllegalArgumentException("Inserts only possible with filled entries");
		}

		PwsRecord theRecord = pwsFile.newRecord();
		anEntry.toPwsRecord(theRecord);

		pwsFile.add(theRecord);

		theRecord = pwsFile.getRecord(pwsFile.getRecordCount() - 1); // TODO can
		// this
		// be
		// made
		// more
		// robust?

		anEntry = sparsify(PwsEntryBean.fromPwsRecord(theRecord, sparseFields));
		anEntry.setStoreIndex(pwsFile.getRecordCount() - 1);
		sparseEntries.add(anEntry);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pwsafe.lib.datastore.PwsEntryStore#clear()
	 */
	public void clear() {
		sparseEntries.clear();
		pwsFile = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pwsafe.lib.datastore.PwsEntryStore#getEntry(int)
	 */
	public PwsEntryBean getEntry(final int anIndex) {
		if (sparseEntries != null && anIndex < sparseEntries.size()) {
			final PwsRecord theRecord = pwsFile.getRecord(anIndex);
			final PwsEntryBean theEntry = PwsEntryBean.fromPwsRecord(theRecord);
			theEntry.setStoreIndex(anIndex);
			theEntry.setSparse(false);
			return theEntry;
		} else {
			return null;
		}
	}

	/**
	 * @return the pwsFile
	 */
	public PwsFile getPwsFile() {
		return pwsFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.pwsafe.lib.datastore.PwsEntryStore#getSparseEntries()
	 */
	public List<PwsEntryBean> getSparseEntries() {
		if (sparseEntries == null) {
			sparseEntries = new ArrayList<PwsEntryBean>();
		}
		return sparseEntries;
	}

	/**
	 * @param pwsFile the pwsFile to set
	 */
	public void setPwsFile(final PwsFile pwsFile) {
		this.pwsFile = pwsFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.pwsafe.lib.datastore.PwsEntryStore#setSparseFields(java.util.Set)
	 */
	public void setSparseFields(final Set<PwsFieldType> fieldTypes) {

		// if this is an update and it is less sparse than before -> refill the
		// sparse list
		if (sparseFields != null && !sparseFields.containsAll(fieldTypes)) {
			sparseFields = fieldTypes;
			refresh();
		} else {
			sparseFields = fieldTypes;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.pwsafe.lib.datastore.PwsEntryStore#updateEntry(org.pwsafe.lib.datastore
	 * .PwsEntryBean)
	 */
	public boolean updateEntry(final PwsEntryBean anEntry) {
		final int index = anEntry.getStoreIndex();
		if (anEntry.isSparse() || index < 0) {
			throw new IllegalArgumentException("Updates only possible with filled entries");
		}

		if (index >= sparseEntries.size()) {
			throw new IndexOutOfBoundsException("record index too big - no record with index "
					+ index);
		}
		final PwsRecord theRecord = pwsFile.getRecord(index);
		final PwsEntryBean origBean = PwsEntryBean.fromPwsRecord(theRecord);
		origBean.setStoreIndex(index);
		if (origBean.equals(anEntry)) {
			LOGGER.warn("Update without change");
		}
		anEntry.toPwsRecord(theRecord);
		pwsFile.set(index, theRecord);
		final PwsEntryBean newEntry = PwsEntryBean.fromPwsRecord(theRecord, sparseFields);
		newEntry.setStoreIndex(index);
		sparseEntries.set(index, sparsify(newEntry));

		return true;
	}

	private PwsEntryBean sparsify(final PwsEntryBean anEntry) {

		anEntry.setSparse(true);
		return anEntry;
	}

	public boolean removeEntry(final PwsEntryBean anEntry) {
		final int index = anEntry.getStoreIndex();

		if (index >= sparseEntries.size()) {
			throw new IndexOutOfBoundsException("record index too big - no record with index "
					+ index);
		}
		pwsFile.getRecord(index);
		// TODO: Check if this Record is the same
		final boolean result = pwsFile.removeRecord(index);

		refresh();
		return result;
	}


	public void loaded(final PwsRecord aRecord) {
		addRecord(aRecord);
	}

}
