package io.openur.domain.bung.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Getter
@Table(name = "tb_bungs")
@NoArgsConstructor
@AllArgsConstructor
public class BungEntity  {
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
    // 종료 플래그
}
