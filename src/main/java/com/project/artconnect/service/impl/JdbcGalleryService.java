package com.project.artconnect.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.GalleryService;

/**
 * JDBC-backed implementation of GalleryService.
 * Replaces InMemoryGalleryService by fetching data from the database via DAO.
 */
public class JdbcGalleryService implements GalleryService {
    private final GalleryDao galleryDao;
    private final ExhibitionDao exhibitionDao;

    public JdbcGalleryService(GalleryDao galleryDao, ExhibitionDao exhibitionDao) {
        this.galleryDao = galleryDao;
        this.exhibitionDao = exhibitionDao;
    }

    @Override
    public List<Gallery> getAllGalleries() {
        return galleryDao.findAll();
    }

    @Override
    public Optional<Gallery> getGalleryByName(String name) {
        List<Gallery> galleries = galleryDao.findAll();
        return galleries.stream()
                .filter(g -> g.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public List<Exhibition> getExhibitionsByGallery(Gallery gallery) {
        List<Exhibition> allExhibitions = exhibitionDao.findAll();
        return allExhibitions.stream()
                .filter(e -> e.getGallery() != null && e.getGallery().getName().equals(gallery.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public void saveGallery(Gallery gallery) {
        galleryDao.save(gallery);
    }

    @Override
    public void updateGallery(Gallery gallery) {
        galleryDao.update(gallery);
    }

    @Override
    public void deleteGallery(String name) {
        galleryDao.delete(name);
    }
}
