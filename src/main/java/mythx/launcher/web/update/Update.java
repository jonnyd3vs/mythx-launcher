package mythx.launcher.web.update;

public class Update {

    private int id;
    private String title;
    private String status;
    private String description;
    private String date;

    public Update(int id, String title, String status, String description, String date) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.description = description;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }
}
