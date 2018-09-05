package com.github.davidmoten.odata.client;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.oasisopen.odata.csdl.v4.TAction;
import org.oasisopen.odata.csdl.v4.TAnnotations;
import org.oasisopen.odata.csdl.v4.TComplexType;
import org.oasisopen.odata.csdl.v4.TDataServices;
import org.oasisopen.odata.csdl.v4.TEdmx;
import org.oasisopen.odata.csdl.v4.TEntityContainer;
import org.oasisopen.odata.csdl.v4.TEntityType;
import org.oasisopen.odata.csdl.v4.TEnumType;
import org.oasisopen.odata.csdl.v4.TFunction;
import org.oasisopen.odata.csdl.v4.TTerm;

public class ReadTest {

    @Test
    public void testReadMsgraphCsdl() throws JAXBException {
        JAXBContext c = JAXBContext.newInstance(TDataServices.class);
        Unmarshaller unmarshaller = c.createUnmarshaller();
        TEdmx t = unmarshaller
                .unmarshal(new StreamSource(ReadTest.class.getResourceAsStream("/msgraph-1.0-20180905.xml")),
                        TEdmx.class)
                .getValue();
        t.getDataServices().getSchema().stream().forEach(s -> s.getComplexTypeOrEntityTypeOrTypeDefinition().stream()
                .peek(ReadTest::handle)
                .map(x -> x.getClass().getName()) //
                .distinct() //
                .count());
    }

    private static void handle(Object o) {
        log(o);
        if (o instanceof TEnumType) {
            handle((TEnumType) o);
        } else if (o instanceof TEntityType) {
            handle((TEntityType) o);
        } else if (o instanceof TComplexType) {
            handle((TComplexType) o);
        } else if (o instanceof TAction) {
            handle((TAction) o);
        } else if (o instanceof TFunction) {
            handle((TFunction) o);
        } else if (o instanceof TTerm) {
            handle((TTerm) o);
        } else if (o instanceof TEntityContainer) {
            handle((TEntityContainer) o);
        } else if (o instanceof TAnnotations) {
            handle((TAnnotations) o);
        } else {
            throw new RuntimeException("unexpected");
        }
    }
    
    private static void log(Object o) {
        System.out.println(o.getClass().getName() + "=" + o);
    }
    
    private static void handle(TEnumType t) {
        System.out.println(t.getName()  + "="+ t.getMemberOrAnnotation());
    }
    
    private static void handle(TEntityType t) {
    }
    
    private static void handle(TComplexType t) {
    } 
    private static void handle(TAction t) {
    } 
    private static void handle(TFunction t) {
    } 
    private static void handle(TTerm t) {
    } 
    private static void handle(TEntityContainer t) {
    } 
    private static void handle(TAnnotations t) {
    } 
}
