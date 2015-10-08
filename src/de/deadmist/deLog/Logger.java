package de.deadmist.deLog;

import java.io.*;
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Provides simple logging to both a log file and the standard output
 *
 * @author Deadmist
 */
public class Logger {

    private static File logFile = new File("log.log");
    private static FileChannel channel;
    private static boolean debugEnabled, infoEnabled, errorEnabled, warningEnabled;
    private static boolean debug2StdOut, info2StdOut, error2StdOut, warning2Stdout;
    private static BufferedWriter writer;
    private static PrintWriter printWriter;

    /**
     * Logs a warning with attached stacktrace of an exception<br>
     * Only does something if {@link #setWarningEnabled(boolean) setWarningEnabled} is set to true
     *
     * @param tag     Tag for this message
     * @param message Your log message
     * @param ex      Exception to attach
     */
    public static void w(String tag, String message, Exception ex) {
        if (!warningEnabled) return;
        write(tag, "WARN", message, ex, warning2Stdout);
    }

    /**
     * Logs a warning<br>
     * Only does something if {@link #setWarningEnabled(boolean) setWarningEnabled} is set to true
     *
     * @param tag     Tag for this message
     * @param message Your log message
     */
    public static void w(String tag, String message) {
        if (!warningEnabled) return;
        w(tag, message, null);
    }

    /**
     * Logs an error with attached stacktrace of an exception<br>
     * Only does something if {@link #setErrorEnabled(boolean) setErrorEnabled} is set to true
     *
     * @param tag     Tag for this message
     * @param message Error message
     * @param ex      Exception to attach
     */
    public static void e(String tag, String message, Exception ex) {
        if (!errorEnabled) return;
        write(tag, "ERROR", message, ex, error2StdOut);
    }

    /**
     * Logs an error<br>
     * Only does something if {@link #setErrorEnabled(boolean) setErrorEnabled} is set to true
     *
     * @param tag     Tag for this message
     * @param message Error message
     */
    public static void e(String tag, String message) {
        if (!errorEnabled) return;
        e(tag, message, null);
    }

    /**
     * Logs an information message and attaches the stacktrace of an exception<br>
     * Only does something if {@link #setInfoEnabled(boolean) setInfoEnabled} is set to true
     *
     * @param tag     Tag for this message
     * @param message Message to log
     * @param ex      Exception to attach
     */
    public static void i(String tag, String message, Exception ex) {
        if (!infoEnabled) return;
        write(tag, "INFO", message, ex, info2StdOut);
    }

    /**
     * Logs an information message<br>
     * Only does something if {@link #setInfoEnabled(boolean) setInfoEnabled} is set to true
     *
     * @param tag     Tag for this message
     * @param message Message to log
     */
    public static void i(String tag, String message) {
        if (!infoEnabled) return;
        i(tag, message, null);
    }

    /**
     * Logs a debug message and attaches the stacktrace of an exception<br>
     * Only does something if {@link #setDebugEnabled(boolean) setDebugEnabled} is set to true
     *
     * @param tag     Tag for this message
     * @param message Message to log
     * @param ex      Exception to attach
     */
    public static void d(String tag, String message, Exception ex) {
        if (!debugEnabled) return;
        write(tag, "DEBUG", message, ex, debug2StdOut);
    }

    /**
     * Logs a debug message<br>
     * Only does something if {@link #setDebugEnabled(boolean) setDebugEnabled} is set to true
     *
     * @param tag     Tag for this message
     * @param message Message to log
     */
    public static void d(String tag, String message) {
        if (!debugEnabled) return;
        d(tag, message, null);
    }

    /**
     * Enables logging on this level and all higher levels<br>
     * The order is, from highest to lowest:<br>
     * ERROR -&gt; WARNING -&gt; INFO -&gt; DEBUG -&gt; NONE
     *
     * @param level The desired log level or "None" to turn of logging
     * @throws IOException Thrown if the log file could not be opened
     */
    public static void setLogLevel(String level) throws IOException {
        switch (level.toLowerCase()) {
            case "none":
                setDebugEnabled(false);
                setInfoEnabled(false);
                setWarningEnabled(false);
                setErrorEnabled(false);
                break;
            case "error":
                setDebugEnabled(false);
                setInfoEnabled(false);
                setWarningEnabled(false);
                setErrorEnabled(true);
                break;
            case "warning":
                setDebugEnabled(false);
                setInfoEnabled(false);
                setWarningEnabled(true);
                setErrorEnabled(true);
                break;
            case "info":
                setDebugEnabled(false);
                setInfoEnabled(true);
                setWarningEnabled(true);
                setErrorEnabled(true);
                break;
            case "debug":
                setDebugEnabled(true);
                setInfoEnabled(true);
                setWarningEnabled(true);
                setErrorEnabled(true);
                break;
            default:
                throw new IllegalArgumentException("LogLevel not recognized");
        }
    }

    /**
     * Assembles the log from tag, level, message and (optionally) exception and writes it to disk and stdOut
     *
     * @param tag           Tag for this message, use to group messages
     * @param level         The severity of the message
     * @param message       The message body it self
     * @param ex            Optional, an exception that gets logged with the message. Leave null for no exception
     * @param printToSdtOut If the message should also be displayed on standard output
     */
    private static void write(String tag, String level, String message, Exception ex, boolean printToSdtOut) {
        String date = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss").format(LocalDateTime.now());
        String out = String.format("%s [%s] (%s) %s\n", date, level, tag, message);

        if (writer == null || printWriter == null) {
            System.err.printf("%s [ERROR] (LOGGING) Logfile was not correctly opened!\n", date);
            return;
        }

        try {
            writer.write(out);
            writer.flush();
            if (printToSdtOut) System.out.printf("%s", out);
            if (ex != null) {
                ex.printStackTrace(printWriter);
                if (printToSdtOut) ex.printStackTrace();
            }
        } catch (IOException e) {
            System.err.printf("%s [ERROR] (LOGGING) Could not write to log file: %s\n", date, logFile.getAbsoluteFile());
            e.printStackTrace();
        }
    }

    /**
     * Enables displaying of all messages to standard output
     *
     * @param enable Enable/disable displaying
     */
    public static void setAll2StdOut(boolean enable) {
        debug2StdOut = enable;
        info2StdOut = enable;
        error2StdOut = enable;
        warning2Stdout = enable;
    }

    /**
     * Enables logging of debug messages, otherwise they are silently discarded
     *
     * @param enabled Enabled/disable debug messages
     * @throws IOException Thrown when something goes wrong opening the log file
     */
    public static void setDebugEnabled(boolean enabled) throws IOException {
        debugEnabled = enabled;
        if (writer == null || printWriter == null) open();
    }

    /**
     * Enables logging of error messages, otherwise they are silently discarded
     *
     * @param enabled Enabled/disable error messages
     * @throws IOException Thrown when something goes wrong opening the log file
     */
    public static void setErrorEnabled(boolean enabled) throws IOException {
        errorEnabled = enabled;
        if (writer == null || printWriter == null) open();
    }

    /**
     * Enables logging of info messages, otherwise they are silently discarded
     *
     * @param enabled Enabled/disable info messages
     * @throws IOException Thrown when something goes wrong opening the log file
     */
    public static void setInfoEnabled(boolean enabled) throws IOException {
        infoEnabled = enabled;
        if (writer == null || printWriter == null) open();
    }

    /**
     * Enables logging of warning messages, otherwise they are silently discarded
     *
     * @param enabled Enabled/disable warning messages
     * @throws IOException Thrown when something goes wrong opening the log file
     */
    public static void setWarningEnabled(boolean enabled) throws IOException {
        warningEnabled = enabled;
        if (writer == null || printWriter == null) open();
    }

    /**
     * Enables displaying of warning messages to the standard output (i.e. the console)<br>
     * Messages get logged to file regardless of this setting
     *
     * @param enable Enable/disable display on stdOut
     */
    public static void setWarning2Stdout(boolean enable) {
        Logger.warning2Stdout = enable;
    }

    /**
     * Enables displaying of info messages to the standard output (i.e. the console)<br>
     * Messages get logged to file regardless of this setting
     *
     * @param enable Enable/disable display on stdOut
     */
    public static void setInfo2StdOut(boolean enable) {
        Logger.info2StdOut = enable;
    }

    /**
     * Enables displaying of error messages to the standard output (i.e. the console)<br>
     * Messages get logged to file regardless of this setting
     *
     * @param enable Enable/disable display on stdOut
     */
    public static void setError2StdOut(boolean enable) {
        Logger.error2StdOut = enable;
    }

    /**
     * Enables displaying of debug messages to the standard output (i.e. the console)<br>
     * Messages get logged to file regardless of this setting
     *
     * @param enable Enable/disable display on stdOut
     */
    public static void setDebug2StdOut(boolean enable) {
        Logger.debug2StdOut = enable;
    }

    /**
     * Sets the path to the log file
     *
     * @param path Path to new log file
     * @throws IOException Thrown when something goes wrong closing the old file or opening the new one
     */
    public static void setLogFile(String path) throws IOException {
        logFile = new File(path);
        if (writer != null || printWriter != null) {
            close();
            open();
        }
    }

    /**
     * Opens the log file before logging can be done
     *
     * @throws IOException Thrown when something goes wrong opening the file
     */
    public static void open() throws IOException {
        writer = new BufferedWriter(new FileWriter(logFile, true));
        printWriter = new PrintWriter(writer);
        channel = new RandomAccessFile(logFile, "rw").getChannel();
    }

    /**
     * Closes the log file
     *
     * @throws IOException Thrown when something goes wrong closing the file
     */
    public static void close() throws IOException {
        if (printWriter != null) printWriter.close();
        if (writer != null) writer.close();

        channel.close();

        printWriter = null;
        writer = null;
    }

    //There is no use in creating an object
    private Logger() {}

}
