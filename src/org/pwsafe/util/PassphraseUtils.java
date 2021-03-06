/*
 * $Id: PassphraseUtils.java 488 2013-12-11 23:22:25Z roxon $
 * 
 * Copyright (c) 2008-2011 David Muller <roxon@users.sourceforge.net>.
 * All rights reserved. Use of the code is allowed under the
 * Artistic License 2.0 terms, as specified in the LICENSE file
 * distributed with this code, or available from
 * http://www.opensource.org/licenses/artistic-license-2.0.php
 */
package org.pwsafe.util;

import org.pwsafe.lib.I18nHelper;
import org.pwsafe.lib.Log;
import org.pwsafe.lib.Util;
import org.pwsafe.lib.exception.InvalidPassphrasePolicy;

/**
 *
 */
public class PassphraseUtils {
	/**
	 * Log4j logger
	 */
	private static final Log LOG = Log.getInstance(PassphraseUtils.class.getPackage().getName());

	/**
	 * Standard lowercase characters.
	 */
	public static final char[] LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz".toCharArray();

	/**
	 * Standard uppercase characters.
	 */
	public static final char[] UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

	/**
	 * Standard digit characters.
	 */
	public static final char[] DIGIT_CHARS = "1234567890".toCharArray();

	/**
	 * Standard symbol characters.
	 */
	public static final char[] SYMBOL_CHARS = "+-=_@#$%^&;:,.<>/~\\[](){}?!|".toCharArray();

	/**
	 * Lowercase characters with confusable characters removed.
	 */
	public static final char[] EASYVISION_LC_CHARS = "abcdefghijkmnopqrstuvwxyz".toCharArray();

	/**
	 * Uppercase characters with confusable characters removed.
	 */
	public static final char[] EASYVISION_UC_CHARS = "ABCDEFGHJKLMNPQRTUVWXY".toCharArray();

	/**
	 * Digit characters with confusable characters removed.
	 */
	public static final char[] EASYVISION_DIGIT_CHARS = "346789".toCharArray();

	/**
	 * Symbol characters with confusable characters removed.
	 */
	public static final char[] EASYVISION_SYMBOL_CHARS = "+-=_@#$%^&<>/~\\?".toCharArray();

	/**
	 * The minimum length that a password must be to be not weak.
	 */
	public static final int MIN_PASSWORD_LEN = 4;

	/**
	 * Private for singleton pattern
	 */
	private PassphraseUtils() {
		super();
	}

	/**
	 * Generates a new random password according to the policy supplied.
	 * 
	 * @param policy the {@link PassphrasePolicy} policy
	 * 
	 * @return A new random password.
	 * 
	 * @throws InvalidPassphrasePolicy
	 */
	public static String makePassword(PassphrasePolicy policy) throws InvalidPassphrasePolicy {
		LOG.enterMethod("makePassword");

		LOG.debug2(policy.toString());

		char allChars[][];
		boolean typesSeen[];
		StringBuilder password;
		int typeCount;

		if (!policy.isValid()) {
			LOG.info(I18nHelper.getInstance().formatMessage("I0004",
					new Object[] { policy.toString() }));
			throw new InvalidPassphrasePolicy();
		}

		password = new StringBuilder(policy.length);
		typeCount = 0;

		if (policy.digitChars) {
			++typeCount;
		}
		if (policy.lowercaseChars) {
			++typeCount;
		}
		if (policy.uppercaseChars) {
			++typeCount;
		}
		if (policy.symbolChars) {
			++typeCount;
		}

		allChars = new char[typeCount][];
		typesSeen = new boolean[4];

		for (int ii = 0; ii < typeCount; ++ii) {
			typesSeen[ii] = true;
		}

		if (policy.easyview) {
			int ii = 0;

			if (policy.digitChars) {
				allChars[ii++] = EASYVISION_DIGIT_CHARS;
			}
			if (policy.lowercaseChars) {
				allChars[ii++] = EASYVISION_LC_CHARS;
			}
			if (policy.uppercaseChars) {
				allChars[ii++] = EASYVISION_UC_CHARS;
			}
			if (policy.symbolChars) {
				allChars[ii++] = EASYVISION_SYMBOL_CHARS;
			}
		} else {
			int ii = 0;

			if (policy.digitChars) {
				allChars[ii++] = DIGIT_CHARS;
			}
			if (policy.lowercaseChars) {
				allChars[ii++] = LOWERCASE_CHARS;
			}
			if (policy.uppercaseChars) {
				allChars[ii++] = UPPERCASE_CHARS;
			}
			if (policy.symbolChars) {
				allChars[ii++] = SYMBOL_CHARS;
			}
		}

		do {
			password.delete(0, password.length());

			for (int ii = 0; ii < policy.length; ++ii) {
				int type;

				type = Util.positiveRand() % typeCount;
				typesSeen[type] = false;

				password.append(allChars[type][Util.positiveRand() % allChars[type].length]);
			}
		} while (typesSeen[0] || typesSeen[1] || typesSeen[2] || typesSeen[3]);

		LOG.debug2("Generated password is " + password.toString());

		LOG.leaveMethod("makePassword");

		return password.toString();
	}

	/**
	 * Checks the password against a set of rules to determine whether it is
	 * considered weak. The rules are: </p>
	 * <p>
	 * <ul>
	 * <li>It is at least <code>MIN_PASSWORD_LEN</code> characters long.
	 * <li>At least one lowercase character.
	 * <li>At least one uppercase character.
	 * <li>At least one digit or symbol character.
	 * </ul>
	 * 
	 * @param password the password to check.
	 * 
	 * @return <code>true</code> if the password is considered to be weak,
	 *         <code>false</code> otherwise.
	 */
	public static boolean isWeakPassword(String password) {
		boolean hasUC = false;
		boolean hasLC = false;
		boolean hasDigit = false;
		boolean hasSymbol = false;

		if (password.length() < MIN_PASSWORD_LEN) {
			return true;
		}

		for (int ii = 0; ii < password.length(); ++ii) {
			char c;

			c = password.charAt(ii);

			if (Character.isDigit(c)) {
				hasDigit = true;
			} else if (Character.isUpperCase(c)) {
				hasUC = true;
			} else if (Character.isLowerCase(c)) {
				hasLC = true;
			} else {
				hasSymbol = true;
			}
		}

		if (hasUC && hasLC && (hasDigit || hasSymbol)) {
			return false;
		}
		return true;
	}
}
