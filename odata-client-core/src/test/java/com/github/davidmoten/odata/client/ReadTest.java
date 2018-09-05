package com.github.davidmoten.odata.client;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.oasisopen.odata.csdl.v4.TDataServices;
import org.oasisopen.odata.csdl.v4.TEdmx;

public class ReadTest {

    @Test
    public void testReadMsgraphCsdl() throws JAXBException {
        JAXBContext c = JAXBContext.newInstance(TDataServices.class);
        Unmarshaller unmarshaller = c.createUnmarshaller();
        TEdmx t =  unmarshaller
                .unmarshal(new StreamSource(ReadTest.class.getResourceAsStream("/msgraph-1.0-20180905.xml")), TEdmx.class).getValue();
        t.getDataServices().getSchema().stream().forEach(s -> s.getComplexTypeOrEntityTypeOrTypeDefinition().stream().map(x -> x.getClass().getName()).distinct().forEach(System.out::println));
    }

}
