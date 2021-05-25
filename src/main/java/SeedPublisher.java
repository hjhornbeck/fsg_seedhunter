import picocli.CommandLine;             // used to parse the command line
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;                    // points to the config file
import java.util.concurrent.Callable;   // allows
import java.util.Properties;            // read our config file

/**
 * The brains of FSG SeedHunter. Acts as an intermediary between the server
 *  and the seed finding code, directing the search for seeds and coordinating
 *  which seeds to find by communicating with the server.
 */
@Command(name = "SeedPublisher",
         mixinStandardHelpOptions = true,
         version = "0.1",
         description = "Mines for Minecraft seeds and banks them for later use.")
public class SeedPublisher implements Callable<Integer> {

    @Option(names = {"-c", "--config"}, description = "Location of the configuration file.")
    private File config = new File("SeedPublisher.config");

    @Option(names = {"-t", "--threads"}, description = "Number of threads to use. Zero or negative implies all available.")
    private int threads = 0;

    @Option(names = {"-s", "--seeds"}, description = "Find this many seeds, then exit. Zero runs indefinitely.")
    private int seeds = 0;


    /**
     * The main entry point for execution.
     *
     * @return An integer error code, or zero if no error occurred.
     * @throws Exception if there was an unexpected error.
     */
    @Override
    public Integer call() throws Exception {

        // read in the config file
        // while (forever)

        return 0;
    }

    /**
     * A thin wrapper around <code>call()</code>.
     * @param args The command-line arguments.
     * @since 0.1
     */
    public static void main(String... args) {
        int exitCode = new CommandLine(new SeedPublisher()).execute(args);
        System.exit(exitCode);
    }
}
