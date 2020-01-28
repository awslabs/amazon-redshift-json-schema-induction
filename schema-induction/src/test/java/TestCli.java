import aws.json.schema.induction.Cli;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestCli {
    @BeforeEach
    void setUp() {
        try {
            String outDir = "target/tmp";
            Files.createDirectories(Paths.get(outDir));
            purgeDirectory(new File(outDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void purgeDirectory(File dir) {
        for (File file: dir.listFiles()) {
            if (file.isDirectory())
                purgeDirectory(file);
            file.delete();
        }
    }
    @Test
    public void readClaims() {
        List<String> args = Stream.of("-i ", "../data/fhir/claims.json",
                "-s", "target/tmp/claims_tree.json",
                "-d", "target/tmp/claims.ddl",
                "-c", "profile",
                "-t", "FHIR_TEST_DB.Claims",
                "-l", "target/tmp/json.json",
                "-r", "us-east-1",
                "-root", "Claim",
                "-a").collect(Collectors.toList());
        int exitCode = 0;
        try {
            exitCode = new Cli().run(args.toArray(new String[0]));
            Assertions.assertEquals(0, exitCode);
            File res = new File("target/tmp/claims.ddl");
            File expected = new File("src/test/expected/claims.ddl");
            Assertions.assertTrue(FileUtils.contentEquals(res,expected),"Generated DDL is not expected!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testHelp() throws Exception {
        List<String> args = Stream.of("-h").collect(Collectors.toList());

        int exitCode = new Cli().run(args.toArray(new String[0]));
        Assertions.assertEquals(0, exitCode);
    }
}
