package com.project.artconnect.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.util.ConnectionManager;

/**
 * JDBC implementation for WorkshopDao.
 */
public class JdbcWorkshopDao implements WorkshopDao {

    /**
     * Finds a workshop by ID.
     */
    @Override
    public Optional<Workshop> findById(Long id) {
        String query = "SELECT w.workshop_id, w.titre, w.date, w.prix, w.niveau, " +
                "ar.artist_id, ar.nom, ar.city, ar.email, ar.birth_year " +
                "FROM workshops w " +
                "JOIN artists ar ON w.artist_id = ar.artist_id " +
                "WHERE w.workshop_id = ?";

        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Workshop workshop = mapResultSetToWorkshop(rs);
                    return Optional.of(workshop);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding workshop by id: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Fetches all workshops from the database.
     */
    @Override
    public List<Workshop> findAll() {
        List<Workshop> workshops = new ArrayList<>();
        String query = "SELECT w.workshop_id, w.titre, w.date, w.prix, w.niveau, " +
                "ar.artist_id, ar.nom, ar.city, ar.email, ar.birth_year " +
                "FROM workshops w " +
                "JOIN artists ar ON w.artist_id = ar.artist_id";

        try (Connection conn = ConnectionManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Workshop workshop = mapResultSetToWorkshop(rs);
                workshops.add(workshop);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all workshops: " + e.getMessage());
            e.printStackTrace();
        }

        return workshops;
    }

    /**
     * Helper method to map a ResultSet row to a Workshop object with associated
     * Artist.
     */
    private Workshop mapResultSetToWorkshop(ResultSet rs) throws SQLException {
        Workshop workshop = new Workshop();
        workshop.setTitle(rs.getString("titre"));

        // Map date (assuming it's DATE type in database, converting to LocalDateTime)
        java.util.Date date = new java.util.Date(rs.getDate("date").getTime());
        LocalDateTime dateTime = LocalDateTime.of(
                rs.getDate("date").toLocalDate().getYear(),
                rs.getDate("date").toLocalDate().getMonth(),
                rs.getDate("date").toLocalDate().getDayOfMonth(),
                0, 0, 0);
        workshop.setDate(dateTime);

        workshop.setPrice(rs.getDouble("prix"));
        workshop.setLevel(rs.getString("niveau"));

        // Create and link Artist
        Artist artist = new Artist();
        artist.setName(rs.getString("nom"));
        artist.setCity(rs.getString("city"));
        artist.setContactEmail(rs.getString("email"));
        artist.setBirthYear(rs.getInt("birth_year"));
        workshop.setInstructor(artist);

        return workshop;
    }

    @Override
    public void save(Workshop workshop) {
        String query = "INSERT INTO workshops (titre, date, prix, niveau, artist_id) VALUES (?, ?, ?, ?, (SELECT artist_id FROM artists LIMIT 1))";
        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, workshop.getTitle());
            pstmt.setObject(2, workshop.getDate());
            pstmt.setDouble(3, workshop.getPrice());
            pstmt.setString(4, workshop.getLevel());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving workshop: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void update(Workshop workshop) {
        String query = "UPDATE workshops SET titre = ?, date = ?, prix = ?, niveau = ? WHERE titre = ?";
        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, workshop.getTitle());
            pstmt.setObject(2, workshop.getDate());
            pstmt.setDouble(3, workshop.getPrice());
            pstmt.setString(4, workshop.getLevel());
            pstmt.setString(5, workshop.getTitle());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating workshop: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM workshops WHERE workshop_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting workshop: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
