import aws.json.schema.induction.JsonPathsReader;
import aws.json.schema.induction.JsonSchemaGenerator;
import aws.json.schema.induction.JsonStructureBuilder;
import aws.json.schema.induction.RedshiftDDLGenerator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TestSchemaInduction {
    @BeforeEach
    void setUp() {
        try {
            Files.createDirectories(Paths.get("target/tmp"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void readClaims() throws IOException {
        File dataFile = new File("../data/fhir/claims1.json");
//        File dataFile = new File("data/patient.json");
        boolean isArray = true;
        InputStreamReader in = new InputStreamReader(new FileInputStream(dataFile), "UTF-8");
        JsonStructureBuilder builder = new JsonStructureBuilder(isArray);
        JsonPathsReader reader = new JsonPathsReader(List.of(builder));

        reader.readStream(in, isArray);

        PrintWriter pathsWriter = new PrintWriter(new FileWriter("target/tmp/claims_paths.csv"));
        builder.writePaths(pathsWriter);
        pathsWriter.close();

        PrintWriter treeWriter = new PrintWriter(new FileWriter("target/tmp/claims_tree.txt"));
        builder.writeTree(treeWriter);
        treeWriter.close();

        RedshiftDDLGenerator redshiftDDLGenerator = new RedshiftDDLGenerator();

        String ddl = redshiftDDLGenerator.generate(builder.getRoot(),"FHIR_TEST_DB.Claims", "s3://bucket/FHIR/fhirbase-demo/claims-1", true);
        System.out.println(ddl);

        JsonSchemaGenerator schemaGenerator = new JsonSchemaGenerator();
        ObjectNode claimSchema = schemaGenerator.generateSchemaTree(builder.getRoot(), "Claim");
        PrintWriter sWriter = new PrintWriter(new FileWriter("target/tmp/schema.json"));
        sWriter.print(schemaGenerator.prettyPrintJsonString(claimSchema));
        sWriter.close();
    }

    @Test
    public void readPatient() throws IOException {
        File dataFile = new File("../data/fhir/patient.json");
        boolean isArray = false;
        InputStreamReader in = new InputStreamReader(new FileInputStream(dataFile), "UTF-8");
        JsonStructureBuilder builder = new JsonStructureBuilder(isArray);
        JsonPathsReader reader = new JsonPathsReader(List.of(builder));

        reader.readStream(in, isArray);

        PrintWriter pathsWriter = new PrintWriter(new FileWriter("target/tmp/patient_paths.csv"));
        builder.writePaths(pathsWriter);
        pathsWriter.close();

        PrintWriter treeWriter = new PrintWriter(new FileWriter("target/tmp/patient_tree.txt"));
        builder.writeTree(treeWriter);
        treeWriter.close();

        RedshiftDDLGenerator redshiftDDLGenerator = new RedshiftDDLGenerator();

        String ddl = redshiftDDLGenerator.generate(builder.getRoot(),"Patient", "s3://mybucket/foo", false);
    }
}
