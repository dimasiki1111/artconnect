package com.project.artconnect.service;

import java.util.List;
import java.util.Optional;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;

public interface GalleryService {
    List<Gallery> getAllGalleries();

    Optional<Gallery> getGalleryByName(String name);

    List<Exhibition> getExhibitionsByGallery(Gallery gallery);

    void saveGallery(Gallery gallery);

    void updateGallery(Gallery gallery);

    void deleteGallery(String name);
}
