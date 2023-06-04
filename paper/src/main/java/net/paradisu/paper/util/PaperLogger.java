package net.paradisu.paper.util;

import lombok.AllArgsConstructor;
import net.paradisu.core.utils.ParadisuLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

@AllArgsConstructor
public class PaperLogger implements ParadisuLogger {
    private final Logger logger;

    @Override
    public void severe(String message) {
        logger.severe(message);
    }

    @Override
    public void severe(String message, Throwable error) {
        logger.log(Level.SEVERE, message, error);
    }

    @Override
    public void error(String message) {
        logger.warning(message);
    }

    @Override
    public void error(String message, Throwable error) {
        logger.log(Level.WARNING, message, error);
    }

    @Override
    public void warn(String message) {
        error(message);
    }

        @Override
    public void warn(String message, Throwable error) {
        error(message, error);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }
}
