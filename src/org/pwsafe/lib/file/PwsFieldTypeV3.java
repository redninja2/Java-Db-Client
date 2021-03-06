/*
 * $Id: PwsFieldTypeV3.java 488 2013-12-11 23:22:25Z roxon $
 * 
 * Copyright (c) 2008-2014 David Muller <roxon@users.sourceforge.net>.
 * All rights reserved. Use of the code is allowed under the
 * Artistic License 2.0 terms, as specified in the LICENSE file
 * distributed with this code, or available from
 * http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pwsafe.lib.file;

public enum PwsFieldTypeV3 implements PwsFieldType {
	V3_ID_STRING(0), UUID(1), GROUP(2), TITLE(3), USERNAME(4), NOTES(5), PASSWORD(6), CREATION_TIME(
			7), PASSWORD_MOD_TIME(8), LAST_ACCESS_TIME(9), PASSWORD_LIFETIME(10), PASSWORD_POLICY_DEPRECATED(
			11), LAST_MOD_TIME(12), URL(13), AUTOTYPE(14), PASSWORD_HISTORY(15), PASSWORD_POLICY(16), PASSWORD_EXPIRY_INTERVAL(
			17), END_OF_RECORD(255);

	private int id;
	private String name;

	private PwsFieldTypeV3(int anId) {
		id = anId;
		name = toString();
	}

	private PwsFieldTypeV3(int anId, String aName) {
		id = anId;
		name = aName;
	}

	public int getId() {
		return id;
	}

	public String getName() {

		return name;
	}

	public static PwsFieldTypeV3 valueOf(int anId) {
		if (anId == 255) {
			return END_OF_RECORD;
		} else {
			return values()[anId];
		}
	}
}
