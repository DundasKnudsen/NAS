
/**
 * Copyright 1997-2015 Stefan Nilsson, 2015-2017 Douglas Wikstrom.
 * This file is part of the NIC/NAS software licensed under BSD
 * License 2.0. See LICENSE file.
 */

package se.kth.csc.nas;

/**
 * Assembler operator that is translated into an opcode executed by
 * the computer.
 */
enum Operator {
    HALT("0", "halt"),
    LOAD("1", "load"),
    LOADC("2", "loadc"),
    LOADR("3", "loadr"),
    STORE("4", "store"),
    STORER("5", "storer"),
    MOVE("6", "move"),
    ADD("7", "add"),
    ADDC("8", "addc"),
    MUL("9", "mul"),
    SUB("a", "sub"),
    SHIFT("b", "shift"),
    AND("c", "and"),
    OR("d", "or"),
    XOR("e", "xor"),
    JUMP("f", "jump"),
    JUMPE("f", "jumpe"),
    JUMPN("f", "jumpn"),
    JUMPL("f", "jumpl"),
    JUMPLE("f", "jumple"),
    NOOP("f", "noop");

    /**
     * Operator code in hexadecimal notation.
     */
    private final String code;

    /**
     * Operator name in assembler language.
     */
    private final String name;

    /**
     * Creates an operator with the given code and assembly name.
     *
     * @param code Operator code.
     * @param name Operator name in assembler langugage.
     */
    Operator(final String code, final String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Returns the code of this operator.
     *
     * @return Code of this operator.
     */
    String code() {
        return code;
    }

    @Override
    public String toString() {
        return name;
    }
}
