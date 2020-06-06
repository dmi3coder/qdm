package net.quarkify.qdm;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import net.quarkify.qdm.upgrade.UpgradeCommand;

import static picocli.CommandLine.Command;

@TopCommand
@Command(mixinStandardHelpOptions = true,
        version = "1.0.0",
        subcommands = UpgradeCommand.class
)
public class QdmCommand implements Runnable {
    @Override
    public void run() {
    }
}
