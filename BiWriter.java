
/**
 * Copyright 1997-2015 Stefan Nilsson, 2015-2017 Douglas Wikstrom.
 * This file is part of the NIC/NAS software licensed under BSD
 * License 2.0. See LICENSE file.
 */

package se.kth.csc.nas;

import java.io.PrintWriter;

/**
 * Writer of instructions and parameters.
 */
class BiWriter {

    /**
     * Underlying print writer.
     */
    protected final PrintWriter pw;

    /**
     * Creates a printer for binary objects.
     *
     * @param pw Underlying printer.
     */
    BiWriter(final PrintWriter pw) {
        this.pw = pw;
    }

    /**
     * Flush the current line.
     */
    void flush() {
        pw.println();
    }

    /**
     * Print operator code of operator.
     *
     * @param operator Operator.
     */
    void printOperator(final Operator operator) {
        pw.print(operator.code());
    }

    /**
     * Print integer value as a byte.
     *
     * @param b Byte to be written.
     */
    void printByte(final int b) {
        if (b < 0 || b > 15) {
            throwError("byte", b);
        } else {
            pw.printf("%x", b);
        }
    }

    /**
     * Print integer value as a word.
     *
     * @param w Byte to be written.
     */
    void printWord(final int w) {
        if (w < 0 || w > 0xff) {
            throwError("word", w);
        } else {
            pw.printf("%02x", w);
        }
    }

    /**
     * Print integer representation of an operator code verbatim.
     *
     * @param c Code as integer.
     */
    void printCode(final int c) {
        if (c < 0 || c > 0xffff) {
            throwError("code", c);
        } else {
            pw.printf("%04x", c);
        }
    }

    /**
     * Syntactic sugar for throwing an error.
     *
     * @param msg Error message.
     * @param n Integer input.
     */
    private void throwError(final String msg, final int n) {
        throw new Error("Internal error: " + msg
                        + "=0x" + Integer.toHexString(n));
    }
}
