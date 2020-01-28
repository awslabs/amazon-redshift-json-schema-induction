package aws.json.schema.induction;

import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.scene.shape.Path;
import org.apache.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Cli {
    private static class Options {
        @Option(names = { "-i", "--input" }, required = true, description = "An input json file path.")
        String inputFile = null;

        @Option(names = { "-s", "--schema" }, description = "An output schema file json file path")
        String outSchemaFile = null;

        @Option(names = { "-d", "--ddl" }, description = "An output ddl file for Redshift")
        String outDdlFile = null;

        @Option(names = { "--stats" }, description = "An output stats file")
        String outStatsFile = null;

        @Option(names = { "-c", "--cred" }, description = "which type of s3 credentials to use (ec2|profile)")
        private String s3 = "ec2";

        @Option(names = { "-t", "--table" }, description = "table name to use when creating DDL")
        private String tableName = "";

        @Option(names = { "-l", "--location" }, description = "table location to use when creating DDL")
        private String tableLocation = "";

        @Option(names = { "-r", "--region" }, description = "s3 region to use")
        private String region = "";

        @Option(names = { "-root", "--root" }, description = "s3 region to use")
        private String rootDefinition = "";

        @Option(names = { "-a", "--array" }, description = "is the document a json array")
        private boolean isArray = false;

        @Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
        private boolean helpRequested = false;
    }
    final static Logger logger = Logger.getLogger(Cli.class);

    public static void main(String[] args) throws Exception {
        int exitCode = new Cli().run(args);
        System.exit(exitCode);
    }

    public int run(String[] args) throws Exception {
        Options opt = new Options();
        CommandLine cmd = new CommandLine(opt);
        CommandLine.ParseResult parseResult = cmd.parseArgs(args);
        if (!parseResult.errors().isEmpty()) {
            System.err.println(parseResult.errors());
            return -1;
        }
        if (cmd.isUsageHelpRequested()) {
            cmd.usage(System.out);
            return 0;
        }
        InputStreamReader inStream;
        if (opt.inputFile.startsWith("s3:")) {
            AmazonS3 s3;
            if (opt.s3.compareToIgnoreCase("profile") == 0) {
                s3 = AmazonS3Client.builder().withCredentials(new ProfileCredentialsProvider())
                        .withRegion(opt.region).build();
            } else {
                s3 = AmazonS3Client.builder().withCredentials(new EC2ContainerCredentialsProviderWrapper()).build();
            }
            final AmazonS3URI inputUri = new AmazonS3URI(opt.inputFile);
            String bucket_name = inputUri.getBucket();
            String key_name = inputUri.getKey();
            try {
                final S3Object s3Object = s3.getObject(bucket_name, key_name);
                final S3ObjectInputStream objectInputStream = s3Object.getObjectContent();
                inStream = new InputStreamReader(objectInputStream);
            } catch (Exception e) {
                throw new RuntimeException("Error reading input file " + opt.inputFile,e);
            }

        } else {
            File dataFile = new File(opt.inputFile);
            if (!dataFile.exists()) {
                throw new RuntimeException("Error reading input file " + opt.inputFile);
            }

            inStream = new InputStreamReader(new FileInputStream(dataFile), "UTF-8");
        }

        JsonStructureBuilder builder = new JsonStructureBuilder(opt.isArray);

        JsonPathsReader reader = new JsonPathsReader(builder);

        reader.readStream(inStream, opt.isArray);
        logger.info("total number of unique paths: " + builder.getPaths().size());
        if (opt.outSchemaFile != null) {
            JsonSchemaGenerator schemaGenerator = new JsonSchemaGenerator();
            ObjectNode schema = schemaGenerator.generateSchemaTree(builder.getRoot(), opt.rootDefinition);
            PrintWriter sWriter = new PrintWriter(new FileWriter(opt.outSchemaFile));
            sWriter.print(schemaGenerator.prettyPrintJsonString(schema));
            sWriter.close();
        }

        if (opt.outDdlFile != null) {
            PrintWriter ddlWriter = new PrintWriter(new FileWriter(opt.outDdlFile));
            RedshiftDDLGenerator redshiftDDLGenerator = new RedshiftDDLGenerator();
            redshiftDDLGenerator.writeDDL(builder.getRoot(), opt.tableName, opt.tableLocation, opt.isArray,ddlWriter);
            ddlWriter.close();
        }
        return 0;
    }
}
