package com.example.sbtemplate.mono.infrastructure.in.jsf.model;

import java.util.List;
import lombok.Data;

@Data
public class Education {
    private String institution;
    private String area;
    private String url;
    private String startDate;
    private String endDate;
    private List<String> courses;
}
