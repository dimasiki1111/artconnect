package com.project.artconnect.util;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.persistence.JdbcArtistDao;
import com.project.artconnect.persistence.JdbcArtworkDao;
import com.project.artconnect.persistence.JdbcCommunityMemberDao;
import com.project.artconnect.persistence.JdbcExhibitionDao;
import com.project.artconnect.persistence.JdbcGalleryDao;
import com.project.artconnect.persistence.JdbcWorkshopDao;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.service.ExhibitionService;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.service.impl.JdbcArtistService;
import com.project.artconnect.service.impl.JdbcArtworkService;
import com.project.artconnect.service.impl.JdbcCommunityService;
import com.project.artconnect.service.impl.JdbcExhibitionService;
import com.project.artconnect.service.impl.JdbcGalleryService;
import com.project.artconnect.service.impl.JdbcWorkshopService;

/**
 * Service Provider to manage singleton instances of services and handle their
 * initialization.
 * 
 * CONFIGURATION: To switch between InMemory and JDBC backends, uncomment the
 * appropriate initialization section below.
 */
public class ServiceProvider {

    // ============ JDBC BACKEND (Database) - UNCOMMENT TO USE ============
    // Initialize DAOs (JDBC implementations)
    private static final ArtistDao artistDao = new JdbcArtistDao();
    private static final ArtworkDao artworkDao = new JdbcArtworkDao();
    private static final CommunityMemberDao communityMemberDao = new JdbcCommunityMemberDao();
    private static final GalleryDao galleryDao = new JdbcGalleryDao();
    private static final ExhibitionDao exhibitionDao = new JdbcExhibitionDao();
    private static final WorkshopDao workshopDao = new JdbcWorkshopDao();

    // Initialize Services (JDBC-backed)
    private static final ArtistService artistService = new JdbcArtistService(artistDao);
    private static final ArtworkService artworkService = new JdbcArtworkService(artworkDao);
    private static final GalleryService galleryService = new JdbcGalleryService(galleryDao, exhibitionDao);
    private static final ExhibitionService exhibitionService = new JdbcExhibitionService(exhibitionDao);
    private static final WorkshopService workshopService = new JdbcWorkshopService(workshopDao);
    private static final CommunityService communityService = new JdbcCommunityService(communityMemberDao);

    // ============ IN-MEMORY BACKEND (Commented Out) ============
    // Uncomment the following lines to use in-memory services instead of JDBC:
    /*
     * private static final InMemoryArtistService artistService = new
     * InMemoryArtistService();
     * private static final InMemoryArtworkService artworkService = new
     * InMemoryArtworkService();
     * private static final InMemoryGalleryService galleryService = new
     * InMemoryGalleryService();
     * private static final InMemoryWorkshopService workshopService = new
     * InMemoryWorkshopService();
     * private static final InMemoryCommunityService communityService = new
     * InMemoryCommunityService();
     * 
     * static {
     * // Initialize services with their dependencies (in-memory mode)
     * artworkService.initData(artistService);
     * galleryService.initData(artworkService);
     * workshopService.initData(artistService);
     * communityService.initData(artworkService);
     * }
     */

    // Public accessors for services
    public static ArtistService getArtistService() {
        return artistService;
    }

    public static ArtworkService getArtworkService() {
        return artworkService;
    }

    public static GalleryService getGalleryService() {
        return galleryService;
    }

    public static ExhibitionService getExhibitionService() {
        return exhibitionService;
    }

    public static WorkshopService getWorkshopService() {
        return workshopService;
    }

    public static CommunityService getCommunityService() {
        return communityService;
    }
}
