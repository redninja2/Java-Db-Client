/*
 * $Id: PwsFieldTypeV2.java 488 2013-12-11 23:22:25Z roxon $
 * 
 * Copyright (c) 2008-2014 David Muller <roxon@users.sourceforge.net>.
 * All rights reserved. Use of the code is allowed under the
 * Artistic License 2.0 terms, as specified in the LICENSE file
 * distributed with this code, or available from
 * http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pwsafe.lib.file;

public enum PwsFieldTypeV2 implements PwsFieldType {

	V2_ID_STRING(0), UUID(1), GROUP(2), TITLE(3), USERNAME(4), NOTES(5), PASSWORD(6), CREATION_TIME(
			7), PASSWORD_MOD_TIME(8), LAST_ACCESS_TIME(9), PASSWORD_LIFETIME(10), PASSWORD_POLICY(
			11), END_OF_RECORD(255);

	private int id;
	private String name;

	private PwsFieldTypeV2(int anId) {
		id = anId;
		name = toString();
	}

	private PwsFieldTypeV2(int anId, String aName) {
		id = anId;
		name = aName;
	}

	public int getId() {
		return id;
	}

	public String getName() {

		return name;
	}
}
