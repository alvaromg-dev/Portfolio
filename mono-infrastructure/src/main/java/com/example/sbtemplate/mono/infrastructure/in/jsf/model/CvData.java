package com.example.sbtemplate.mono.infrastructure.in.jsf.model;

import java.util.List;
import lombok.Data;

@Data
public class CvData {
    private Basics basics;
    private List<Work> work;
    private List<Education> education;
    private List<Project> projects;
    private List<Skill> skills;
}
