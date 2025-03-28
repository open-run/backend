package io.openur.domain.bung.entity;

import io.openur.domain.hashtag.entity.HashtagEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tb_bungs")
@NoArgsConstructor
@AllArgsConstructor
public class BungEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String bungId;

    private String name;

    private String description;

    private String location;

    @Column(name = "start_datetime")
    private LocalDateTime startDateTime;

    @Column(name = "end_datetime")
    private LocalDateTime endDateTime;

    private Float distance;

    private String pace;

    private Integer memberNumber;

    private Boolean hasAfterRun;

    private String afterRunDescription;

    @Column(name = "is_completed")
    private boolean completed;
    private String mainImage;

    @ManyToMany
    @JoinTable(
        name = "tb_bungs_hashtags",
        joinColumns = @JoinColumn(name = "bung_id"),
        inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )

    private Set<HashtagEntity> hashtags;
}
