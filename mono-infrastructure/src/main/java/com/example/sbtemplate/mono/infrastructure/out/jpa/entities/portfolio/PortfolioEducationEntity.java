package com.example.sbtemplate.mono.infrastructure.out.jpa.entities.portfolio;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "portfolio_education_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioEducationEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    private LanguageEntity language;

    @Column(name = "institution", nullable = false, length = 150)
    private String institution;

    @Column(name = "area", nullable = false, length = 200)
    private String area;

    @Column(name = "url", length = 512)
    private String url;

    @Column(name = "start_date", nullable = false, length = 20)
    private String startDate;

    @Column(name = "end_date", length = 20)
    private String endDate;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "portfolio_education_courses", joinColumns = @JoinColumn(name = "education_id"))
    @Column(name = "course", nullable = false, length = 150)
    private List<String> courses = new ArrayList<>();

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @PrePersist
    public void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }
}
