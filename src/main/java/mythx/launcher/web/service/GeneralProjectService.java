package mythx.launcher.web.service;

import mythx.launcher.exception.WebException;
import mythx.launcher.web.controller.GeneralProjectController;

public class GeneralProjectService {
    private final GeneralProjectController controller;

    public GeneralProjectService(GeneralProjectController controller) {
        this.controller = controller;
    }

    public String getDownLoadUrl() {
        try {
           return controller.handleGeneralLinks().getData().getClientUrl();
        } catch (WebException e) {
            throw new RuntimeException(e);
        }
    }

}
