/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.inputmethod.latin.dicttool;

import com.android.inputmethod.latin.makedict.BinaryDictInputOutput;
import com.android.inputmethod.latin.makedict.FormatSpec;
import com.android.inputmethod.latin.makedict.FusionDictionary;
import com.android.inputmethod.latin.makedict.MakedictLog;
import com.android.inputmethod.latin.makedict.UnsupportedFormatException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Main class/method for DictionaryMaker.
 */
public class DictionaryMaker {

    static class Arguments {
        private static final String OPTION_VERSION_1 = "-1";
        private static final String OPTION_VERSION_2 = "-2";
        private static final String OPTION_VERSION_3 = "-3";
        private static final String OPTION_INPUT_SOURCE = "-s";
        private static final String OPTION_INPUT_BIGRAM_XML = "-b";
        private static final String OPTION_INPUT_SHORTCUT_XML = "-c";
        private static final String OPTION_OUTPUT_BINARY = "-d";
        private static final String OPTION_OUTPUT_XML = "-x";
        private static final String OPTION_HELP = "-h";
        public final String mInputBinary;
        public final String mInputUnigramXml;
        public final String mInputShortcutXml;
        public final String mInputBigramXml;
        public final String mOutputBinary;
        public final String mOutputXml;
        public final int mOutputBinaryFormatVersion;

        private void checkIntegrity() throws IOException {
            checkHasExactlyOneInput();
            checkHasAtLeastOneOutput();
            checkNotSameFile(mInputBinary, mOutputBinary);
            checkNotSameFile(mInputBinary, mOutputXml);
            checkNotSameFile(mInputUnigramXml, mOutputBinary);
            checkNotSameFile(mInputUnigramXml, mOutputXml);
            checkNotSameFile(mInputShortcutXml, mOutputBinary);
            checkNotSameFile(mInputShortcutXml, mOutputXml);
            checkNotSameFile(mInputBigramXml, mOutputBinary);
            checkNotSameFile(mInputBigramXml, mOutputXml);
            checkNotSameFile(mOutputBinary, mOutputXml);
        }

        private void checkHasExactlyOneInput() {
            if (null == mInputUnigramXml && null == mInputBinary) {
                throw new RuntimeException("No input file specified");
            } else if (null != mInputUnigramXml && null != mInputBinary) {
                throw new RuntimeException("Both input XML and binary specified");
            } else if (null != mInputBinary && null != mInputBigramXml) {
                throw new RuntimeException("Cannot specify a binary input and a separate bigram "
                        + "file");
            }
        }

        private void checkHasAtLeastOneOutput() {
            if (null == mOutputBinary && null == mOutputXml) {
                throw new RuntimeException("No output specified");
            }
        }

        /**
         * Utility method that throws an exception if path1 and path2 point to the same file.
         */
        private static void checkNotSameFile(final String path1, final String path2)
                throws IOException {
            if (null == path1 || null == path2) return;
            if (new File(path1).getCanonicalPath().equals(new File(path2).getCanonicalPath())) {
                throw new RuntimeException(path1 + " and " + path2 + " are the same file: "
                        + " refusing to process.");
            }
        }

        private void displayHelp() {
            MakedictLog.i(getHelp());
        }

        public static String getHelp() {
            return "Usage: makedict "
                    + "[-s <unigrams.xml> [-b <bigrams.xml>] [-c <shortcuts_and_whitelist.xml>] "
                    + "| [-s <binary input>] [-d <binary output>] [-x <xml output>] "
                    + "[-1] [-2] [-3]\n"
                    + "\n"
                    + "  Converts a source dictionary file to one or several outputs.\n"
                    + "  Source can be an XML file, with an optional XML bigrams file, or a\n"
                    + "  binary dictionary file.\n"
                    + "  Binary version 1 (Ice Cream Sandwich), 2 (Jelly Bean), 3 and XML outputs\n"
                    + "  are supported. All three can be output at the same time, but the same\n"
                    + "  output format cannot be specified several times. The behavior is\n"
                    + "  unspecified if the same file is specified for input and output, or for\n"
                    + "  several outputs.";
        }

        public Arguments(String[] argsArray) throws IOException {
            final LinkedList<String> args = new LinkedList<String>(Arrays.asList(argsArray));
            if (args.isEmpty()) {
                displayHelp();
            }
            String inputBinary = null;
            String inputUnigramXml = null;
            String inputShortcutXml = null;
            String inputBigramXml = null;
            String outputBinary = null;
            String outputXml = null;
            int outputBinaryFormatVersion = 2; // the default version is 2.

            while (!args.isEmpty()) {
                final String arg = args.get(0);
                args.remove(0);
                if (arg.charAt(0) == '-') {
                    if (OPTION_VERSION_2.equals(arg)) {
                        // Do nothing, this is the default
                    } else if (OPTION_VERSION_3.equals(arg)) {
                        outputBinaryFormatVersion = 3;
                    } else if (OPTION_VERSION_1.equals(arg)) {
                        outputBinaryFormatVersion = 1;
                    } else if (OPTION_HELP.equals(arg)) {
                        displayHelp();
                    } else {
                        // All these options need an argument
                        if (args.isEmpty()) {
                            throw new IllegalArgumentException("Option " + arg + " is unknown or "
                                    + "requires an argument");
                        }
                        String filename = args.get(0);
                        args.remove(0);
                        if (OPTION_INPUT_SOURCE.equals(arg)) {
                            if (BinaryDictInputOutput.isBinaryDictionary(filename)) {
                                inputBinary = filename;
                            } else {
                                inputUnigramXml = filename;
                            }
                        } else if (OPTION_INPUT_SHORTCUT_XML.equals(arg)) {
                            inputShortcutXml = filename;
                        } else if (OPTION_INPUT_BIGRAM_XML.equals(arg)) {
                            inputBigramXml = filename;
                        } else if (OPTION_OUTPUT_BINARY.equals(arg)) {
                            outputBinary = filename;
                        } else if (OPTION_OUTPUT_XML.equals(arg)) {
                            outputXml = filename;
                        } else {
                            throw new IllegalArgumentException("Unknown option : " + arg);
                        }
                    }
                } else {
                    if (null == inputBinary && null == inputUnigramXml) {
                        if (BinaryDictInputOutput.isBinaryDictionary(arg)) {
                            inputBinary = arg;
                        } else {
                            inputUnigramXml = arg;
                        }
                    } else if (null == outputBinary) {
                        outputBinary = arg;
                    } else {
                        throw new IllegalArgumentException("Several output binary files specified");
                    }
                }
            }

            mInputBinary = inputBinary;
            mInputUnigramXml = inputUnigramXml;
            mInputShortcutXml = inputShortcutXml;
            mInputBigramXml = inputBigramXml;
            mOutputBinary = outputBinary;
            mOutputXml = outputXml;
            mOutputBinaryFormatVersion = outputBinaryFormatVersion;
            checkIntegrity();
        }
    }

    public static void main(String[] args)
            throws FileNotFoundException, ParserConfigurationException, SAXException, IOException,
            UnsupportedFormatException {
        final Arguments parsedArgs = new Arguments(args);
        FusionDictionary dictionary = readInputFromParsedArgs(parsedArgs);
        writeOutputToParsedArgs(parsedArgs, dictionary);
    }

    /**
     * Invoke the right input method according to args.
     *
     * @param args the parsed command line arguments.
     * @return the read dictionary.
     */
    private static FusionDictionary readInputFromParsedArgs(final Arguments args)
            throws IOException, UnsupportedFormatException, ParserConfigurationException,
            SAXException, FileNotFoundException {
        if (null != args.mInputBinary) {
            return readBinaryFile(args.mInputBinary);
        } else if (null != args.mInputUnigramXml) {
            return readXmlFile(args.mInputUnigramXml, args.mInputShortcutXml, args.mInputBigramXml);
        } else {
            throw new RuntimeException("No input file specified");
        }
    }

    /**
     * Read a dictionary from the name of a binary file.
     *
     * @param binaryFilename the name of the file in the binary dictionary format.
     * @return the read dictionary.
     * @throws FileNotFoundException if the file can't be found
     * @throws IOException if the input file can't be read
     * @throws UnsupportedFormatException if the binary file is not in the expected format
     */
    private static FusionDictionary readBinaryFile(final String binaryFilename)
            throws FileNotFoundException, IOException, UnsupportedFormatException {
        FileInputStream inStream = null;

        try {
            final File file = new File(binaryFilename);
            inStream = new FileInputStream(file);
            final ByteBuffer buffer = inStream.getChannel().map(
                    FileChannel.MapMode.READ_ONLY, 0, file.length());
            return BinaryDictInputOutput.readDictionaryBinary(
                    new BinaryDictInputOutput.ByteBufferWrapper(buffer), null);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
    }

    /**
     * Read a dictionary from a unigram XML file, and optionally a bigram XML file.
     *
     * @param unigramXmlFilename the name of the unigram XML file. May not be null.
     * @param shortcutXmlFilename the name of the shortcut/whitelist XML file, or null if none.
     * @param bigramXmlFilename the name of the bigram XML file. Pass null if there are no bigrams.
     * @return the read dictionary.
     * @throws FileNotFoundException if one of the files can't be found
     * @throws SAXException if one or more of the XML files is not well-formed
     * @throws IOException if one the input files can't be read
     * @throws ParserConfigurationException if the system can't create a SAX parser
     */
    private static FusionDictionary readXmlFile(final String unigramXmlFilename,
            final String shortcutXmlFilename, final String bigramXmlFilename)
            throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
        final FileInputStream unigrams = new FileInputStream(new File(unigramXmlFilename));
        final FileInputStream shortcuts = null == shortcutXmlFilename ? null :
                new FileInputStream(new File(shortcutXmlFilename));
        final FileInputStream bigrams = null == bigramXmlFilename ? null :
                new FileInputStream(new File(bigramXmlFilename));
        return XmlDictInputOutput.readDictionaryXml(unigrams, shortcuts, bigrams);
    }

    /**
     * Invoke the right output method according to args.
     *
     * This will write the passed dictionary to the file(s) passed in the command line arguments.
     * @param args the parsed arguments.
     * @param dict the file to output.
     * @throws FileNotFoundException if one of the output files can't be created.
     * @throws IOException if one of the output files can't be written to.
     */
    private static void writeOutputToParsedArgs(final Arguments args, final FusionDictionary dict)
            throws FileNotFoundException, IOException, UnsupportedFormatException,
            IllegalArgumentException {
        if (null != args.mOutputBinary) {
            writeBinaryDictionary(args.mOutputBinary, dict, args.mOutputBinaryFormatVersion);
        }
        if (null != args.mOutputXml) {
            writeXmlDictionary(args.mOutputXml, dict);
        }
    }

    /**
     * Write the dictionary in binary format to the specified filename.
     *
     * @param outputFilename the name of the file to write to.
     * @param dict the dictionary to write.
     * @param version the binary format version to use.
     * @throws FileNotFoundException if the output file can't be created.
     * @throws IOException if the output file can't be written to.
     */
    private static void writeBinaryDictionary(final String outputFilename,
            final FusionDictionary dict, final int version)
            throws FileNotFoundException, IOException, UnsupportedFormatException {
        final File outputFile = new File(outputFilename);
        final FormatSpec.FormatOptions formatOptions = new FormatSpec.FormatOptions(version);
        BinaryDictInputOutput.writeDictionaryBinary(new FileOutputStream(outputFilename), dict,
                formatOptions);
    }

    /**
     * Write the dictionary in XML format to the specified filename.
     *
     * @param outputFilename the name of the file to write to.
     * @param dict the dictionary to write.
     * @throws FileNotFoundException if the output file can't be created.
     * @throws IOException if the output file can't be written to.
     */
    private static void writeXmlDictionary(final String outputFilename,
            final FusionDictionary dict) throws FileNotFoundException, IOException {
        XmlDictInputOutput.writeDictionaryXml(new FileWriter(outputFilename), dict);
    }
}
