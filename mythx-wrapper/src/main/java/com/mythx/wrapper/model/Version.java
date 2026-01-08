package com.mythx.wrapper.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Version {
    private String version;
    private String size;
    private String filename;
    private String url;
}
