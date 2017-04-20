
/**
 * Copyright 1997-2015 Stefan Nilsson, 2015-2017 Douglas Wikstrom.
 * This file is part of the NIC/NAS software licensed under BSD
 * License 2.0. See LICENSE file.
 */

package se.kth.csc.nas;

import java.util.Formatter;

/**
 * Symbol and associated values appearing in an assembler program.
 */
class Symbol {

    /**
     * Type of this symbol.
     */
    SymbolType type;

    /**
     * Name of symbol.
     */
    String name;

    /**
     * Values associated with symbol.
     */
    int[] values;

    /**
     * Address of word, operator, or label.
     */
    int address;

    /**
     * Creates a symbol with a given type, name, and values.
     *
     * @param type Type of this symbol.
     * @param name Name of symbol.
     * @param values Values associated with symbol.
     */
    Symbol(final SymbolType type, final String name, final int... values) {
        this.type = type;
        this.name = name;
        this.values = values;
    }

    /**
     * Sets address of this symbol.
     *
     * @param address Address of this symbol.
     */
    void setAddress(final int address) {
        this.address = address;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final Formatter fmt = new Formatter(sb);

        fmt.format("%s %s 0x%02x", type, name, address);

        if (type == SymbolType.LABEL) {
            return sb.toString();
        }

        fmt.format(" [");
        for (int n: values) {
            switch (type) {
            case WORD:
                fmt.format("%d ", n >= 128 ? n - 256 : n);
                break;
            case CODE:
                fmt.format("%04x ", n);
                break;
            }
        }
        sb.setLength(sb.length() - 1); // Remove trailing " ".
        sb.append("]");

        return sb.toString();
    }
}
