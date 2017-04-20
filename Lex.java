
/**
 * Copyright 1997-2015 Stefan Nilsson, 2015-2017 Douglas Wikstrom.
 * This file is part of the NIC/NAS software licensed under BSD
 * License 2.0. See LICENSE file.
 */

package se.kth.csc.nas;

/**
 * Lexical analysis routines.
 */
class Lex {

    /**
     * Number of bits in one block.
     */
    final int BLOCKSIZE;

    /**
     * Error log.
     */
    final ErrorLog errorLog;

    /**
     * Creates a lexical analyzer with the given error log.
     *
     * @param BLOCKSIZE Number of bits in each block.
     * @param errorLog Error log.
     */
    Lex(final int BLOCKSIZE, final ErrorLog errorLog) {
        this.BLOCKSIZE = BLOCKSIZE;
        this.errorLog = errorLog;
    }

    /**
     * Strips any comments from the input line and tokenizes it. If
     * the result is empty, then null is returned.
     *
     * @param line Line to be tokenized.
     * @return Tokens or null.
     */
    String[] tokenizeLine(final String line) {

        String s = line;

        // Strip comments.
        int end = s.indexOf("//");
        if (end >= 0) {
            s = s.substring(0, end);
        }

        // Strip spurious white space at ends of string.
        s = s.trim();

        // Split at whitespace into multiple strings containing no
        // whitespace.
        final String[] tokens = s.split("\\s+");

        if (tokens[0].equals("")) {
            return null;
        } else {
            return tokens;
        }
    }

    /**
     * Determines if the input string is a valid identifier.
     *
     * @param s String to verify.
     * @return Indicator of validity of string as identifier.
     */
    boolean isIdentifier(final String s) {
        return s.matches("[a-zA-Z][\\w|\\d]*");
    }

    /**
     * Parse string as a register name of the form "rX", where X is a
     * hexadecimal integer 0-f, and return the result as the integer
     * register index between 0 and 15.
     *
     * @param registerName String representation of register.
     * @return Integer index of register.
     */
    int parseReg(final String registerName) {

        if (registerName.matches("r[0-9]")) {

            return registerName.charAt(1) - '0';

        } else if (registerName.matches("r1[0-5]")) {

            return 10 + registerName.charAt(2) - '0';

        } else if (registerName.matches("r[a-f]")) {

            return 10 + registerName.charAt(1) - 'a';

        } else {

            errorLog.error("Invalid register name! (%s)", registerName);
            return NAS.INT_ERR;
        }
    }

    /**
     * Parses a decimal string representation of an integer and
     * returns its value.
     *
     * @param s String representation of an integer in decimal
     * representation.
     * @return Integer value.
     */
    int parseDec(final String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            errorLog.error("Invalid decimal number! (%s)", s);
            return NAS.INT_ERR;
        }
    }

    /**
     * Parses a signed integer in decimal notation expected to be in
     * the interval [-B,B-1], where B = 2^(BLOCKSIZE * blocks - 1) and returns
     * it modulo 256, i.e., as an integer in [0,2B-1].
     *
     * @param s String representation of an integer in decimal
     * representation.
     * @param blocks Number of blocks available to represent the value.
     * @return Integer value.
     */
    int parseDecBounded(final String s, final int blocks) {
        final int n = parseDec(s);
        final int modulus = 1 << (BLOCKSIZE * blocks);
        final int B = modulus / 2;

        if (n == NAS.INT_ERR) {
            return n;
        } else if (n < -B || n > (B - 1)) {
            errorLog.error("Decimal value out of range! (" + s
                           + ") not in [" + (-B) + "," + (B - 1) + "])");
            return NAS.INT_ERR;
        } else {
            return (modulus + n) % modulus;
        }
    }

    /**
     * Parses a hexadecimal string representation of an integer and
     * returns its value.
     *
     * @param s String representation of an integer in hexadecimal
     * representation.
     * @return Integer value.
     */
    int parseHex(final String s) {
        if (!s.startsWith("0x")) {
            errorLog.error("Hexadecimal numbers must start with \"0x\"! (%s)",
                           s);
            return NAS.INT_ERR;
        } else {
            try {
                return Integer.parseInt(s.substring(2), 16);
            } catch (NumberFormatException e) {
                errorLog.error("Invalid hexadecimal number! (%s)", s);
                return NAS.INT_ERR;
            }
        }
    }

    /**
     * Parses a hexadecimal string representing a value in [0,B-1],
     * where B=2^(BLOCKSIZE * blocks).
     *
     * @param s String representation of an integer in hexadecimal
     * representation.
     * @param blocks Number of blocks available to represent the value.
     * @return Integer value.
     */
    int parseHexBounded(final String s, final int blocks) {
        final int n = parseHex(s);
        final int B = 1 << BLOCKSIZE * blocks;

        if (n == NAS.INT_ERR) {
            return n;
        } else if (n < 0 || n > B) {
            errorLog.error("Hex value out of range! (%s not in [0,%s])",
                           s, String.format("0x%x", B));
            return NAS.INT_ERR;
        } else {
            return n;
        }
    }

    /**
     * Parses an integer represented in decimal in [-B,B-1] or as a
     * hexadecimal number in [0,B-1] and returns it as an integer in
     * [0,B-1], where B=2^(BLOCKSIZE * blocks).
     *
     * @param s String representation of the integer.
     * @param blocks Number of blocks available to represent the value.
     * @return Integer value.
     */
    int parseIntBounded(final String s, final int blocks) {
        if (s.startsWith("0x")) {
            return parseHexBounded(s, blocks);
        } else {
            return parseDecBounded(s, blocks);
        }
    }
}
