package at.alladin.rmbt.shared.json;

/**
 * @author tomas.hreben
 */
public class TestServerJson implements TestServerJsonInterface {

    private int id;
    private String name;
    private int port;
    private String address;

    public TestServerJson() {   }

    public TestServerJson(int id, String name, int port, String address) {
        this.id = id;
        this.name = name;
        this.port = port;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "TestServerJson [id=" + id + ", name=" + name + ", port=" + port + ", address=" + address
                + "]";
    }

}
