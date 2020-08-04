package com.github.davidmoten.msgraph;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.github.davidmoten.odata.client.StreamProvider;

import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.DirectoryObject;
import odata.msgraph.client.entity.FileAttachment;
import odata.msgraph.client.entity.User;

public class GraphExplorerMain {

    public static void main(String[] args) {

        GraphService client = MsGraph.explorer().build();
        {
//            client //
//                    .me() //
//                    .messages() //
//                    .stream() //
//                    .peek(x -> System.out.println(x.getId().orElse("?"))) //
//                    .flatMap(m -> m.getAttachments().metadataFull().stream()) //
//                    .filter(x -> x instanceof FileAttachment) //
//                    .map(x -> (FileAttachment) x) //
//                    .limit(1) //
//                    .peek(x -> System.out.println(x.getName().orElse("?"))) //
//                    .map(x -> x.getStream().get().getBytes().length) //
//                    .forEach(System.out::println);
            String id = "AAMkAGVmMDEzMTM4LTZmYWUtNDdkNC1hMDZiLTU1OGY5OTZhYmY4OABGAAAAAAAiQ8W967B7TKBjgx9rVEURBwAiIsqMbYjsT5e-T7KzowPTAAAAAAEMAAAiIsqMbYjsT5e-T7KzowPTAALGDZ0eAAA=";
//            client.me().messages(id).attachments().metadataFull().stream().filter(x -> x instanceof FileAttachment);
            
            StreamProvider att = client.me().messages(id).get().getStream().get();
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
