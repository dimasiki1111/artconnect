package com.project.artconnect.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;

/**
 * JDBC-backed implementation of ArtistService.
 * Replaces InMemoryArtistService by fetching data from the database via DAO.
 */
public class JdbcArtistService implements ArtistService {
    private final ArtistDao artistDao;

    // For now, disciplines are still in-memory (could be moved to DB in future)
    private final Map<String, Discipline> disciplines = new LinkedHashMap<>();

    public JdbcArtistService(ArtistDao artistDao) {
        this.artistDao = artistDao;
        initDisciplines();
    }

    /**
     * Initialize disciplines in memory.
     * In a production system, this should be fetched from the database.
     */
    private void initDisciplines() {
        disciplines.put("Painting", new Discipline("Painting"));
        disciplines.put("Sculpture", new Discipline("Sculpture"));
        disciplines.put("Photography", new Discipline("Photography"));
        disciplines.put("Digital Art", new Discipline("Digital Art"));
        disciplines.put("Music", new Discipline("Music"));
    }

    @Override
    public List<Artist> getAllArtists() {
        return artistDao.findAll();
    }

    @Override
    public Optional<Artist> getArtistByName(String name) {
        List<Artist> artists = artistDao.findAll();
        return artists.stream()
                .filter(a -> a.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public void createArtist(Artist artist) {
        artistDao.save(artist);
    }

    @Override
    public void updateArtist(Artist artist) {
        artistDao.update(artist);
    }

    @Override
    public void deleteArtist(String name) {
        artistDao.delete(name);
    }

    @Override
    public List<Discipline> getAllDisciplines() {
        return new ArrayList<>(disciplines.values());
    }

    @Override
    public List<Artist> searchArtists(String query, String disciplineName, String city) {
        List<Artist> artists = artistDao.findAll();

        return artists.stream()
                .filter(a -> query == null || a.getName().toLowerCase().contains(query.toLowerCase()))
                .filter(a -> city == null || city.isEmpty() || a.getCity().equalsIgnoreCase(city))
                .filter(a -> disciplineName == null ||
                        a.getDisciplines().stream().anyMatch(d -> d.getName().equals(disciplineName)))
                .collect(Collectors.toList());
    }
}
