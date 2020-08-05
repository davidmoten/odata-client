package com.github.davidmoten.msgraph.beta;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;

import com.github.davidmoten.msgraph.builder.MsGraphClientBuilder;
import com.github.davidmoten.odata.client.PathStyle;
import com.github.davidmoten.odata.client.TestingService.ContainerBuilder;

import odata.msgraph.client.beta.complex.ObjectIdentity;
import odata.msgraph.client.beta.container.GraphService;
import odata.msgraph.client.beta.entity.User;

public class BetaServiceTest {
    
    @Test
    @Ignore
    public void test() {
        GraphService client = clientBuilder().build();
        User user = client //
                .users("fred") //
                // for efficiency only download the fields we need
                .select("id,identities") //
                .get();
        List<ObjectIdentity> ids = user //
                .getIdentities() //
                .stream() //
                .filter(x -> !"emailAddress".equals(x.getSignInType().orElse(""))) //
                .collect(Collectors.toList());
        ObjectIdentity id = ObjectIdentity //
                .builder() //
                .issuer("contoso.onmicrosoft.com") //
                .signInType("emailAddress") //
                .issuerAssignedId("fred@contoso.com") //
                .build();
        ids.add(id);
        user.withIdentities(ids).patch();
    }
    
    private static ContainerBuilder<GraphService> clientBuilder() {
        return GraphService //
                .test() //
                .baseUrl("https://graph.microsoft.com/beta") //
                .pathStyle(PathStyle.IDENTIFIERS_AS_SEGMENTS) //
                .addProperties(MsGraphClientBuilder.createProperties());
    }

}
