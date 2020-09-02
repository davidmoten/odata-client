package com.github.davidmoten.msgraph;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import com.github.davidmoten.odata.client.CollectionPage;
import com.github.davidmoten.odata.client.Serializer;

import odata.msgraph.client.complex.ObjectIdentity;
import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.DirectoryObject;
import odata.msgraph.client.entity.FileAttachment;
import odata.msgraph.client.entity.User;

public class GraphExplorerMain {

    public static void main(String[] args) {

        GraphService client = MsGraph.explorer().build();
        {
            CollectionPage<User> delta = client.users().get();
            System.out.println(Serializer.INSTANCE.serializePrettyPrint(delta.streamWithDeltaLink()));
        }
        System.exit(0);

        {
            Predicate<ObjectIdentity> hasUserPrincipalName = id -> id.getSignInType().orElse("")
                    .equals("userPrincipalName");
            // for compilation only, not running
            User u = client //
                    .users() //
                    .select("id,identities") //
                    .get() //
                    .stream() //
                    .filter(x -> x //
                            .getIdentities() //
                            .stream() //
                            .anyMatch(hasUserPrincipalName)) //
                    .findFirst() //
                    .get();

            List<ObjectIdentity> ids = u.getIdentities().toList();
            System.out.println(ids);
            ObjectIdentity id = ids.stream() //
                    .filter(hasUserPrincipalName) //
                    .findFirst() //
                    .get();
            ObjectIdentity id2 = ObjectIdentity //
                    .builder() //
                    .signInType("userPrincipalName") //
                    .issuer(id.getIssuer().orElse(null)) //
                    .issuerAssignedId(id.getIssuerAssignedId().orElse(null)) //
                    .build();
            ids.add(id2);
            // no real change but see if patch works
            u.withIdentities(ids).patch();
        }

        System.exit(0);
        {
            client //
                    .me() //
                    .messages() //
                    .stream() //
                    .peek(x -> System.out.println(x.getId().orElse("?"))) //
                    .flatMap(m -> m.getAttachments().metadataFull().stream()) //
                    .filter(x -> x instanceof FileAttachment) //
                    .map(x -> (FileAttachment) x) //
                    .limit(1) //
                    .peek(x -> System.out.println(x.getName().orElse("?"))) //
                    .map(x -> x.getStream().get().getBytes().length) //
                    .forEach(System.out::println);
        }

        System.exit(0);

        DirectoryObject u = client //
                .directoryObjects() //
                .getByIds( //
                        Arrays.asList("6e7b768e-07e2-4810-8459-485f84f8f204"), //
                        Arrays.asList("user")) //
                .select("id,mail") //
                .connectTimeout(1, TimeUnit.MILLISECONDS) //
                .stream() //
                .findFirst() //
                .get();

        System.out.println(u);

        System.exit(0);

        client //
                .me() //
                .messages() //
                .select("id") //
                .stream() //
                .flatMap(m -> m.getAttachments().select("name, size").stream()) //
                .limit(5) //
                .map(a -> a.getName().orElse("?") + " " + a.getSize().orElse(-1) + "B") //
                .forEach(System.out::println);

        System.exit(0);

        client //
                .users() //
                .select("displayName") //
                .maxPageSize(10).stream() //
                .limit(10) //
                .map(user -> user.getDisplayName().orElse("?")) //
                .forEach(System.out::println);

        System.exit(0);

        String id = client.me().messages().select("id").stream().limit(1).findFirst().get().getId().get();

        client //
                .me() //
                .messages(id) //
                .attachments() //
                .maxPageSize(2) //
                .select("name,size") //
                .stream() //
                .filter(att -> att instanceof FileAttachment) //
                .limit(2) //
                .map(att -> att.getName().orElse("") + " " + att.getSize().orElse(0) + "B") //
                .forEach(System.out::println);

        System.exit(0);

        client //
                .groups() //
                .top(3) //
                .select("id,displayName,groupTypes") //
                .stream() //
                .limit(3) //
                .filter(g -> g.getGroupTypes().toList().contains("Unified"))
                .peek(g -> System.out.println(g.getDisplayName().orElse("?"))) //
                .flatMap(group -> group //
                        .getMembers() //
                        .select("id,displayName,userType") //
                        .filter(User.class) //
                        .stream()) //
                .filter(user -> "Member".equalsIgnoreCase(user.getUserType().orElse("")))
                .forEach(user -> System.out.println("  " + user.getDisplayName().orElse("?")));
    }

}
