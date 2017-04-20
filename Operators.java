
/**
 * Copyright 1997-2015 Stefan Nilsson, 2015-2017 Douglas Wikstrom.
 * This file is part of the NIC/NAS software licensed under BSD
 * License 2.0. See LICENSE file.
 */

package se.kth.csc.nas;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapping of strings to operators.
 */
class Operators {

    /**
     * Avoid accidental instantiation.
     */
    private Operators() {
    }

    /**
     * Stores hash table of all operators indexed by name.
     */
    private final static Map<String, Operator> map =
        new HashMap<String, Operator>();

    static {
        for (Operator operator : Operator.values()) {
            map.put(operator.toString(), operator);
        }
    }

    /**
     * Returns the operator with the given name.
     *
     * @param name Name of operator.
     */
    public static Operator get(final String name) {
        return map.get(name);
    }
}