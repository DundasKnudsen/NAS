
/**
 * Copyright 1997-2015 Stefan Nilsson, 2015-2017 Douglas Wikstrom.
 * This file is part of the NIC/NAS software licensed under BSD
 * License 2.0. See LICENSE file.
 */

package se.kth.csc.nas;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores list of instructions.
 */
class InstructionList {

    /**
     * Underlying list of instructions.
     */
    final List<Instruction> instructions;

    /**
     * Error log.
     */
    final ErrorLog errorLog;

    /**
     * Creates an empty instruction list.
     *
     * @param errorLog Error log.
     */
    InstructionList(final ErrorLog errorLog) {
        this.instructions = new ArrayList<Instruction>();
        this.errorLog = errorLog;
    }

    /**
     * Stores the given instruction.
     *
     * @param instruction Instruction to be stored.
     */
    void add(final Instruction instruction) {
        instructions.add(instruction);
    }

    /**
     * Returns the number of instructions in the table.
     *
     * @return Number of instructions in the table.
     */
    int size() {
        return instructions.size();
    }

    /**
     * Relocates the given instruction relative the symbol table if
     * needed. Alignment requirements is indicated by the booleans.
     *
     * @param instruction Instruction to relocate.
     * @param symbolTable Symbol table.
     * @param align2 Indicates 2-alignment.
     * @param align4 Indicates 4-alignment.
     */
    void relocateInstruction(final Instruction instruction,
                             final SymbolTable symbolTable,
                             final boolean align2,
                             final boolean align4) {

        final String name = instruction.value.name;

        // Value is explicit, so there is no need to relocate.
        if (name.equals("")) {
            return;
        }

        final Symbol symbol = symbolTable.get(name);
        if (symbol == null) {
            errorLog.error(instruction.lineIndex, "Undefined name! (%s)", name);
        } else {
            final int res =
                (instruction.value.constant
                 + symbol.address
                 + instruction.value.offset) % 0x100;

            if (res < 0 || res > 0xff) {
                final String an = align2 ? "Adress" : "Number";
                errorLog.error(instruction.lineIndex,
                               "%s is out of range! (%s)",
                               an, "" + instruction.value);
            }

            if (align4 && res % 4 != 0 || align2 && res % 2 != 0) {
                final String format =
                    "Address is not aligned on %s-byte boundary! (%s)";

                final String alignment = align4 ? "4" : "2";

                errorLog.error(instruction.lineIndex,
                               format,
                               alignment,
                               instruction.value.toString());
            }

            instruction.value.name = "";
            instruction.value.constant = res;
            instruction.value.offset = 0;
        }
    }

    /**
     * Relocates the symbols in this table relative the given end of
     * program.
     *
     * @param name Name of symbol.
     */
    void relocate(final SymbolTable symbolTable) {

        for (Instruction instruction: instructions) {

            boolean align2 = false;
            boolean align4 = false;

            switch (instruction.operator) {
            case JUMP:
            case JUMPE:
            case JUMPN:
            case JUMPL:
            case JUMPLE:
                align4 = true;
                // fall through
            case LOAD:
            case STORE:
                align2 = true;
                // fall through
            case LOADC:
            case ADDC:

                relocateInstruction(instruction, symbolTable, align2, align4);
            }
        }
    }

    /**
     * Prints the instructions of this table to the binary writer.
     *
     * @param biw Binary writer.
     */
    void printBinary(final BiWriter biw) {
        for (Instruction instruction : instructions) {
            instruction.printBinary(biw);
        }
    }
}
