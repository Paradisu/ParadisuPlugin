package net.paradisu.velocity.util;

import lombok.AllArgsConstructor;
import net.paradisu.core.utils.ParadisuLogger;
import org.slf4j.Logger;

@AllArgsConstructor
public class VelocityLogger implements ParadisuLogger {
    private final Logger logger;

    @Override
    public void severe(String message) {
        logger.error(message);
    }

    @Override
    public void severe(String message, Throwable error) {
        logger.error(message, error);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void error(String message, Throwable error) {
        logger.error(message, error);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void warn(String message, Throwable error) {
        logger.warn(message, error);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }
}
