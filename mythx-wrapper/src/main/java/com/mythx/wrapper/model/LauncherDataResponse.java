package com.mythx.wrapper.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LauncherDataResponse {
    private String status;
    private String message;
    private Version data;
}
