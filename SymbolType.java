
/**
 * Copyright 1997-2015 Stefan Nilsson, 2015-2017 Douglas Wikstrom.
 * This file is part of the NIC/NAS software licensed under BSD
 * License 2.0. See LICENSE file.
 */

package se.kth.csc.nas;

/**
 * Types of user defined symbols.
 */
enum SymbolType {
    WORD("word"),
    CODE("code"),
    LABEL("label"),
    VOID("_");

    /**
     * Name of this symbol type.
     */
    final String name;

    /**
     * Creates a symbol type with the given name.
     *
     * @param name Name of symbol type.
     */
    SymbolType(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
