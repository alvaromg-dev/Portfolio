package com.alvaromg.portfolio.infrastructure.in.jsf.model;

import java.util.List;
import lombok.Data;

@Data
public class Basics {
    private String name;
    private String label;
    private String image;
    private String email;
    private String summary;
    private String status;
    private List<Profile> profiles;
}
