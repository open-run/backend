package io.openur.domain.bung.entity;

import io.openur.domain.bunghashtag.entity.BungHashtagEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter @Builder
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

    private Double latitude;

    private Double longitude;

    @Column(name = "start_datetime")
    private LocalDateTime startDateTime;

    @Column(name = "end_datetime")
    private LocalDateTime endDateTime;

    private Float distance;

    private String pace;

    private Integer memberNumber;

    private Integer  currentMemberNumber;

    private Boolean hasAfterRun;

    private String afterRunDescription;

    @Column(name = "is_completed")
    private boolean completed;

    @Column(name = "is_faded")
    private boolean faded;

    private String mainImage;

    @OneToMany(mappedBy = "bungEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BungHashtagEntity> bungHashtags;
}
