package com.github.davidmoten.odata.client;

import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.oasisopen.odata.csdl.v4.TDataServices;
import org.oasisopen.odata.csdl.v4.TEdmx;

import microsoft.graph.generated.entity.ActivityHistoryItem;

public class GenerateTest {

    @Test
    public void testReadMsgraphCsdl() throws JAXBException {
        JAXBContext c = JAXBContext.newInstance(TDataServices.class);
        Unmarshaller unmarshaller = c.createUnmarshaller();
        TEdmx t = unmarshaller
                .unmarshal(new StreamSource(GenerateTest.class.getResourceAsStream("/msgraph-1.0-20180905.xml")),
                        TEdmx.class)
                .getValue();
        Generator g = new Generator(new Options(), t.getDataServices().getSchema().get(0));
        g.generate();
    }
    
    @Test
    public void generatedObjectCanMethodChain() {
        new ActivityHistoryItem().setActiveDurationSeconds(Optional.empty()).setCreatedDateTime(Optional.empty());
    }
}
