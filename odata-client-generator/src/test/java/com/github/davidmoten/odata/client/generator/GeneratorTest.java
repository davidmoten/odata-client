package com.github.davidmoten.odata.client.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.oasisopen.odata.csdl.v4.TDataServices;
import org.oasisopen.odata.csdl.v4.TEdmx;

import com.github.davidmoten.guavamini.Lists;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

public class GeneratorTest {

    private static final String GENERATED = "target/generated-sources/odata";

    @Test
    public void testGenerateMsgraph() throws JAXBException, IOException {
        JAXBContext c = JAXBContext.newInstance(TDataServices.class);
        Unmarshaller unmarshaller = c.createUnmarshaller();
        TEdmx t = unmarshaller
                .unmarshal(new StreamSource(new FileInputStream("src/main/odata/msgraph-metadata.xml")), TEdmx.class)
                .getValue();

        Map<String, String> schemas = new HashMap<>();
        schemas.put("microsoft.graph", "microsoft.graph.generated");
        schemas.put("microsoft.graph.callRecords", "microsoft.graph.callrecords.generated");
        schemas.put("microsoft.graph.externalConnectors", "microsoft.graph.externalconnectors.generated");
        schemas.put("microsoft.graph.ediscovery", "microsoft.graph.ediscovery.generated");
        schemas.put("microsoft.graph.termStore", "microsoft.graph.termstore.generated");
        schemas.put("microsoft.graph.security", "microsoft.graph.security");
        schemas.put("microsoft.graph.industryData", "microsoft.graph.industrydata");
        schemas.put("microsoft.graph.identityGovernance", "microsoft.graph.identitygovernance");
        schemas.put("microsoft.graph.search", "microsoft.graph.searchs");
        schemas.put("microsoft.graph.partners.billing", "microsoft.graph.partnersbilling");
        List<SchemaOptions> list = schemas.entrySet().stream() //
                .map(entry -> new SchemaOptions(entry.getKey(), entry.getValue())) //
                .collect(Collectors.toList());
        Options options = new Options(GENERATED, list);

        Generator g = new Generator(options, t.getDataServices().getSchema());
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
                new StreamSource(
                        new FileInputStream("../odata-client-msgraph-beta/src/main/odata/msgraph-beta-metadata.xml")),
                TEdmx.class).getValue();
        t.getDataServices().getSchema().forEach(s -> System.out.println(s.getNamespace()));
        Map<String, String> schemas = new HashMap<>();
        schemas.put("microsoft.graph", "microsoft.graph.beta.generated");
        schemas.put("microsoft.graph.callRecords", "microsoft.graph.beta.callRecords.generated");
        schemas.put("microsoft.graph.termStore", "microsoft.graph.beta.termStore.generated");
        schemas.put("microsoft.graph.ediscovery", "microsoft.graph.beta.ediscovery.generated");
        schemas.put("microsoft.graph.externalConnectors", "microsoft.graph.beta.external.connectors");
        schemas.put("microsoft.graph.windowsUpdates", "microsoft.graph.beta.windows.updates");
        schemas.put("microsoft.graph.managedTenants", "microsoft.graph.beta.managed.tenants");
        schemas.put("microsoft.graph.search", "microsoft.graph.beta.search");
        schemas.put("microsoft.graph.security", "microsoft.graph.beta.security");
        schemas.put("microsoft.graph.identityGovernance", "microsoft.graph.beta.identity.governance");
        schemas.put("microsoft.graph.deviceManagement", "microsoft.graph.beta.device.management");
        schemas.put("microsoft.graph.tenantAdmin", "microsoft.graph.beta.tenant.admin");
        schemas.put("microsoft.graph.healthMonitoring", "microsoft.graph.beta.health.monitoring");
        schemas.put("microsoft.graph.networkaccess", "microsoft.graph.beta.networkaccess");
        schemas.put("microsoft.graph.cloudLicensing", "microsoft.graph.beta.cloud.licencing");
        schemas.put("microsoft.graph.teamsAdministration", "microsoft.graph.beta.teams.administration");
        schemas.put("microsoft.graph.industryData", "microsoft.graph.beta.industry.data");
        schemas.put("microsoft.graph.partners.billing", "microsoft.graph.beta.partners.billing");
        schemas.put("microsoft.graph.partners.security", "microsoft.graph.beta.partners.security");
        schemas.put("microsoft.graph.partner.security", "microsoft.graph.beta.partner.security");
        List<SchemaOptions> list = schemas.entrySet().stream() //
                .map(entry -> new SchemaOptions(entry.getKey(), entry.getValue())) //
                .collect(Collectors.toList());
        Options options = new Options(GENERATED, list);
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
