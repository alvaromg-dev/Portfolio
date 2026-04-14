package com.example.sbtemplate.mono.infrastructure.in.jsf.model;

import java.util.List;
import lombok.Data;

@Data
public class Project {
    private String name;
    private String description;
    private String url;
    private List<String> highlights;
}
