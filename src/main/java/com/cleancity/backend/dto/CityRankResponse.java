package com.cleancity.backend.dto;

import java.util.List;

public class CityRankResponse {
    private List<UserRankDto> topUsers;
    private UserRankDto currentUser;

    public CityRankResponse(List<UserRankDto> topUsers, UserRankDto currentUser) {
        this.topUsers = topUsers;
        this.currentUser = currentUser;
    }

    public List<UserRankDto> getTopUsers() {
        return topUsers;
    }

    public void setTopUsers(List<UserRankDto> topUsers) {
        this.topUsers = topUsers;
    }

    public UserRankDto getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(UserRankDto currentUser) {
        this.currentUser = currentUser;
    }

    public static class UserRankDto {
        private String name;
        private Integer rewardPoints;
        private Long rank;

        public UserRankDto(String name, Integer rewardPoints, Long rank) {
            this.name = name;
            this.rewardPoints = rewardPoints;
            this.rank = rank;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getRewardPoints() {
            return rewardPoints;
        }

        public void setRewardPoints(Integer rewardPoints) {
            this.rewardPoints = rewardPoints;
        }

        public Long getRank() {
            return rank;
        }

        public void setRank(Long rank) {
            this.rank = rank;
        }
    }
}
