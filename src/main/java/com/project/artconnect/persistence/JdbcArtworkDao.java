package com.project.artconnect.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.util.ConnectionManager;

/**
 * JDBC implementation for ArtworkDao.
 * Maps database artworks to Artwork model objects, including linking to
 * artists.
 */
public class JdbcArtworkDao implements ArtworkDao {

    /**
     * Fetches all artworks from the database.
     * Also loads the associated Artist for each artwork.
     */
    @Override
    public List<Artwork> findAll() {
        List<Artwork> artworks = new ArrayList<>();
        String query = "SELECT a.artwork_id, a.titre, a.type, a.prix, a.statut, " +
                "ar.artist_id, ar.nom, ar.city, ar.email, ar.birth_year " +
                "FROM artworks a " +
                "JOIN artists ar ON a.artist_id = ar.artist_id";

        try (Connection conn = ConnectionManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Artwork artwork = mapResultSetToArtwork(rs);
                artworks.add(artwork);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all artworks: " + e.getMessage());
            e.printStackTrace();
        }

        return artworks;
    }

    /**
     * Saves a new artwork to the database.
     * The artwork's artist must exist in the database.
     */
    @Override
    public void save(Artwork artwork) {
        // First, find the artist_id by artist name
        String findArtistQuery = "SELECT artist_id FROM artists WHERE nom = ?";
        Integer artistId = null;

        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(findArtistQuery)) {

            pstmt.setString(1, artwork.getArtist().getName());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    artistId = rs.getInt("artist_id");
                } else {
                    System.err.println("Artist not found: " + artwork.getArtist().getName());
                    return;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding artist: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Now insert the artwork
        String insertQuery = "INSERT INTO artworks (artist_id, titre, type, prix, statut) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            pstmt.setInt(1, artistId);
            pstmt.setString(2, artwork.getTitle());
            pstmt.setString(3, artwork.getType());
            pstmt.setDouble(4, artwork.getPrice());
            pstmt.setString(5, artwork.getStatus().toString().toLowerCase());

            pstmt.executeUpdate();
            System.out.println("Artwork '" + artwork.getTitle() + "' saved successfully.");
        } catch (SQLException e) {
            System.err.println("Error saving artwork: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates an existing artwork in the database.
     * Identifies the artwork by title (primary identifier in the model).
     */
    @Override
    public void update(Artwork artwork) {
        String query = "UPDATE artworks SET type = ?, prix = ?, statut = ? WHERE titre = ?";

        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, artwork.getType());
            pstmt.setDouble(2, artwork.getPrice());
            pstmt.setString(3, artwork.getStatus().toString().toLowerCase());
            pstmt.setString(4, artwork.getTitle());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Artwork '" + artwork.getTitle() + "' updated successfully.");
            } else {
                System.out.println("No artwork found with title: " + artwork.getTitle());
            }
        } catch (SQLException e) {
            System.err.println("Error updating artwork: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Deletes an artwork by title.
     */
    @Override
    public void delete(String title) {
        String query = "DELETE FROM artworks WHERE titre = ?";

        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, title);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Artwork '" + title + "' deleted successfully.");
            } else {
                System.out.println("No artwork found with title: " + title);
            }
        } catch (SQLException e) {
            System.err.println("Error deleting artwork: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Finds all artworks by a specific artist name.
     */
    @Override
    public List<Artwork> findByArtistName(String artistName) {
        List<Artwork> artworks = new ArrayList<>();
        String query = "SELECT a.artwork_id, a.titre, a.type, a.prix, a.statut, " +
                "ar.artist_id, ar.nom, ar.city, ar.email, ar.birth_year " +
                "FROM artworks a " +
                "JOIN artists ar ON a.artist_id = ar.artist_id " +
                "WHERE ar.nom = ?";

        try (Connection conn = ConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, artistName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Artwork artwork = mapResultSetToArtwork(rs);
                    artworks.add(artwork);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding artworks by artist: " + e.getMessage());
            e.printStackTrace();
        }

        return artworks;
    }

    /**
     * Helper method to map a ResultSet row to an Artwork object with associated
     * Artist.
     */
    private Artwork mapResultSetToArtwork(ResultSet rs) throws SQLException {
        Artwork artwork = new Artwork();
        artwork.setTitle(rs.getString("titre"));
        artwork.setType(rs.getString("type"));
        artwork.setPrice(rs.getDouble("prix"));

        // Map status enum
        String statusStr = rs.getString("statut");
        if (statusStr != null) {
            try {
                artwork.setStatus(Artwork.Status.valueOf(statusStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                artwork.setStatus(Artwork.Status.FOR_SALE); // Default
            }
        }

        // Create and link Artist
        Artist artist = new Artist();
        artist.setName(rs.getString("nom"));
        artist.setCity(rs.getString("city"));
        artist.setContactEmail(rs.getString("email"));
        artist.setBirthYear(rs.getInt("birth_year"));
        artwork.setArtist(artist);

        return artwork;
    }
}
