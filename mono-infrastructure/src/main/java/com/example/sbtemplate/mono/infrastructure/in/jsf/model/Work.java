package com.example.sbtemplate.mono.infrastructure.in.jsf.model;

import java.util.List;
import lombok.Data;

@Data
public class Work {
    private String name;
    private String position;
    private String url;
    private String startDate;
    private String endDate;
    private String summary;
    private List<String> highlights;
}
