package edu.sysu.pmglab.simulation;

import edu.sysu.pmglab.commandParser.CommandLineProgram;
import edu.sysu.pmglab.commandParser.annotation.Synopsis;
import edu.sysu.pmglab.commandParser.annotation.option.Option;
import edu.sysu.pmglab.commandParser.annotation.option.OptionGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Wenjie Peng
 * @create 2024-08-20 05:50
 * @description
 */
@Synopsis(
        usage = {"simulate <input> [options]"},
        version = {"stable-2 (last edited on 2024.03.10)"}
)
@OptionGroup(
        name = "simulation options",
        items = {
                @Option(names = {"--length-threshold", "-lt"}, defaultTo = "50-5000",
                        type = {String.class}, format = "min_len-max_len",
                        description = {"Specify the SVs with min and max length."}
                ),
                @Option(names = {"--init-chromosome", "-ic"},
                        type = {String.class},
                        description = {"Clear all existing chromosomes."}
                ),
                @Option(names = {"--sv-type", "-st"},defaultTo = "INS,DEL,DUP,BND",
                        type = {String.class},
                        description = {"Specify which types will be simulated."}
                ),
                @Option(names = {"--extra-chromosome", "-ec"},
                        type = {String.class},
                        description = {"Specify which chromosomes will be simulated."}
                ),
        }
)

public class PopulationSimulationProgram extends CommandLineProgram {

    private static final Logger logger = LoggerFactory.getLogger("SDFA Simulation - Command Line");
    protected PopulationSimulationProgram(String[] args) {
        super(args);
    }

    @Override
    protected void work() throws Exception, Error {

    }
}
