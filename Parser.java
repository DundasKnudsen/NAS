
/**
 * Copyright 1997-2015 Stefan Nilsson, 2015-2017 Douglas Wikstrom.
 * This file is part of the NIC/NAS software licensed under BSD
 * License 2.0. See LICENSE file.
 */

package se.kth.csc.nas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Parser for assembler source files.
 */
public class Parser {

    /**
     * Number of bits in each block.
     */
    final static int BLOCKSIZE = 4;

    /**
     * Lines of source code.
     */
    final List<String> lines;

    /**
     * Error log.
     */
    final ErrorLog errorLog;

    /**
     * Table of symbols.
     */
    final SymbolTable symbolTable;

    /**
     * List of instructions.
     */
    final InstructionList instructionList;

    /**
     * Lexical analyzer.
     */
    final Lex lex;

    /**
     * Creates a parser with the given error log.
     *
     * @param maxErrors Maximal number of errors logged before
     * aborting.
     */
    public Parser(final int maxErrors) {
        this.lines = new ArrayList<String>();
        this.errorLog = new ErrorLog(lines, maxErrors);
        this.symbolTable = new SymbolTable();
        this.instructionList = new InstructionList(errorLog);
        this.lex = new Lex(BLOCKSIZE, errorLog);
    }

    /**
     * Parses a label and stores it in the symbol table.
     *
     * @param s String expected to be a label.
     */
    void parseLabel(final String s) {

        if (symbolTable.get(s) == null) {

            if (lex.isIdentifier(s)) {
                final Symbol symbol = new Symbol(SymbolType.LABEL, s);
                symbol.setAddress(4 * instructionList.size());
                symbolTable.put(symbol);
            } else {
                errorLog.error("Invalid name! (%s)", s);
            }
        } else {
            errorLog.error("Name already defined! (%s)", s);
        }
    }

    /**
     * Parses operators that take no arguments.
     *
     * @param operator Operator.
     * @param tokens Parameters (should only contain the name of the
     * operator).
     */
    void parseNoArgs(final Operator operator, final String[] tokens) {
        if (tokens.length == 1) {

            int t;
            switch (operator) {

            case HALT:
                t = 0;
                break;
            case NOOP:
                t = 1;
                break;
            default:
                throw new Error("Illegal invocation! This is a bug!");
            }

            final Instruction ins =
                new Instruction(lines.size(), operator, 0, 0, t, null);
            instructionList.add(ins);

        } else if (tokens.length > 1) {

            errorLog.error("Unexpected operand! (%s)", tokens[1]);
        } else {

            throw new Error("Illegal invocation! This is a bug!");
        }
    }

    /**
     * Parses a value that may either be a hexidecimal or decimal
     * constant, or defined relative a symbolic address. In the latter
     * case the result is an offset symbolic value. This can be loaded
     * into a register or used as an address.
     *
     * @param s String representation of a value.
     * @return Constant or symbolic value defined using an offset.
     */
    Value parseValue(final String s) {

        int n = 0;
        String name = "";
        int offset = 0;

        final char first = s.charAt(0);

        // Decimal and hexadecimal constant values start with '-' or a
        // decimal digit.
        if (first == '-' || ('0' <= first && first <= '9')) {

            n = lex.parseIntBounded(s, 2);

        // Symbolic value, or symbolic value with offset.
        } else {

            final int middle = Math.max(s.indexOf("+"), s.indexOf("-"));

            if (middle >= 0) {
                name = s.substring(0, middle);

                String oString;
                if (s.charAt(middle) == '-') {
                    oString = s.substring(middle);
                } else {
                    oString = s.substring(middle + 1);
                }
                offset = lex.parseDecBounded(oString, 2);

            } else {
                name = s;
            }
        }

        // Can never be converted to a value.
        if (n == NAS.INT_ERR || offset == NAS.INT_ERR) {
            return null;

        // Constant value or symbolic value (assuming that the name is
        // associated with a value.
        } else if (name.equals("") || lex.isIdentifier(name)) {

            return new Value(name, n, offset);

        // Name is not an identifier at all and can not be associated
        // with a value anywhere else.
        } else {
            errorLog.error("Invalid name! (%s)", name);
            return null;
        }
    }

    /**
     * Parses an unconditional jump instruction.
     *
     * @param operator Jump operator.
     * @param tokens Parameters (must be a single value).
     */
    void parseJump(final Operator operator, final String[] tokens) {
        if (tokens.length == 2) {

            final Value v = parseValue(tokens[1]);
            if (v == null) {
                return;
            } else {
                v.useHexFormat();
                final Instruction ins =
                    new Instruction(lines.size(), operator, 0, 0, 0, v);
                instructionList.add(ins);
            }
        } else {
            errorLog.error("Need exactly one value after operator! (%s)",
                           operator.toString());
        }
    }

    /**
     * Parses an operator that takes a register and a value as
     * parameters.
     *
     * @param operator Operator.
     * @param tokens Parameters to operator.
     */
    void parseRegValue(final Operator operator, final String[] tokens) {
        if (tokens.length == 3) {

            final int r = lex.parseReg(tokens[1]);
            final Value v = parseValue(tokens[2]);

            if (r == NAS.INT_ERR || v == null) {
                return;
            } else {

                // Print addresses in hexadecimal format.
                switch (operator) {
                case LOAD:
                case STORE:
                case JUMPE:
                case JUMPN:
                case JUMPL:
                case JUMPLE:
                    v.useHexFormat();
                    break;
                }

                final Instruction ins =
                    new Instruction(lines.size(), operator, r, 0, 0, v);
                instructionList.add(ins);
            }
        } else {
            errorLog.error("Need register and value after operator! (%s)",
                           operator.toString());
        }
    }

    /**
     * Parses an operator that takes two registers as parameters.
     *
     * @param operator Operator.
     * @param tokens Parameters to operator.
     */
    void parseRegReg(final Operator operator, final String[] tokens) {
        if (tokens.length == 3) {

            final int r = lex.parseReg(tokens[1]);
            final int s = lex.parseReg(tokens[2]);

            if (r != NAS.INT_ERR && s != NAS.INT_ERR) {
                final Instruction ins =
                    new Instruction(lines.size(), operator, 0, r, s, null);
                instructionList.add(ins);
            }
        } else {
            errorLog.error("Need two registers after operator! (%s)",
                           operator.toString());
        }
    }

    /**
     * Parses an operator that takes three registers as parameters.
     *
     * @param operator Operator.
     * @param tokens Parameters to operator.
     */
    void parseRegRegReg(final Operator operator, final String[] tokens) {
        if (tokens.length == 4) {

            final int r = lex.parseReg(tokens[1]);
            final int s = lex.parseReg(tokens[2]);
            final int t = lex.parseReg(tokens[3]);

            if (r != NAS.INT_ERR && s != NAS.INT_ERR && t != NAS.INT_ERR) {
                final Instruction ins =
                    new Instruction(lines.size(), operator, r, s, t, null);
                instructionList.add(ins);
            }
        } else {
            errorLog.error("Need three registers after operator! (%s)",
                           operator.toString());
        }
    }

    /**
     * Parses an instruction.
     *
     * @param operator Operator.
     * @param tokens Parameters to operator.
     */
    void parseInstruction(final Operator operator, final String[] tokens) {
        switch (operator) {

        // Operators taking no arguments.
        case NOOP:
        case HALT:
            parseNoArgs(operator, tokens);
            break;
        case JUMP:
            parseJump(operator, tokens);
            break;
        case LOAD:
        case LOADC:
        case STORE:
        case ADDC:
        case JUMPE:
        case JUMPN:
        case JUMPL:
        case JUMPLE:
            parseRegValue(operator, tokens);
            break;
        case LOADR:
        case STORER:
        case MOVE:
            parseRegReg(operator, tokens);
            break;
        default:
            parseRegRegReg(operator, tokens);
            break;
        }
    }

    /**
     * Parses the values following a directive and returns the
     * corresponding integer values. If no values are given, then an
     * array of length one containing the zero value is
     * returned. Invalid values are replaced by zero.
     *
     * @param type Type of directive, which must be word or code.
     * @param tokens Tokens on input line.
     */
    int[] parseDirectiveValues(final SymbolType type, final String[] tokens) {

        final int noValues = Math.max(tokens.length - 2, 1);
        final int[] values = new int[noValues];

        for (int i = 2; i < tokens.length; i++) {

            int n;
            if (type == SymbolType.WORD) {
                n = lex.parseIntBounded(tokens[i], 2);
            } else { // CODE
                n = lex.parseIntBounded(tokens[i], 4);
            }

            values[i - 2] = (n != NAS.INT_ERR) ? n : 0;
        }
        return values;
    }

    /**
     * Parse variable number of values for a word/code directive.
     *
     * @param type Type of symbol, which must be either word or code.
     * @param tokens Tokens on the line.
     */
    void parseDirective(final SymbolType type, final String[] tokens) {
        if (tokens.length < 2) {
            final String e =
                String.format("Need name after directive! (%s)", tokens[0]);
            errorLog.error(e);
            return;
        }

        final String name = tokens[1];

        if (symbolTable.get(name) == null) {

            if (lex.isIdentifier(name)) {

                final int[] values = parseDirectiveValues(type, tokens);
                final Symbol symbol = new Symbol(type, name, values);
                symbolTable.put(symbol);

            } else {
                errorLog.error("Invalid name!", name);
            }
        } else {
            errorLog.error("Name already defined", name);
        }
    }

    /**
     * Parses the given line and updates the symbol table or
     * instruction list.
     *
     * @param line Line of input.
     */
    void parseLine(final String[] tokens) {

        String[] ctokens = tokens;
        String first = ctokens[0];

        // Does the line contain a leading label?
        if (first.endsWith(":")) {

            parseLabel(first.substring(0, first.length() - 1));

            // Labels may preceed other content, so we need to leave
            // the rest in place if any as if there was no label.
            if (tokens.length > 1) {
                ctokens = Arrays.copyOfRange(tokens, 1, tokens.length);
                first = ctokens[0];
            } else {
                return;
            }
        }

        // Parse word, code directive, or an instruction line.
        if (first.equals(SymbolType.WORD.name)) {

            parseDirective(SymbolType.WORD, ctokens);

        } else if (first.equals(SymbolType.CODE.name)) {

            parseDirective(SymbolType.CODE, ctokens);

        } else {

            final Operator operator = Operators.get(first);
            if (operator == null) {
                errorLog.error("Unknown instruction! (%s)", first);
            } else {
                parseInstruction(operator, ctokens);
            }
        }
    }

    /**
     * Parses the assembler program.
     *
     * @param source Assembler source.
     */
    public IntermediateFormat parse(final BufferedReader source)
        throws IOException {

        String line = source.readLine();

        try {
            while (line != null) {
                lines.add(line);

                String[] tokens = lex.tokenizeLine(line);
                if (tokens != null) {
                    parseLine(tokens);
                }
                line = source.readLine();
            }
        } catch (TooManyErrorsException tmee) {
        }

        return new IntermediateFormat(lines, errorLog, symbolTable,
                                      instructionList);
    }

    /**
     * Parses the assembler program and returns the resulting symbol
     * table and list of instructions.
     *
     * @param source Assembler source.
     */
    public IntermediateFormat parse(final String source) throws IOException {
        final StringReader sr = new StringReader(source);
        BufferedReader br = null;
        IntermediateFormat inf = null;
        try {
            br = new BufferedReader(sr);
            inf = parse(br);
        } catch (final IOException ioe) {
            errorLog.error(ioe, "Unable to read from source string!");
        } finally {
            if (br != null) {
                br.close();
            }
        }
        return inf;
    }
}
