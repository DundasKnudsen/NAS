
/**
 * Copyright 1997-2015 Stefan Nilsson, 2015-2017 Douglas Wikstrom.
 * This file is part of the NIC/NAS software licensed under BSD
 * License 2.0. See LICENSE file.
 */

package se.kth.csc.nas;

/**
 * Thrown to indicate that there was an error.
 */
public class NASError extends Error {

    public NASError(final String message) {
        super(message);
    }
    public NASError(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
