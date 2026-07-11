-- ================================================================
-- ArtConnect — Étape 3 | Tâche 4
-- Scénarios Transactionnels
-- Base : Art_connect
-- ================================================================
-- Prérequis : exécuter les scripts précédents dans l'ordre :
--   1. Art-connect_bdd.sql
--   2. Insert.sql
--   3. Rest.sql
--   4. artconnect_t3_triggers_procs.sql  (tables + triggers + procs)
-- ================================================================

USE Art_connect;

-- ================================================================
-- SCÉNARIO A — Inscription atomique d'un membre à plusieurs ateliers
-- ================================================================
-- Contexte : "Lyon Creative Space" souhaite s'inscrire à la fois
-- à "Sculpture moderne" (workshop 2) et "Photo artistique avancée"
-- (workshop 3). Les deux inscriptions doivent réussir ensemble,
-- ou être toutes les deux annulées si l'une échoue.
-- ================================================================

-- Nettoyage préventif (rend le scénario reproductible)
DELETE FROM inscription_atelier
WHERE community_id = 2 AND workshop_id IN (2, 3);

-- ── Test A1 : cas nominal (tout réussit) ────────────────────────
START TRANSACTION;

    INSERT INTO inscription_atelier (workshop_id, community_id, date_inscription)
    VALUES (2, 2, NOW());    -- Lyon Creative Space → Sculpture moderne

    INSERT INTO inscription_atelier (workshop_id, community_id, date_inscription)
    VALUES (3, 2, NOW());    -- Lyon Creative Space → Photo artistique avancée

COMMIT;

-- Vérification A1 : deux lignes attendues
SELECT ia.inscription_id,
       cm.name           AS membre,
       w.titre           AS atelier,
       w.date,
       ia.date_inscription
FROM inscription_atelier ia
JOIN workshops  w  ON ia.workshop_id  = w.workshop_id
JOIN community  cm ON ia.community_id = cm.community_id
WHERE ia.community_id = 2;


-- ── Test A2 : cas d'erreur → ROLLBACK ───────────────────────────
-- On tente d'inscrire "Marseille Visual Arts" (community_id=3) à
-- l'atelier 2 ET à un atelier inexistant (workshop_id=99).
-- La seconde insertion viole la FK → ROLLBACK total.
-- Résultat attendu : aucune inscription pour community_id=3.

DELETE FROM inscription_atelier WHERE community_id = 3;

START TRANSACTION;

    SAVEPOINT avant_inscriptions;

    INSERT INTO inscription_atelier (workshop_id, community_id, date_inscription)
    VALUES (2, 3, NOW());    -- OK : Marseille → Sculpture moderne

    -- La ligne suivante provoque une FK violation (workshop 99 inexistant)
    -- Décommenter pour tester le ROLLBACK :
    -- INSERT INTO inscription_atelier (workshop_id, community_id, date_inscription)
    -- VALUES (99, 3, NOW());

    -- Si on décommente la ligne ci-dessus, MySQL lève une erreur,
    -- et on doit exécuter ROLLBACK dans le client (ou gérer côté application).
    -- En production : géré dans le handler SQLEXCEPTION de la procédure.

COMMIT;

-- Vérification A2 (avec rollback simulé) :
SELECT COUNT(*) AS inscriptions_community_3
FROM inscription_atelier WHERE community_id = 3;
-- Sans ROLLBACK → 1 ; avec ROLLBACK → 0


-- ================================================================
-- SCÉNARIO B — Transfert d'une œuvre d'une galerie à une autre
-- ================================================================
-- Contexte : "Formes Urbaines" (artwork 2, Noah Petit) doit être
-- transférée de "Rhône Gallery" (gallery 2) à "Galerie Opéra"
-- (gallery 1) suite à un accord entre les deux galeries.
-- Opérations atomiques :
--   1. Vérifier que l'œuvre est bien dans la galerie source.
--   2. Mettre à jour gallery_id vers la galerie cible.
--   3. Créer une entrée dans l'exposition cible si elle existe.
-- ================================================================

-- État initial
SELECT artwork_id, titre, gallery_id, statut, prix
FROM artworks WHERE artwork_id = 2;

START TRANSACTION;

    SAVEPOINT avant_transfert;

    -- Étape 1 : vérification que l'œuvre est bien dans la galerie source
    -- (en production, stocker dans une variable et tester)
    SET @src_gallery = (SELECT gallery_id FROM artworks WHERE artwork_id = 2);

    -- Étape 2 : transfert vers la galerie cible
    UPDATE artworks
    SET gallery_id = 1         -- vers Galerie Opéra
    WHERE artwork_id = 2
      AND gallery_id = 2;      -- seulement si elle est bien en source

    -- Vérification que l'UPDATE a affecté 1 ligne
    -- (si 0 ligne → l'œuvre n'était pas dans la galerie source → ROLLBACK)
    SET @lignes = ROW_COUNT();

    -- Étape 3 : relier l'œuvre à une exposition de la galerie cible
    -- (exposition "Modern Paris" exhibition_id=1, dans gallery_id=1)
    -- Ici, on met à jour le statut cohérent avec son nouvel emplacement
    UPDATE artworks
    SET statut = 'exposition'
    WHERE artwork_id = 2;

COMMIT;

-- Vérification B : l'œuvre doit maintenant être dans la galerie 1
SELECT aw.artwork_id,
       aw.titre,
       aw.statut,
       g.nom        AS galerie_actuelle,
       ar.nom       AS artiste
FROM artworks aw
JOIN galleries g ON aw.gallery_id = g.gallery_id
JOIN artists   ar ON aw.artist_id  = ar.artist_id
WHERE aw.artwork_id = 2;


-- ── Simulation ROLLBACK sur Scénario B ──────────────────────────
-- Si on avait voulu transférer depuis la gallery 1 mais l'œuvre
-- est gallery 2, ROW_COUNT() = 0 → on rollback manuellement.
-- Ce cas est géré dans la couche application Java (vérifier rowCount).


-- ================================================================
-- SCÉNARIO C — Onboarding complet d'un nouvel artiste
-- ================================================================
-- Contexte : intégration d'un nouvel artiste "Léa Martin" sur la
-- plateforme. En une seule opération atomique :
--   1. Créer l'artiste dans la table artists.
--   2. Créer sa première œuvre et la relier à sa galerie.
--   3. Créer un atelier qu'il proposera.
-- Si l'une des étapes échoue (email déjà existant, galerie invalide…)
-- → toutes les insertions sont annulées.
-- ================================================================

-- Nettoyage préventif
DELETE FROM workshops WHERE artist_id = (SELECT artist_id FROM artists WHERE email = 'lea.martin@example.com');
DELETE FROM artworks  WHERE artist_id = (SELECT artist_id FROM artists WHERE email = 'lea.martin@example.com');
DELETE FROM artists   WHERE email     = 'lea.martin@example.com';

START TRANSACTION;

    -- Étape 1 : créer l'artiste
    INSERT INTO artists (nom, city, email, birth_year)
    VALUES ('Léa Martin', 'Bordeaux', 'lea.martin@example.com', 1994);

    SET @new_artist_id = LAST_INSERT_ID();

    -- Étape 2 : créer sa première œuvre, rattachée à la Galerie Opéra
    INSERT INTO artworks (artist_id, gallery_id, titre, type, prix, statut)
    VALUES (@new_artist_id, 1, 'Bordeaux Éternel', 'aquarelle', 850.00, 'for_sale');

    SET @new_artwork_id = LAST_INSERT_ID();

    -- Étape 3 : créer son premier atelier
    INSERT INTO workshops (artist_id, titre, date, prix, niveau, max_places)
    VALUES (@new_artist_id, 'Aquarelle pour tous', '2026-09-10', 55.00, 'debutant', 15);

    SET @new_workshop_id = LAST_INSERT_ID();

COMMIT;

-- Vérification C : les trois entités doivent exister
SELECT 'ARTISTE'  AS type, @new_artist_id  AS id, 'Léa Martin'         AS nom UNION ALL
SELECT 'OEUVRE',           @new_artwork_id,        'Bordeaux Éternel'        UNION ALL
SELECT 'ATELIER',          @new_workshop_id,        'Aquarelle pour tous';

SELECT ar.nom AS artiste, aw.titre AS oeuvre, w.titre AS atelier
FROM artists ar
JOIN artworks aw ON aw.artist_id = ar.artist_id
JOIN workshops w ON w.artist_id  = ar.artist_id
WHERE ar.artist_id = @new_artist_id;


-- ── Simulation ROLLBACK sur Scénario C ──────────────────────────
-- Même tentative mais avec un email déjà utilisé → ROLLBACK total :
-- Ni l'artiste, ni l'œuvre, ni l'atelier ne doivent persister.

-- START TRANSACTION;
--
--     INSERT INTO artists (nom, city, email, birth_year)
--     VALUES ('Doublon Test', 'Paris', 'emma.laurent@example.com', 1990); -- email déjà pris → ERREUR
--
--     INSERT INTO artworks (artist_id, gallery_id, titre, type, prix, statut)
--     VALUES (LAST_INSERT_ID(), 1, 'Tableau Doublon', 'peinture', 500.00, 'for_sale');
--
-- ROLLBACK;  -- Aucune des deux lignes ne doit persister


-- ================================================================
-- RÉCAPITULATIF DES RÉSULTATS ATTENDUS
-- ================================================================

-- État final de la base après tous les scénarios
SELECT 'inscription_atelier' AS table_name, COUNT(*) AS lignes FROM inscription_atelier UNION ALL
SELECT 'audit_artworks',                    COUNT(*)           FROM audit_artworks       UNION ALL
SELECT 'artists',                           COUNT(*)           FROM artists              UNION ALL
SELECT 'artworks',                          COUNT(*)           FROM artworks             UNION ALL
SELECT 'workshops',                         COUNT(*)           FROM workshops;

-- ================================================================
-- FIN — SCÉNARIOS TRANSACTIONNELS
-- ================================================================
