package com.project.artconnect.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Booking;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.WorkshopService;

/**
 * JDBC-backed implementation of WorkshopService.
 * Replaces InMemoryWorkshopService by fetching data from the database via DAO.
 */
public class JdbcWorkshopService implements WorkshopService {
    private final WorkshopDao workshopDao;

    public JdbcWorkshopService(WorkshopDao workshopDao) {
        this.workshopDao = workshopDao;
    }

    @Override
    public List<Workshop> getAllWorkshops() {
        return workshopDao.findAll();
    }

    @Override
    public Optional<Workshop> getWorkshopByTitle(String title) {
        List<Workshop> workshops = workshopDao.findAll();
        return workshops.stream()
                .filter(w -> w.getTitle().equalsIgnoreCase(title))
                .findFirst();
    }

    @Override
    public void bookWorkshop(Workshop workshop, CommunityMember member) {
        // TODO: Implement when Booking DAO is available
        System.out.println("Workshop booking not yet implemented for JDBC backend.");
    }

    @Override
    public List<Booking> getBookingsByMember(CommunityMember member) {
        // TODO: Implement when Booking DAO is available
        return new ArrayList<>();
    }

    @Override
    public void saveWorkshop(Workshop workshop) {
        workshopDao.save(workshop);
    }

    @Override
    public void updateWorkshop(Workshop workshop) {
        workshopDao.update(workshop);
    }

    @Override
    public void deleteWorkshop(int id) {
        workshopDao.delete(id);
    }
}
