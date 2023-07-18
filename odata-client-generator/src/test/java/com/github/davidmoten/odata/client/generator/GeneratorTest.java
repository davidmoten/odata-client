package com.github.davidmoten.odata.client.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.oasisopen.odata.csdl.v4.TDataServices;
import org.oasisopen.odata.csdl.v4.TEdmx;

import com.github.davidmoten.guavamini.Lists;

public class GeneratorTest {

    private static final String GENERATED = "target/generated-sources/odata";

    @Test
    public void testGenerateMsgraph() throws JAXBException, IOException {
        JAXBContext c = JAXBContext.newInstance(TDataServices.class);
        Unmarshaller unmarshaller = c.createUnmarshaller();
        TEdmx t = unmarshaller.unmarshal(
                new StreamSource(new FileInputStream("src/main/odata/msgraph-metadata.xml")),
                TEdmx.class).getValue();
        SchemaOptions o1 = new SchemaOptions("microsoft.graph",
                "microsoft.graph.generated");
        SchemaOptions o2 = new SchemaOptions("microsoft.graph.callRecords",
                "microsoft.graph.callrecords.generated");
        SchemaOptions o3 = new SchemaOptions("microsoft.graph.externalConnectors", 
                "microsoft.graph.externalconnectors.generated");
        SchemaOptions o4 = new SchemaOptions("microsoft.graph.ediscovery", 
                "microsoft.graph.ediscovery.generated");
        SchemaOptions o5 = new SchemaOptions("microsoft.graph.termStore", 
                "microsoft.graph.termstore.generated");
        SchemaOptions o6 = new SchemaOptions("microsoft.graph.security", 
                "microsoft.graph.security");
        SchemaOptions o7 = new SchemaOptions("microsoft.graph.industryData", 
                        "microsoft.graph.industrydata");
        SchemaOptions o8 = new SchemaOptions("microsoft.graph.identityGovernance", 
                "microsoft.graph.identitygovernance");
        Options options = new Options(GENERATED, Lists.newArrayList(o1, o2, o3, o4, o5, o6, o7, o8));
        Generator g = new Generator(options,
                t.getDataServices().getSchema());
        g.generate();
        File file = new File(GENERATED + "/microsoft/graph/generated/entity/FileAttachment.java");
        Files.copy(file.toPath(), new File("../src/docs/FileAttachment.java").toPath(),
                StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void testGenerateMsgraphBeta() throws JAXBException, IOException {
        JAXBContext c = JAXBContext.newInstance(TDataServices.class);
        Unmarshaller unmarshaller = c.createUnmarshaller();
        TEdmx t = unmarshaller.unmarshal(
                new StreamSource(new FileInputStream(
                        "../odata-client-msgraph-beta/src/main/odata/msgraph-beta-metadata.xml")),
                TEdmx.class).getValue();
        t.getDataServices().getSchema().forEach(s -> System.out.println(s.getNamespace()));
        SchemaOptions o1 = new SchemaOptions("microsoft.graph",
                "microsoft.graph.beta.generated");
        SchemaOptions o2 = new SchemaOptions("microsoft.graph.callRecords",
                "microsoft.graph.beta.callRecords.generated");
        SchemaOptions o3 = new SchemaOptions("microsoft.graph.termStore",
                "microsoft.graph.beta.termStore.generated");
        SchemaOptions o4 = new SchemaOptions("microsoft.graph.ediscovery",
                "microsoft.graph.beta.ediscovery.generated");
        SchemaOptions o5 = new SchemaOptions("microsoft.graph.externalConnectors",
                "microsoft.graph.beta.external.connectors");
        SchemaOptions o6 = new SchemaOptions("microsoft.graph.windowsUpdates",
                "microsoft.graph.beta.windows.updates");
        // microsoft.graph.managedTenants
        SchemaOptions o7 = new SchemaOptions("microsoft.graph.managedTenants",
                "microsoft.graph.beta.managed.tenants");
        SchemaOptions o8 = new SchemaOptions("microsoft.graph.search",
                "microsoft.graph.beta.search");
        SchemaOptions o9 = new SchemaOptions("microsoft.graph.security",
                "microsoft.graph.beta.security");
        SchemaOptions o10 = new SchemaOptions("microsoft.graph.identityGovernance",
                "microsoft.graph.beta.identity.governance");
        SchemaOptions o11 = new SchemaOptions("microsoft.graph.deviceManagement",
                "microsoft.graph.beta.device.management");
        SchemaOptions o12 = new SchemaOptions("microsoft.graph.tenantAdmin",
                "microsoft.graph.beta.tenant.admin");
        Options options = new Options(GENERATED, Arrays.asList(o1, o2,
                o3, o4, o5, o6, o7, o8, o9, o10, o11, o12));
        Generator g = new Generator(options, t.getDataServices().getSchema());
        g.generate();
    }

    @Test
    public void testGenerateODataTestService() throws JAXBException, FileNotFoundException {
        JAXBContext c = JAXBContext.newInstance(TDataServices.class);
        Unmarshaller unmarshaller = c.createUnmarshaller();
        TEdmx t = unmarshaller.unmarshal(
                new StreamSource(
                        new FileInputStream("src/main/odata/odata-test-service-metadata.xml")),
                TEdmx.class).getValue();
        SchemaOptions o = new SchemaOptions("ODataDemo", "odata.test.generated");
        Options options = new Options(GENERATED, Collections.singletonList(o));
        Generator g = new Generator(options,
                Collections.singletonList(t.getDataServices().getSchema().get(0)));
        g.generate();
    }

    public static void main(String[] args) throws JAXBException, IOException {
        //noinspection InfiniteLoopStatement
        while (true) {
            new GeneratorTest().testGenerateMsgraph();
        }
    }
}
