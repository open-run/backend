package io.openur.domain.bunghashtag.entity;

import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.hashtag.entity.HashtagEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tb_bungs_hashtags")
@NoArgsConstructor
@AllArgsConstructor
public class BungHashtagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bungHashtagId;

    @ManyToOne(targetEntity = BungEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "bung_id")
    private BungEntity bungEntity;

    @ManyToOne(targetEntity = HashtagEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id")
    private HashtagEntity hashtagEntity;
}

