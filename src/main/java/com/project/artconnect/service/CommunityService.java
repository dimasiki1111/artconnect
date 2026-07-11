package com.project.artconnect.service;

import java.util.List;
import java.util.Optional;

import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Review;

public interface CommunityService {
    List<CommunityMember> getAllMembers();

    Optional<CommunityMember> getMemberByName(String name);

    List<Review> getReviewsByMember(CommunityMember member);

    void addMember(CommunityMember member);

    void updateMember(CommunityMember member);

    void deleteMember(String name);
}
