package com.project.artconnect.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

/**
 * JDBC implementation for ExhibitionDao.
 */
public class JdbcExhibitionDao implements ExhibitionDao {

    /**
     * Fetches all exhibitions from the database.
     */
    @Override
    public List<Exhibition> findAll() {
        List<Exhibition> exhibitions = new ArrayList<>();
        String query = "SELECT e.exhibition_id, e.titre, e.date_debut, e.theme, " +
                "g.gallery_id, g.nom, g.localisation, g.note " +
                "FROM exhibitions e " +
                "JOIN galleries g ON e.gallery_id = g.gallery_id";

        try (Connection conn = ConnectionManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Exhibition exhibition = mapResultSetToExhibition(rs);
                exhibitions.add(exhibition);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all exhibitions: " + e.getMessage());
            e.printStackTrace();
        }

        return exhibitions;
    }

    /**
     * Saves a new exhibition to the database.
     */
    @Override
    public void save(Exhibition exhibition) {
        // First, find the gallery_id by gallery name
        String findGalleryQuery = "SELECT gallery_id FROM galleries WHERE nom = ?";
        Integer galleryId = null;

        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(findGalleryQuery)) {

            pstmt.setString(1, exhibition.getGallery().getName());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    galleryId = rs.getInt("gallery_id");
                } else {
                    System.err.println("Gallery not found: " + exhibition.getGallery().getName());
                    return;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding gallery: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Now insert the exhibition
        String insertQuery = "INSERT INTO exhibitions (gallery_id, titre, date_debut, theme) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            pstmt.setInt(1, galleryId);
            pstmt.setString(2, exhibition.getTitle());
            pstmt.setDate(3, java.sql.Date.valueOf(exhibition.getStartDate()));
            pstmt.setString(4, exhibition.getTheme());

            pstmt.executeUpdate();
            System.out.println("Exhibition '" + exhibition.getTitle() + "' saved successfully.");
        } catch (SQLException e) {
            System.err.println("Error saving exhibition: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates an existing exhibition in the database.
     */
    @Override
    public void update(Exhibition exhibition) {
        String query = "UPDATE exhibitions SET theme = ?, date_debut = ? WHERE titre = ?";

        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, exhibition.getTheme());
            pstmt.setDate(2, java.sql.Date.valueOf(exhibition.getStartDate()));
            pstmt.setString(3, exhibition.getTitle());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Exhibition '" + exhibition.getTitle() + "' updated successfully.");
            } else {
                System.out.println("No exhibition found with title: " + exhibition.getTitle());
            }
        } catch (SQLException e) {
            System.err.println("Error updating exhibition: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Deletes an exhibition by title.
     */
    @Override
    public void delete(String title) {
        String query = "DELETE FROM exhibitions WHERE titre = ?";

        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, title);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Exhibition '" + title + "' deleted successfully.");
            } else {
                System.out.println("No exhibition found with title: " + title);
            }
        } catch (SQLException e) {
            System.err.println("Error deleting exhibition: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to map a ResultSet row to an Exhibition object with associated
     * Gallery.
     */
    private Exhibition mapResultSetToExhibition(ResultSet rs) throws SQLException {
        Exhibition exhibition = new Exhibition();
        exhibition.setTitle(rs.getString("titre"));

        // Map date
        java.sql.Date sqlDate = rs.getDate("date_debut");
        if (sqlDate != null) {
            exhibition.setStartDate(sqlDate.toLocalDate());
        }

        exhibition.setTheme(rs.getString("theme"));

        // Create and link Gallery
        Gallery gallery = new Gallery();
        gallery.setName(rs.getString("nom"));
        gallery.setAddress(rs.getString("localisation"));
        gallery.setRating(rs.getDouble("note"));
        exhibition.setGallery(gallery);

        return exhibition;
    }
}
