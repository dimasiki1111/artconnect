use Art_connect;
-- =========================
-- ARTISTS
-- =========================
INSERT INTO artists (nom, city, email, birth_year) VALUES
('Emma Laurent', 'Paris', 'emma.laurent@example.com', 1992),
('Noah Petit', 'Lyon', 'noah.petit@example.com', 1988),
('Ines Haddad', 'Marseille', 'ines.haddad@example.com', 1996);

-- =========================
-- COMMUNITY
-- =========================
INSERT INTO community (name, email, city) VALUES
('Paris Art Hub', 'contact@parisarthub.com', 'Paris'),
('Lyon Creative Space', 'info@lyonspace.com', 'Lyon'),
('Marseille Visual Arts', 'hello@marseilleva.com', 'Marseille');

-- =========================
-- GALLERIES
-- =========================
INSERT INTO galleries (community_id, nom, localisation, note) VALUES
(1, 'Galerie Opéra', '10 Rue de l Opera, Paris', 4.8),
(2, 'Rhône Gallery', '22 Quai du Rhône, Lyon', 4.4),
(3, 'Vieux-Port Gallery', '5 Quai des Belges, Marseille', 4.6);

-- =========================
-- ARTWORKS
-- =========================
INSERT INTO artworks (artist_id, gallery_id, titre, type, prix, statut) VALUES
(1, 1, 'Paris Lumière', 'peinture', 1500.00, 'exposition'),
(2, 2, 'Formes Urbaines', 'sculpture', 2300.00, 'for_sale'),
(3, 3, 'Bleu Méditerranée', 'photographie', 900.00, 'sold');

-- =========================
-- EXHIBITIONS
-- =========================
INSERT INTO exhibitions (gallery_id, titre, date_debut, theme) VALUES
(1, 'Modern Paris', '2026-06-01', 'Art contemporain'),
(2, 'Rhône Vision', '2026-06-15', 'Abstraction urbaine'),
(3, 'Marseille Sea Art', '2026-07-10', 'Marine & lumière');

-- =========================
-- WORKSHOPS
-- =========================
INSERT INTO workshops (artist_id, titre, date, prix, niveau) VALUES
(1, 'Peinture débutant', '2026-06-05', 45.00, 'debutant'),
(2, 'Sculpture moderne', '2026-06-20', 70.00, 'intermediaire'),
(3, 'Photo artistique avancée', '2026-07-12', 80.00, 'avance');

select * from artists;
select * from community;
select * from galleries;
select * from artworks;
select * from exhibitions;
select * from workshops;