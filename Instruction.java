
/**
 * Copyright 1997-2015 Stefan Nilsson, 2015-2017 Douglas Wikstrom.
 * This file is part of the NIC/NAS software licensed under BSD
 * License 2.0. See LICENSE file.
 */

package se.kth.csc.nas;

import java.util.Formatter;

/**
 * Abstract representation of an instruction.
 */
class Instruction {

    /**
     * Line number of source code where the instruction appeared.
     */
    int lineIndex;

    /**
     * Operator.
     */
    Operator operator;

    /**
     * Register index.
     */
    int r;

    /**
     * Register index.
     */
    int s;

    /**
     * Register index.
     */
    int t;

    /**
     * Value in case the two last blocks represent a value.
     */
    Value value;

    /**
     * Creates an instruction parsed from the given line number in the
     * assembler source, with the operator and components as
     * indicated. All parameters are not used for all operators.
     *
     * @param lineIndex Line number of instruction.
     * @param operator Operator.
     * @param r Register index.
     * @param s Register index.
     * @param t Register index.
     * @param value Value.
     */
    Instruction(final int lineIndex,
                final Operator operator,
                final int r,
                final int s,
                final int t,
                final Value value) {
        this.lineIndex = lineIndex;
        this.operator = operator;
        this.r = r;
        this.s = s;
        this.t = t;
        this.value = value;
    }

    /**
     * Print a binary representation of this instruction.
     *
     * @param biw Binary writer.
     */
    void printBinary(final BiWriter biw) {

        int v = value == null ? 0 : value.constant;
        int b = 0;

        biw.printOperator(operator);

        switch (operator) {
        case JUMPLE:
            b++;
            // fall through
        case JUMPL:
            b++;
            // fall through
        case JUMPN:
            b++;
            // fall through
        case JUMPE:
        case JUMP:
        case LOAD:
        case LOADC:
        case STORE:
        case ADDC:
            biw.printByte(r);
            biw.printWord(v + b);
            break;
        default:
            biw.printByte(r);
            biw.printByte(s);
            biw.printByte(t);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final Formatter fmt = new Formatter(sb);

        fmt.format("%-7s", operator);

        switch (operator) {
        case HALT:
        case NOOP:
            break;
        case JUMP:
            fmt.format("%s", value);
            break;
        case LOAD:
        case LOADC:
        case STORE:
        case ADDC:
        case JUMPE:
        case JUMPN:
        case JUMPL:
        case JUMPLE:
            fmt.format("r%x %s", r, value);
            break;
        case LOADR:
        case STORER:
        case MOVE:
            fmt.format("r%x r%x", s, t);
            break;
        default:
            fmt.format("r%x r%x r%x", r, s, t);
            break;
        }

        return sb.toString();
    }
}
