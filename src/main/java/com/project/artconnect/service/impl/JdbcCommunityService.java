package com.project.artconnect.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Review;
import com.project.artconnect.service.CommunityService;

/**
 * JDBC-backed implementation of CommunityService.
 * Replaces InMemoryCommunityService by fetching data from the database via DAO.
 */
public class JdbcCommunityService implements CommunityService {
    private final CommunityMemberDao communityMemberDao;

    public JdbcCommunityService(CommunityMemberDao communityMemberDao) {
        this.communityMemberDao = communityMemberDao;
    }

    @Override
    public List<CommunityMember> getAllMembers() {
        return communityMemberDao.findAll();
    }

    @Override
    public Optional<CommunityMember> getMemberByName(String name) {
        List<CommunityMember> members = communityMemberDao.findAll();
        return members.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public List<Review> getReviewsByMember(CommunityMember member) {
        // TODO: Implement when Review DAO is available
        return new ArrayList<>();
    }

    @Override
    public void addMember(CommunityMember member) {
        communityMemberDao.save(member);
    }

    @Override
    public void updateMember(CommunityMember member) {
        communityMemberDao.update(member);
    }

    @Override
    public void deleteMember(String name) {
        communityMemberDao.delete(name);
    }
}
