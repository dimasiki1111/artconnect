package com.project.artconnect.dao;

import java.util.List;
import java.util.Optional;

import com.project.artconnect.model.Gallery;

public interface GalleryDao {
    Optional<Gallery> findById(Long id);

    List<Gallery> findAll();

    void save(Gallery gallery);

    void update(Gallery gallery);

    void delete(String name);
}
