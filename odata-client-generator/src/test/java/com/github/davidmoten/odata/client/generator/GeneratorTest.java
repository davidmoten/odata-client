package com.github.davidmoten.odata.client.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Ignore;
import org.junit.Test;
import org.oasisopen.odata.csdl.v4.TDataServices;
import org.oasisopen.odata.csdl.v4.TEdmx;

public class GeneratorTest {

    private static final String GENERATED = "target/generated-sources/odata";

    @Test
    public void testGenerateMsgraph() throws JAXBException, IOException {
        JAXBContext c = JAXBContext.newInstance(TDataServices.class);
        Unmarshaller unmarshaller = c.createUnmarshaller();
        TEdmx t = unmarshaller
                .unmarshal(new StreamSource(new FileInputStream("src/main/odata/msgraph-metadata.xml")), TEdmx.class)
                .getValue();
        SchemaOptions schemaOptions = new SchemaOptions("microsoft.graph", "microsoft.graph.generated");
        Options options = new Options(GENERATED, Collections.singletonList(schemaOptions));
        Generator g = new Generator(options, Collections.singletonList(t.getDataServices().getSchema().get(0)));
        g.generate();
        File file = new File(GENERATED + "/microsoft/graph/generated/entity/FileAttachment.java");
        Files.copy(file.toPath(), new File("../src/docs/FileAttachment.java").toPath(),
                StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    @Ignore
    public void testGenerateMsgraphBeta() throws JAXBException, IOException {
        JAXBContext c = JAXBContext.newInstance(TDataServices.class);
        Unmarshaller unmarshaller = c.createUnmarshaller();
        TEdmx t = unmarshaller
                .unmarshal(new StreamSource(new FileInputStream("../odata-client-msgraph-beta/src/main/odata/msgraph-beta-metadata.xml")), TEdmx.class)
                .getValue();
        
        SchemaOptions schemaOptions = new SchemaOptions("microsoft.graph", "microsoft.graph.betas.generated");
        Options options = new Options(GENERATED, Collections.singletonList(schemaOptions));
        Generator g = new Generator(options, t.getDataServices().getSchema());
        g.generate();
    }

    @Test
    public void testGenerateODataTestService() throws JAXBException, FileNotFoundException {
        JAXBContext c = JAXBContext.newInstance(TDataServices.class);
        Unmarshaller unmarshaller = c.createUnmarshaller();
        TEdmx t = unmarshaller
                .unmarshal(new StreamSource(new FileInputStream("src/main/odata/odata-test-service-metadata.xml")),
                        TEdmx.class)
                .getValue();
        SchemaOptions schemaOptions = new SchemaOptions("ODataDemo", "odata.test.generated");
        Options options = new Options(GENERATED, Collections.singletonList(schemaOptions));
        Generator g = new Generator(options, Collections.singletonList(t.getDataServices().getSchema().get(0)));
        g.generate();
    }

    public static void main(String[] args) throws JAXBException, IOException {
        while (true) {
            new GeneratorTest().testGenerateMsgraph();
        }
    }
}
