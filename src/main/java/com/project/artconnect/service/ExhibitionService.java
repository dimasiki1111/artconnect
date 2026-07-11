package com.project.artconnect.service;

import java.util.List;

import com.project.artconnect.model.Exhibition;

/**
 * Service interface for Exhibition operations.
 * Provides business logic for managing exhibitions.
 */
public interface ExhibitionService {
    /**
     * Retrieves all exhibitions.
     *
     * @return list of all exhibitions
     */
    List<Exhibition> getAllExhibitions();

    /**
     * Saves a new exhibition.
     *
     * @param exhibition the exhibition to save
     */
    void saveExhibition(Exhibition exhibition);

    /**
     * Updates an existing exhibition.
     *
     * @param exhibition the exhibition to update
     */
    void updateExhibition(Exhibition exhibition);

    /**
     * Deletes an exhibition by title.
     *
     * @param title the title of the exhibition to delete
     */
    void deleteExhibition(String title);
}
