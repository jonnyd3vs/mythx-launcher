package mythx.launcher.web.update;

public class UpdateRequest {

    private String status;
    private Update[] data;

    public UpdateRequest() {}

    public String getStatus() {
        return status;
    }

    public Update[] getData() {
        return data;
    }
}
