
/**
 * Copyright 1997-2015 Stefan Nilsson, 2015-2017 Douglas Wikstrom.
 * This file is part of the NIC/NAS software licensed under BSD
 * License 2.0. See LICENSE file.
 */

package se.kth.csc.nas;

import java.util.Formatter;

/**
 * Represents a constant or symbolic integer value, which may be
 * defined in terms of other symbolic values and an offset.
 */
class Value {

    /**
     * Basis of a symbolic value defined in terms of another symbolic
     * value.
     */
    String name;

    /**
     * Constant value.
     */
    int constant;

    /**
     * Offset from the basis symbolic value.
     */
    int offset;

    /**
     * Determines if hexadecimal or decimal notation is used when
     * printing.
     */
    boolean hexFormat;

    /**
     * Creates a value from the given symbolic name, constant, and
     * offset. The former may be the empty string in the case of a
     * constant value.
     *
     * @param name Symbolic name or the empty string.
     * @param constant Constant value.
     * @param offset Offset.
     */
    Value(final String name, final int constant, final int offset) {
        this.name = name;
        this.constant = constant;
        this.offset = offset;
        this.hexFormat = false;
    }

    /**
     * Sets the format of this value to hexadecimal.
     */
    public void useHexFormat() {
        this.hexFormat = true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final Formatter fmt = new Formatter(sb);

        if (hexFormat) {
            fmt.format("0x%02x", constant - offset);
        } else {
            int no = constant - offset;
            fmt.format("%d", no >= 128 ? no - 256 : no);
        }

        if (!name.equals("")) {
            fmt.format("(%s)", name);
        }

        String offs = offset > 0 ? "+" : "";
        if (offset != 0) {
            offs += offset;
        }
        sb.append(offs);

        return sb.toString();
    }
}
