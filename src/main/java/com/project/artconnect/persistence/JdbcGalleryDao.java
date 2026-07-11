package com.project.artconnect.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

/**
 * JDBC implementation for GalleryDao.
 */
public class JdbcGalleryDao implements GalleryDao {

    /**
     * Finds a gallery by ID.
     */
    @Override
    public Optional<Gallery> findById(Long id) {
        String query = "SELECT gallery_id, nom, localisation, note FROM galleries WHERE gallery_id = ?";

        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Gallery gallery = mapResultSetToGallery(rs);
                    return Optional.of(gallery);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding gallery by id: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Fetches all galleries from the database.
     */
    @Override
    public List<Gallery> findAll() {
        List<Gallery> galleries = new ArrayList<>();
        String query = "SELECT gallery_id, nom, localisation, note FROM galleries";

        try (Connection conn = ConnectionManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Gallery gallery = mapResultSetToGallery(rs);
                galleries.add(gallery);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all galleries: " + e.getMessage());
            e.printStackTrace();
        }

        return galleries;
    }

    /**
     * Helper method to map a ResultSet row to a Gallery object.
     */
    private Gallery mapResultSetToGallery(ResultSet rs) throws SQLException {
        Gallery gallery = new Gallery();
        gallery.setName(rs.getString("nom"));
        gallery.setAddress(rs.getString("localisation"));
        gallery.setRating(rs.getDouble("note"));
        return gallery;
    }

    @Override
    public void save(Gallery gallery) {
        String query = "INSERT INTO galleries (nom, localisation, note) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, gallery.getName());
            pstmt.setString(2, gallery.getAddress());
            pstmt.setDouble(3, gallery.getRating());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving gallery: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void update(Gallery gallery) {
        String query = "UPDATE galleries SET localisation = ?, note = ? WHERE nom = ?";
        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, gallery.getAddress());
            pstmt.setDouble(2, gallery.getRating());
            pstmt.setString(3, gallery.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating gallery: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String name) {
        String query = "DELETE FROM galleries WHERE nom = ?";
        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting gallery: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
