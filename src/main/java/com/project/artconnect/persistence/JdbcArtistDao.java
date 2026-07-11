package com.project.artconnect.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.util.ConnectionManager;

/**
 * JDBC implementation for ArtistDao.
 * Maps database artists to Artist model objects.
 */
public class JdbcArtistDao implements ArtistDao {

    /**
     * Fetches all artists from the database.
     * Note: Disciplines are not loaded here (separate concern).
     */
    @Override
    public List<Artist> findAll() {
        List<Artist> artists = new ArrayList<>();
        String query = "SELECT artist_id, nom, city, email, birth_year FROM artists";

        try (Connection conn = ConnectionManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Artist artist = mapResultSetToArtist(rs);
                artists.add(artist);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all artists: " + e.getMessage());
            e.printStackTrace();
        }

        return artists;
    }

    /**
     * Saves a new artist to the database.
     */
    @Override
    public void save(Artist artist) {
        String query = "INSERT INTO artists (nom, city, email, birth_year) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, artist.getName());
            pstmt.setString(2, artist.getCity());
            pstmt.setString(3, artist.getContactEmail());
            pstmt.setInt(4, artist.getBirthYear() != null ? artist.getBirthYear() : 0);

            pstmt.executeUpdate();
            System.out.println("Artist '" + artist.getName() + "' saved successfully.");
        } catch (SQLException e) {
            System.err.println("Error saving artist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates an existing artist in the database.
     * Identifies the artist by name (primary identifier in the model).
     */
    @Override
    public void update(Artist artist) {
        String query = "UPDATE artists SET city = ?, email = ?, birth_year = ? WHERE nom = ?";

        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, artist.getCity());
            pstmt.setString(2, artist.getContactEmail());
            pstmt.setInt(3, artist.getBirthYear() != null ? artist.getBirthYear() : 0);
            pstmt.setString(4, artist.getName());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Artist '" + artist.getName() + "' updated successfully.");
            } else {
                System.out.println("No artist found with name: " + artist.getName());
            }
        } catch (SQLException e) {
            System.err.println("Error updating artist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Deletes an artist by name.
     */
    @Override
    public void delete(String artistName) {
        String query = "DELETE FROM artists WHERE nom = ?";

        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, artistName);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Artist '" + artistName + "' deleted successfully.");
            } else {
                System.out.println("No artist found with name: " + artistName);
            }
        } catch (SQLException e) {
            System.err.println("Error deleting artist: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Finds all artists from a specific city.
     */
    @Override
    public List<Artist> findByCity(String city) {
        List<Artist> artists = new ArrayList<>();
        String query = "SELECT artist_id, nom, city, email, birth_year FROM artists WHERE city = ?";

        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, city);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Artist artist = mapResultSetToArtist(rs);
                    artists.add(artist);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding artists by city: " + e.getMessage());
            e.printStackTrace();
        }

        return artists;
    }

    /**
     * Helper method to map a ResultSet row to an Artist object.
     */
    private Artist mapResultSetToArtist(ResultSet rs) throws SQLException {
        Artist artist = new Artist();
        artist.setName(rs.getString("nom"));
        artist.setCity(rs.getString("city"));
        artist.setContactEmail(rs.getString("email"));
        artist.setBirthYear(rs.getInt("birth_year"));
        return artist;
    }
}
