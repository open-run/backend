package io.openur.domain.hashtag.entity;

import io.openur.domain.bunghashtag.entity.BungHashtagEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tb_hashtags")
@NoArgsConstructor
@AllArgsConstructor
public class HashtagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hashtagId;

    @Column(unique = true)
    private String hashtagStr;
    
    @OneToMany(mappedBy = "hashtagEntity")
    private List<BungHashtagEntity> bungHashtags;
}
