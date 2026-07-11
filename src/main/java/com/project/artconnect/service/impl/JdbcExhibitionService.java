package com.project.artconnect.service.impl;

import java.util.List;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.service.ExhibitionService;

/**
 * JDBC implementation of ExhibitionService.
 * Delegates business logic to the ExhibitionDao.
 */
public class JdbcExhibitionService implements ExhibitionService {
    private final ExhibitionDao exhibitionDao;

    /**
     * Constructor with dependency injection.
     *
     * @param exhibitionDao the DAO for exhibition operations
     */
    public JdbcExhibitionService(ExhibitionDao exhibitionDao) {
        this.exhibitionDao = exhibitionDao;
    }

    /**
     * Retrieves all exhibitions from the database.
     *
     * @return list of all exhibitions
     */
    @Override
    public List<Exhibition> getAllExhibitions() {
        return exhibitionDao.findAll();
    }

    /**
     * Saves a new exhibition to the database.
     *
     * @param exhibition the exhibition to save
     */
    @Override
    public void saveExhibition(Exhibition exhibition) {
        exhibitionDao.save(exhibition);
    }

    /**
     * Updates an existing exhibition in the database.
     *
     * @param exhibition the exhibition to update
     */
    @Override
    public void updateExhibition(Exhibition exhibition) {
        exhibitionDao.update(exhibition);
    }

    /**
     * Deletes an exhibition by title.
     *
     * @param title the title of the exhibition to delete
     */
    @Override
    public void deleteExhibition(String title) {
        exhibitionDao.delete(title);
    }
}
