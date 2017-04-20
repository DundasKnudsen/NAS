
/**
 * Copyright 1997-2015 Stefan Nilsson, 2015-2017 Douglas Wikstrom.
 * This file is part of the NIC/NAS software licensed under BSD
 * License 2.0. See LICENSE file.
 */

package se.kth.csc.nas;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

/**
 * Container class for parsing results.
 */
class IntermediateFormat {

    /**
     * Lines of source code.
     */
    List<String> lines;

    /**
     * Error log.
     */
    ErrorLog errorLog;

    /**
     * Table of symbols.
     */
    SymbolTable symbolTable;

    /**
     * List of instructions.
     */
    InstructionList instructionList;

    /**
     * Creates container for parsing results.
     *
     * @param lines Lines of source code.
     * @param errorLog Error log.
     * @param symbolTable Table of symbols.
     * @param instructionList List of instructions.
     */
    IntermediateFormat(final List<String> lines,
                       final ErrorLog errorLog,
                       final SymbolTable symbolTable,
                       final InstructionList instructionList) {
        this.lines = lines;
        this.errorLog = errorLog;
        this.symbolTable = symbolTable;
        this.instructionList = instructionList;
    }

    /**
     * Generates binary code to the given binary writer.
     *
     * @param biw Binary writer.
     */
    void generateBinary(final BiWriter biw) {

        // Magic words to recognize our executable files.
        biw.printCode(0x1f1f);
        biw.printCode(0x1f1f);

        // Relocate words to the end of program + halt + jump to 0
        symbolTable.relocate(4 * instructionList.size() + 8);

        // Relocate symbolic values used in instructions using the
        // symbol table.
        instructionList.relocate(symbolTable);

        // Print instructions.
        instructionList.printBinary(biw);

        // Halt and jump to address 0 at the end.
        biw.printCode(0x0000);
        biw.printCode(0xf000);

        // Print symbol table at the end of the program.
        symbolTable.print(biw);

        // Indicate end of executable file.
        biw.flush();
    }

    /**
     * Generates binary code to the given writer.
     *
     * @param w Writer.
     */
    void generateBinary(final Writer w) {
        final PrintWriter pw = new PrintWriter(w);
        final BiWriter biw = new BiWriter(pw);
        generateBinary(biw);
    }

    /**
     * Generates binary code.
     *
     * @return Binary program.
     */
    String generateBinary() {
        StringWriter sw = new StringWriter();
        try {
            generateBinary(sw);
            final String executable = sw.toString();
            if (executable.length() > 256) {
                errorLog.error(String.format("Executable is too long! "
                                             + "(%d > 256)",
                                             executable.length()));
                return null;
            } else {
                return executable;
            }
        } catch (final TooManyErrorsException tmee) {
            return null;
        }
    }

    /**
     * Returns the number of errors logged.
     *
     * @return Number of errors logged.
     */
    public int getNoErrors() {
        return errorLog.getNoErrors();
    }

    /**
     * Generates error log as a string.
     *
     * @param label Label of source code.
     * @return Error log as a string.
     */
    String generateErrors(final String label) {
        return errorLog.toString(label);
    }
}
