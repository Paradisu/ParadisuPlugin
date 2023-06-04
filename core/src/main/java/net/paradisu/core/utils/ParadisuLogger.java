package net.paradisu.core.utils;

public interface ParadisuLogger {
    /**
     * Logs a severe message to console
     *
     * @param message the message to log
     */
    public void severe(String message);

    /**
     * Logs a severe message and an exception to console
     *
     * @param message the message to log
     * @param error the error to throw
     */
    public void severe(String message, Throwable error);

    /**
     * Logs an error message to console
     *
     * @param message the message to log
     */
    public void error(String message);

    /**
     * Logs an error message and an exception to console
     *
     * @param message the message to log
     * @param error the error to throw
     */
    public void error(String message, Throwable error);

    /**
     * Logs a warning message to console
     *
     * @param message the message to log
     */
    public void warn(String message);

    /**
     * Logs a warning message to console
     *
     * @param message the message to log
     */
    public void warn(String message, Throwable error);

    /**
     * Logs an info message to console
     *
     * @param message the message to log
     */
    public void info(String message);
}
