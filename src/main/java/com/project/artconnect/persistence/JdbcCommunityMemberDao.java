package com.project.artconnect.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.util.ConnectionManager;

/**
 * JDBC implementation for CommunityMemberDao.
 */
public class JdbcCommunityMemberDao implements CommunityMemberDao {

    /**
     * Finds a community member by ID.
     */
    @Override
    public Optional<CommunityMember> findById(Long id) {
        String query = "SELECT community_id, name, email, city FROM community WHERE community_id = ?";

        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    CommunityMember member = mapResultSetToMember(rs);
                    return Optional.of(member);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding community member by id: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Fetches all community members from the database.
     */
    @Override
    public List<CommunityMember> findAll() {
        List<CommunityMember> members = new ArrayList<>();
        String query = "SELECT community_id, name, email, city FROM community";

        try (Connection conn = ConnectionManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                CommunityMember member = mapResultSetToMember(rs);
                members.add(member);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all community members: " + e.getMessage());
            e.printStackTrace();
        }

        return members;
    }

    /**
     * Helper method to map a ResultSet row to a CommunityMember object.
     */
    private CommunityMember mapResultSetToMember(ResultSet rs) throws SQLException {
        CommunityMember member = new CommunityMember();
        member.setName(rs.getString("name"));
        member.setEmail(rs.getString("email"));
        member.setCity(rs.getString("city"));
        return member;
    }

    @Override
    public void save(CommunityMember member) {
        String query = "INSERT INTO community (name, email, city) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getEmail());
            pstmt.setString(3, member.getCity());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving community member: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void update(CommunityMember member) {
        String query = "UPDATE community SET email = ?, city = ? WHERE name = ?";
        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, member.getEmail());
            pstmt.setString(2, member.getCity());
            pstmt.setString(3, member.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating community member: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String name) {
        String query = "DELETE FROM community WHERE name = ?";
        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting community member: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
