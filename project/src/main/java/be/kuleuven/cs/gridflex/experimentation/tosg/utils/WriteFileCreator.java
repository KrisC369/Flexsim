package be.kuleuven.cs.gridflex.experimentation.tosg.utils;

import be.kuleuven.cs.gridflex.persistence.MapDBMemoizationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for creating the necessary write files and initializing them as memoization dbs.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public final class WriteFileCreator {

    private WriteFileCreator() {
    }

    private static final int SLEEP_TIME = 200;
    private static final Logger logger = LoggerFactory.getLogger(WriteFileCreator.class);

    /**
     * Thre runnable main method.
     *
     * @param args cmdline args.
     */
    public static void main(String[] args) {
        try {
            for (String file : args) {
                logger.info("Writing ouput caching files for {}", file);
                MapDBMemoizationContext.builder().setFileName(file).ensureFileExists().build();
                sleep(SLEEP_TIME);
            }
        } catch (InterruptedException e) {
            logger.error("Sleep after after file write got interrupted", e);
            System.exit(1);
        }
    }

    private static void sleep(long time) throws InterruptedException {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            logger.error("Interrupt caught", e);
            throw e;
        }
    }
}
