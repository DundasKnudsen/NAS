
/**
 * Copyright 1997-2015 Stefan Nilsson, 2015-2017 Douglas Wikstrom.
 * This file is part of the NIC/NAS software licensed under BSD
 * License 2.0. See LICENSE file.
 */

package se.kth.csc.nas;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.util.Arrays;

/**
 * Assembler for NIC.
 */
public final class NAS {

    /**
     * Integer representing an error.
     */
    public static int INT_ERR = Integer.MIN_VALUE;

    /**
     * Version of this software.
     */
    public static String VERSION =
        NAS.class.getPackage().getSpecificationVersion();

    /**
     * Default number of errors logged before aborting.
     */
    public final static int MAX_ERRORS = 10;

    /**
     * Generates the output filename from the input filename.
     *
     * @param inputFilename Input filename.
     * @param postfix Postfix for output filename.
     * @return Output filename.
     */
    protected static String outputFilename(final String inputFilename,
                                           final String postfix) {
        if (inputFilename.endsWith(".as")) {
            return inputFilename.replaceFirst(".as\\z", postfix);
        } else {
            return inputFilename + postfix;
        }
    }

    /**
     * Assembles the input assembly program to an executable for NIC.
     *
     * @param label Program label, e.g., the filename.
     * @param program Program to assemble.
     * @param maxErrors Maximal number of errors logged before
     * aborting.
     * @param errorStream Destination of error log.
     * @return Assembled executable.
     * @throws IOException If there was an IO problem.
     */
    public static String assemble(final String label,
                                  final String program,
                                  final int maxErrors,
                                  final PrintStream errorStream)
        throws IOException {

        final Parser parser = new Parser(maxErrors);
        final IntermediateFormat intermediateFormat = parser.parse(program);

        String executable;
        if (intermediateFormat.errorLog.getNoErrors() == 0) {
            executable = intermediateFormat.generateBinary();
        } else {
            executable = null;
        }

        if (errorStream != null && intermediateFormat.getNoErrors() > 0) {
            errorStream.print(intermediateFormat.generateErrors(label));
        }
        return executable;
    }

    /**
     * Assembles the input assembly program to an executable for NIC.
     *
     * @param inputFilename Program to assemble.
     * @param maxErrors Maximal number of errors logged before
     * aborting.
     * @param errorStream Destination of error log.
     * @return Assembled executable.
     * @throws IOException If there was an IO problem.
     */
    protected static String assemble(final String inputFilename,
                                     final int maxErrors,
                                     final PrintStream errorStream)
        throws IOException {
        final byte[] inputBytes = Files.readAllBytes(Paths.get(inputFilename));
        final String program = new String(inputBytes, StandardCharsets.UTF_8);
        final String executable =
            assemble(inputFilename, program, maxErrors, errorStream);

        if (executable != null) {
            final String outputFilename = outputFilename(inputFilename, ".bi");
            final byte[] outputBytes =
                executable.getBytes(StandardCharsets.UTF_8);
            Files.write(Paths.get(outputFilename), outputBytes,
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                        java.nio.file.StandardOpenOption.WRITE);
        }
        return executable;
    }

    /**
     * Assembles the input assembly program to an executable for NIC.
     *
     * @param inputFilename Program to assemble.
     * @param maxErrors Maximal number of errors logged before
     * aborting.
     * @param errorStream Destination of error log.
     * @return Assembled executable.
     * @throws IOException If there was an IO problem.
     */
    protected static boolean assembleKattis(final String inputFilename,
                                            final int maxErrors,
                                            final PrintStream errorStream)
        throws IOException {
        final byte[] inputBytes = Files.readAllBytes(Paths.get(inputFilename));
        final String program = new String(inputBytes, StandardCharsets.UTF_8);

        final String executable =
            assemble(inputFilename, program, maxErrors, errorStream);
        if (executable == null) {
            return false;
        } else {
            final String outputFilename =
                outputFilename(inputFilename, ".java");

            final String javaStringConstant =
                program.replaceAll("\n", "\\n").replaceAll("\"", "\\\"");
            final String javaProgram =
                Kattis.PROGRAM_TEMPLATE.replace("PROGRAM_PLACE_HOLDER",
                                                javaStringConstant);
            final byte[] outputBytes =
                javaProgram.getBytes(StandardCharsets.UTF_8);
            Files.write(Paths.get(outputFilename), outputBytes,
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                        java.nio.file.StandardOpenOption.WRITE);

            return true;
        }
    }

    /**
     * Print the message and exit with the exit code.
     *
     * @param message What to print.
     * @param exitCode Exit code of the program.
     */
    protected static void printExit(final String message, final int exitCode) {
        System.out.println(message);
        System.exit(0);
    }

    /**
     * Prints usage information.
     */
    protected static void printUsageInfo() {
        printExit("nas [-v|-h|-kattis] [-maxerr <number>] [<source>]\n"
                  + "-v      Print version.\n"
                  + "-h      Print usage information.\n"
                  // + "-kattis Output a Java file for use with Kattis.\n"
                  + "-maxerr Maximal number of errors.",
                  0);
    }

    /**
     * Prints an error message and exits with exit code 1.
     *
     * @param message What to print.
     */
    protected static void errorExit(final String message) {
        printExit("ERROR: " + message, 1);
    }

    /**
     * Command line interface for NAS.
     *
     * @param args Arguments to program.
     */
    public static void main(String[] args) {
        String e;
        int len = args.length;
        boolean executable = true;

        // Non-functional parameters.
        if (len == 0) {
            errorExit("No arguments given! (Use \"-h\" for help.)");
        } else if (len == 1) {
            if (args[0].equals("-h")) {
                printUsageInfo();
            } else if (args[0].equals("-v")) {
                printExit(VERSION, 0);
            // } else if (args[0].equals("-kattis")) {
            //     executable = false;
            //     args = Arrays.copyOfRange(args, 1, args.length);
            }
        }

        // Handle user provided bound on number of errors.
        int fileIndex = 0;
        int maxErrors = MAX_ERRORS;
        if (len == 3) {
            if (args[0].equals("-maxerr")) {
                fileIndex = 2;
                try {
                    maxErrors = Integer.parseInt(args[1]);
                } catch (NumberFormatException nfe) {
                    e = String.format("Maximal number of errors is not an "
                                      + "integer! (%s)", args[1]);
                    errorExit(e);
                }

                if (maxErrors < 0) {
                    e = String.format("Negative maximal number of errors! (%s)",
                                      maxErrors);
                    errorExit(e);
                }
            } else {
                e = String.format("Illegal parameters! (%s %s %s)",
                                  args[0], args[1], args[2]);
                errorExit(e);
            }
        }

        if (len - fileIndex > 1) {
            e = String.format("Too few or many arguments! (%s)", len);
            errorExit(e);
        }

        final String filepath = args[fileIndex];
        final File file = new File(filepath);

        if (!file.exists() || !file.canRead()) {
            e = String.format("Input file does not exist or is not readable! "
                              + "(%s)", filepath);
            errorExit(e);
        }

        try {
            assemble(filepath, maxErrors, System.out);
        } catch (IOException ioe) {
            throw new Error("Internal IO error!", ioe);
        }
    }
}

