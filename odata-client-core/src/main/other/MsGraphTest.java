import microsoft.graph.generated.container.GraphService;

public class MsGraphTest {
    
    public void test() {
        new GraphService(null).users("1").contacts("1").get();
    }

}
