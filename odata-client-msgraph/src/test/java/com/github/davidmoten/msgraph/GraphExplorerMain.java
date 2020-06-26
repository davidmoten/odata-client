package com.github.davidmoten.msgraph;

import com.github.davidmoten.odata.client.CollectionPage;
import com.github.davidmoten.odata.client.Serializer;

import odata.msgraph.client.complex.InternetMessageHeader;
import odata.msgraph.client.container.GraphService;
import odata.msgraph.client.entity.FileAttachment;
import odata.msgraph.client.entity.Message;
import odata.msgraph.client.entity.User;

public class GraphExplorerMain {

    public static void main(String[] args) {

        GraphService client = MsGraph.explorer().build();
        Message m = client.me().messages().stream().findFirst().get();
        System.out.println(m.getSubject());
        CollectionPage<InternetMessageHeader> c = m.getInternetMessageHeaders();
        System.out.println(c.currentPage().size());
        System.out.println(c.nextPage().isPresent());
        System.out.println(Serializer.INSTANCE.serialize(c));
        m.getInternetMessageHeaders().forEach(System.out::println);
        
        System.exit(0);

        client //
                .users() //
                .select("displayName") //
                .maxPageSize(10).stream() //
                .limit(10) //
                .map(user -> user.getDisplayName().orElse("?")) //
                .forEach(System.out::println);

        System.exit(0);

        String id = client.me().messages().select("id").stream().limit(1).findFirst().get().getId()
                .get();

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
