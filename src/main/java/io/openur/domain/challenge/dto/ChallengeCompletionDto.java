package io.openur.domain.challenge.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class ChallengeCompletionDto {
    private List<String> userIds;
    private String location;
    private LocalDateTime completionDate;

    // TODO: 어떻게 넘어갈지를 모르겠네...?
    private String outfitInfos;

    @Getter
    public static class ofLocation {
        private List<String> userIds;
        private String location;

        public ofLocation(ChallengeCompletionDto dto) {
            this.userIds = dto.getUserIds();
            this.location = dto.getLocation();
        }
    }

    @Getter
    public static class ofDate {
        private List<String> userIds;
        private LocalDateTime completionDate;

        public ofDate(ChallengeCompletionDto dto) {
            this.userIds = dto.getUserIds();
            this.completionDate = dto.getCompletionDate();
        }
    }

    @Getter
    public static class ofCount {
        private List<String> userIds;

        public ofCount(ChallengeCompletionDto dto) {
            this.userIds = dto.getUserIds();
        }
    }

    @Getter
    public static class ofOutfit {
        private List<String> userIds;
        private String outfitInfos;

        public ofOutfit(ChallengeCompletionDto dto) {
            this.userIds = dto.getUserIds();
            this.outfitInfos = dto.getOutfitInfos();
        }
    }
}
