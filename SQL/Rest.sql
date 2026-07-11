use Art_connect;
-- Vue 1 : œuvres disponibles à la vente
CREATE VIEW vue_artworks_disponibles AS
SELECT 
    a.artwork_id,
    a.titre,
    a.type,
    a.prix,
    ar.nom AS artiste,
    g.nom AS galerie
FROM artworks a
JOIN artists ar ON a.artist_id = ar.artist_id
LEFT JOIN galleries g ON a.gallery_id = g.gallery_id
WHERE a.statut = 'for_sale';

-- Vue 2 : artistes sans email (sécurité)
CREATE VIEW vue_artistes_public AS
SELECT 
    artist_id,
    nom,
    city,
    birth_year
FROM artists;

-- Vue 3 : expositions avec galerie
CREATE VIEW vue_expositions AS
SELECT 
    e.exhibition_id,
    e.titre,
    e.date_debut,
    e.theme,
    g.nom AS galerie,
    g.localisation
FROM exhibitions e
JOIN galleries g ON e.gallery_id = g.gallery_id;

-- Index pour filtrer les œuvres par statut
CREATE INDEX idx_artworks_statut ON artworks(statut);

-- Index pour accélérer les jointures avec artists
CREATE INDEX idx_artworks_artist ON artworks(artist_id);

-- Index pour accélérer les jointures avec galleries
CREATE INDEX idx_artworks_gallery ON artworks(gallery_id);

-- Index pour optimiser les recherches d'expositions par galerie
CREATE INDEX idx_exhibitions_gallery ON exhibitions(gallery_id);

-- Index pour les recherches par ville des artistes
CREATE INDEX idx_artists_city ON artists(city);

