package com.project.artconnect.dao;

import java.util.List;
import java.util.Optional;

import com.project.artconnect.model.Workshop;

public interface WorkshopDao {
    Optional<Workshop> findById(Long id);

    List<Workshop> findAll();

    void save(Workshop workshop);

    void update(Workshop workshop);

    void delete(int id);
}
