package com.mythx.launcher.web.service;

import com.mythx.launcher.exception.WebException;
import com.mythx.launcher.web.controller.GeneralProjectController;

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
