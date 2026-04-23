package com.cleancity.backend.service;

import com.cleancity.backend.dto.CityRankResponse;
import com.cleancity.backend.entity.User;
import com.cleancity.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public CityRankResponse getCityRank(UUID currentUserId) {
        // Get top 5 users
        List<User> topUsersEntities = userRepository.findTop5ByOrderByRewardPointsDesc();
        
        List<CityRankResponse.UserRankDto> topUsers = topUsersEntities.stream()
                .map(u -> new CityRankResponse.UserRankDto(
                        u.getName(),
                        u.getRewardPoints(),
                        userRepository.getRankByRewardPoints(u.getRewardPoints())
                ))
                .collect(Collectors.toList());

        // Get current user details
        User currentUserEntity = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long currentUserRank = userRepository.getRankByRewardPoints(currentUserEntity.getRewardPoints());
        
        CityRankResponse.UserRankDto currentUser = new CityRankResponse.UserRankDto(
                currentUserEntity.getName(),
                currentUserEntity.getRewardPoints(),
                currentUserRank
        );

        return new CityRankResponse(topUsers, currentUser);
    }
}
