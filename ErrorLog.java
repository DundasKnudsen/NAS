
/**
 * Copyright 1997-2015 Stefan Nilsson, 2015-2017 Douglas Wikstrom.
 * This file is part of the NIC/NAS software licensed under BSD
 * License 2.0. See LICENSE file.
 */

package se.kth.csc.nas;

import java.util.Arrays;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Log for parsing errors.
 */
public class ErrorLog {

    /**
     * Lines parsed so far including the current line.
     */
    final List<String> lines;

    /**
     * Maximal number of errors.
     */
    final int maxErrors;

    /**
     * Errors that have occured so far.
     */
    final List<ErrorEntry> errorEntries;

    /**
     * Indicates if there were too many errors.
     */
    boolean tooManyErrors;

    /**
     * Creates an error log for a file, with a list of lines, and a
     * maximal number of errors.
     *
     * @param lines List of parsed lines including the current line.
     * @param maxErrors Maximal number of errors accepted before an
     * exception is thrown.
     */
    public ErrorLog(final List<String> lines,
                    final int maxErrors) {
        this.lines = lines;
        this.maxErrors = maxErrors;
        this.errorEntries = new ArrayList<ErrorEntry>();
        this.tooManyErrors = false;
    }

    /**
     * Returns the number of errors logged.
     *
     * @return Number of errors logged.
     */
    public int getNoErrors() {
        return errorEntries.size();
    }

    /**
     * Records an error in the log.
     *
     * @param lineIndex Line at which the error occured. Minus one if
     * this is not applicable.
     * @param throwable Exception that triggered the error. Null if
     * the error was not triggered by an exception.
     * @param params Strings of which the first is a format.
     * @throws TooManyErrorsException If more than the maximum number
     * of errors have occured.
     */
    void error(final int lineIndex,
               final Throwable throwable,
               final String ... params)
        throws TooManyErrorsException {
        String message;
        if (params.length == 1) {
            message = params[0];
        } else {
            final Object[] strings =
                (Object[]) Arrays.copyOfRange(params, 1, params.length);
            message = String.format(params[0], strings);
        }
        errorEntries.add(new ErrorEntry(lineIndex, message, throwable));

        if (errorEntries.size() >= maxErrors) {
            tooManyErrors = true;
            throw new TooManyErrorsException();
        }
    }

    /**
     * Records an error in the log.
     *
     * @param lineIndex Line at which the error occured.
     * @param params Strings of which the first is a format.
     * @throws TooManyErrorsException If more than the maximum number
     * of errors have occured.
     */
    void error(final int lineIndex, final String ... params)
        throws TooManyErrorsException {
        error(lineIndex, null, params);
    }

    /**
     * Records an error in the log.
     *
     * @param params Strings of which the first is a format.
     * @throws TooManyErrorsException If more than the maximum number
     * of errors have occured.
     */
    void error(final String ... params) throws TooManyErrorsException {
        error(lines.size(), null, params);
    }

    /**
     * Records an error in the log.
     *
     * @param throwable Exception that triggered the error. Null if
     * the error was not triggered by an exception.
     * @param params Strings of which the first is a format.
     * @throws TooManyErrorsException If more than the maximum number
     * of errors have occured.
     */
    void error(final Throwable throwable, final String ... params)
        throws TooManyErrorsException {
        error(-1, throwable, params);
    }

    /**
     * Returns an error report as a string.
     *
     * @param label Label of source code.
     * @return Error report.
     */
    String toString(final String label) {

        final int errors = errorEntries.size();

        final StringBuilder sb = new StringBuilder();

        if (errors > 0) {
            if (label == null) {
                sb.append("Errors:\n");
            } else {
                sb.append("Errors in " + label + ":\n");
            }
        }

        for (ErrorEntry errorEntry : errorEntries) {
            sb.append(errorEntry).append("\n");
        }

        if (errors > 0) {
            final String summary =
                String.format("%d error%s", errors, errors > 1 ? "s" : "");
            sb.append(summary);
        }

        if (tooManyErrors) {
            sb.append("\nToo many errors! (only the first are listed)");
        }
        return sb.toString();
    }
}

/**
 * Entry representing a single error. There may be multiple errors for
 * a single line of the source.
 */
class ErrorEntry {

    /**
     * Line number at which the error occured or minus one if this is
     * not applicable.
     */
    int lineIndex;

    /**
     * Error message.
     */
    String message;

    /**
     * Exception that caused the error or null if no exception
     * triggered the error.
     */
    Throwable throwable;

    /**
     * Creates an error entry in the log.
     *
     * @param lineIndex Line number at which the error occured or
     * minus one if this is not applicable.
     * @param message Error message.
     * @param throwable Exception that caused the error or null if no
     * exception triggered the error.
     */
    ErrorEntry(final int lineIndex, final String message,
               final Throwable throwable) {
        this.lineIndex = lineIndex;
        this.message = message;
        this.throwable = throwable;
    }

    @Override
    public String toString() {

        String s;
        if (lineIndex < 0) {
            s = message;
        } else {
            s = String.format("%d: %s", lineIndex, message);
        }
        return s;
    }
}
