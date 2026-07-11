-- ================================================================
-- ArtConnect — Étape 3 | Tâche 3
-- Déclencheurs (Triggers) et Programmes Stockés
-- Base : Art_connect
-- Auteur : Équipe projet TI603
-- ================================================================
-- Dépendances : Art-connect_bdd.sql + Insert.sql + Rest.sql
-- exécuter ce script APRÈS les trois fichiers ci-dessus.
-- ================================================================

USE Art_connect;

-- ================================================================
-- PRÉAMBULE — Ajouts de colonnes et tables nécessaires
-- (extensions cohérentes avec le modèle existant)
-- ================================================================

-- Ajout de date_fin sur exhibitions pour la cohérence temporelle
ALTER TABLE exhibitions
    ADD COLUMN date_fin DATE NULL
        COMMENT 'Date de fin de l exposition (doit être > date_debut)';

-- Ajout de max_places sur workshops pour la gestion des capacités
ALTER TABLE workshops
    ADD COLUMN max_places INT NOT NULL DEFAULT 20
        COMMENT 'Nombre maximal de participants';

-- Table d'inscription aux ateliers (relation community ↔ workshops)
CREATE TABLE IF NOT EXISTS inscription_atelier (
    inscription_id  INT  PRIMARY KEY AUTO_INCREMENT,
    workshop_id     INT  NOT NULL,
    community_id    INT  NOT NULL,
    date_inscription DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ia_workshop   FOREIGN KEY (workshop_id)  REFERENCES workshops(workshop_id)  ON DELETE CASCADE,
    CONSTRAINT fk_ia_community  FOREIGN KEY (community_id) REFERENCES community(community_id) ON DELETE CASCADE,
    CONSTRAINT uq_ia_unique     UNIQUE (workshop_id, community_id)
) ENGINE = InnoDB COMMENT = 'Inscriptions des membres communautaires aux ateliers';

-- Table d'audit des modifications d'œuvres
CREATE TABLE IF NOT EXISTS audit_artworks (
    audit_id        INT  PRIMARY KEY AUTO_INCREMENT,
    artwork_id      INT  NOT NULL,
    ancien_statut   ENUM('for_sale','sold','exposition'),
    nouveau_statut  ENUM('for_sale','sold','exposition'),
    ancien_prix     DECIMAL(10,2),
    nouveau_prix    DECIMAL(10,2),
    modifie_le      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB COMMENT = 'Journal des modifications de statut et de prix des oeuvres';


-- ================================================================
-- SECTION 1 — DÉCLENCHEURS (TRIGGERS)
-- T1 : Cohérence des dates d'exposition
-- T2 : Capacité maximale des ateliers
-- T3 : Audit des modifications d'œuvres
-- T4 : Blocage de la réactivation d'une œuvre vendue
-- ================================================================


-- ----------------------------------------------------------------
-- T1 : trg_verif_dates_exposition
-- Objectif : garantir que date_fin est strictement postérieure
--            à date_debut lors de tout INSERT ou UPDATE.
-- ----------------------------------------------------------------

DELIMITER $$

DROP TRIGGER IF EXISTS trg_verif_dates_expo_insert$$
CREATE TRIGGER trg_verif_dates_expo_insert
BEFORE INSERT ON exhibitions
FOR EACH ROW
BEGIN
    IF NEW.date_fin IS NOT NULL AND NEW.date_fin <= NEW.date_debut THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
            '[T1] Erreur : date_fin doit être postérieure à date_debut.';
    END IF;
END$$

DROP TRIGGER IF EXISTS trg_verif_dates_expo_update$$
CREATE TRIGGER trg_verif_dates_expo_update
BEFORE UPDATE ON exhibitions
FOR EACH ROW
BEGIN
    IF NEW.date_fin IS NOT NULL AND NEW.date_fin <= NEW.date_debut THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
            '[T1] Erreur : date_fin doit être postérieure à date_debut.';
    END IF;
END$$

DELIMITER ;

-- ── Tests T1 ────────────────────────────────────────────────────

-- Cas 1 : dates cohérentes → doit réussir
UPDATE exhibitions
SET date_fin = '2026-06-30'
WHERE exhibition_id = 1;   -- Modern Paris : 2026-06-01 → 2026-06-30 ✓

-- Cas 2 : date_fin antérieure à date_debut → doit lever une erreur
-- UPDATE exhibitions SET date_fin = '2026-05-01' WHERE exhibition_id = 1;


-- ----------------------------------------------------------------
-- T2 : trg_verif_places_atelier
-- Objectif : bloquer une inscription si l'atelier est déjà
--            complet (nb d'inscrits >= max_places).
-- ----------------------------------------------------------------

DELIMITER $$

DROP TRIGGER IF EXISTS trg_verif_places_atelier$$
CREATE TRIGGER trg_verif_places_atelier
BEFORE INSERT ON inscription_atelier
FOR EACH ROW
BEGIN
    DECLARE v_max    INT;
    DECLARE v_count  INT;

    SELECT max_places INTO v_max
    FROM workshops
    WHERE workshop_id = NEW.workshop_id;

    SELECT COUNT(*) INTO v_count
    FROM inscription_atelier
    WHERE workshop_id = NEW.workshop_id;

    IF v_count >= v_max THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
            '[T2] Erreur : cet atelier a atteint sa capacité maximale.';
    END IF;
END$$

DELIMITER ;

-- ── Tests T2 ────────────────────────────────────────────────────

-- Réduire temporairement la capacité de l'atelier 1 à 1 pour tester
UPDATE workshops SET max_places = 1 WHERE workshop_id = 1;

-- Première inscription → doit réussir
INSERT INTO inscription_atelier (workshop_id, community_id)
VALUES (1, 1);

-- Deuxième inscription → doit échouer (capacité = 1)
-- INSERT INTO inscription_atelier (workshop_id, community_id) VALUES (1, 2);

-- Remise à la valeur normale
UPDATE workshops SET max_places = 20 WHERE workshop_id = 1;
DELETE FROM inscription_atelier WHERE workshop_id = 1 AND community_id = 1;


-- ----------------------------------------------------------------
-- T3 : trg_audit_artwork_modif
-- Objectif : enregistrer automatiquement dans audit_artworks
--            toute modification de statut ou de prix d'une œuvre,
--            pour assurer une traçabilité complète.
-- ----------------------------------------------------------------

DELIMITER $$

DROP TRIGGER IF EXISTS trg_audit_artwork_modif$$
CREATE TRIGGER trg_audit_artwork_modif
AFTER UPDATE ON artworks
FOR EACH ROW
BEGIN
    -- On n'insère une ligne que si statut ou prix a réellement changé
    IF OLD.statut <> NEW.statut OR
       (OLD.prix <> NEW.prix OR
        (OLD.prix IS NULL AND NEW.prix IS NOT NULL) OR
        (OLD.prix IS NOT NULL AND NEW.prix IS NULL))
    THEN
        INSERT INTO audit_artworks
            (artwork_id, ancien_statut, nouveau_statut, ancien_prix, nouveau_prix, modifie_le)
        VALUES
            (OLD.artwork_id, OLD.statut, NEW.statut, OLD.prix, NEW.prix, NOW());
    END IF;
END$$

DELIMITER ;

-- ── Tests T3 ────────────────────────────────────────────────────

-- Modifier le prix de 'Formes Urbaines' → doit créer une ligne d'audit
UPDATE artworks
SET prix = 2800.00
WHERE artwork_id = 2;   -- Formes Urbaines (Noah Petit)

SELECT * FROM audit_artworks;   -- une ligne attendue

-- Modifier le statut → une 2e ligne d'audit
UPDATE artworks
SET statut = 'exposition'
WHERE artwork_id = 2;

SELECT * FROM audit_artworks;   -- deux lignes attendues


-- ----------------------------------------------------------------
-- T4 : trg_bloquer_reactivation_sold
-- Objectif : une œuvre marquée 'sold' est définitivement vendue.
--            Elle ne peut plus repasser à 'for_sale' ni à
--            'exposition'. Cela garantit l'intégrité des ventes.
-- ----------------------------------------------------------------

DELIMITER $$

DROP TRIGGER IF EXISTS trg_bloquer_reactivation_sold$$
CREATE TRIGGER trg_bloquer_reactivation_sold
BEFORE UPDATE ON artworks
FOR EACH ROW
BEGIN
    IF OLD.statut = 'sold' AND NEW.statut <> 'sold' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
            '[T4] Erreur : une oeuvre vendue (sold) ne peut pas changer de statut.';
    END IF;
END$$

DELIMITER ;

-- ── Tests T4 ────────────────────────────────────────────────────

-- Cas 1 : 'Bleu Méditerranée' est sold → tenter de la remettre for_sale → ERREUR
-- UPDATE artworks SET statut = 'for_sale' WHERE artwork_id = 3;

-- Cas 2 : passer 'Paris Lumière' de 'exposition' à 'for_sale' → doit réussir
UPDATE artworks
SET statut = 'for_sale'
WHERE artwork_id = 1;

-- Cas 3 : passer 'Paris Lumière' à 'sold' → réussit
UPDATE artworks
SET statut = 'sold'
WHERE artwork_id = 1;

-- Cas 4 : tenter de la remettre à 'exposition' → doit échouer
-- UPDATE artworks SET statut = 'exposition' WHERE artwork_id = 1;

-- Remise en état pour ne pas perturber les données
UPDATE artworks SET statut = 'exposition', prix = 1500.00 WHERE artwork_id = 1;
-- Note : T4 bloque après SOLD → on remet manuellement pour les tests suivants
-- Dans un vrai scénario, on n'aurait jamais à revenir en arrière.
DELETE FROM audit_artworks;   -- Nettoyage de l'audit de test


-- ================================================================
-- SECTION 2 — FONCTIONS STOCKÉES
-- F1 : fn_nb_inscrits_atelier
-- F2 : fn_valeur_totale_galerie
-- ================================================================


-- ----------------------------------------------------------------
-- F1 : fn_nb_inscrits_atelier
-- Retourne le nombre de membres inscrits à un atelier donné.
-- Usage : SELECT fn_nb_inscrits_atelier(1);
-- ----------------------------------------------------------------

DELIMITER $$

DROP FUNCTION IF EXISTS fn_nb_inscrits_atelier$$
CREATE FUNCTION fn_nb_inscrits_atelier(p_workshop_id INT)
RETURNS INT
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_nb INT DEFAULT 0;

    SELECT COUNT(*) INTO v_nb
    FROM inscription_atelier
    WHERE workshop_id = p_workshop_id;

    RETURN v_nb;
END$$

DELIMITER ;

-- Test F1
SELECT w.titre,
       fn_nb_inscrits_atelier(w.workshop_id) AS nb_inscrits,
       w.max_places
FROM workshops w;


-- ----------------------------------------------------------------
-- F2 : fn_valeur_totale_galerie
-- Retourne la somme des prix des œuvres 'for_sale'
-- exposées dans une galerie donnée.
-- Usage : SELECT fn_valeur_totale_galerie(1);
-- ----------------------------------------------------------------

DELIMITER $$

DROP FUNCTION IF EXISTS fn_valeur_totale_galerie$$
CREATE FUNCTION fn_valeur_totale_galerie(p_gallery_id INT)
RETURNS DECIMAL(15,2)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_total DECIMAL(15,2) DEFAULT 0.00;

    SELECT COALESCE(SUM(prix), 0.00) INTO v_total
    FROM artworks
    WHERE gallery_id = p_gallery_id
      AND statut = 'for_sale';

    RETURN v_total;
END$$

DELIMITER ;

-- Test F2
SELECT g.nom,
       fn_valeur_totale_galerie(g.gallery_id) AS valeur_oeuvres_en_vente
FROM galleries g
ORDER BY valeur_oeuvres_en_vente DESC;


-- ================================================================
-- SECTION 3 — PROCÉDURES STOCKÉES
-- P1 : sp_creer_exposition_avec_oeuvre
-- P2 : sp_inscrire_membre_atelier
-- P3 : sp_profil_artiste
-- ================================================================


-- ----------------------------------------------------------------
-- P1 : sp_creer_exposition_avec_oeuvre
-- Crée une exposition dans une galerie ET rattache automatiquement
-- une œuvre à cette exposition en mettant son statut à 'exposition'.
-- Les deux opérations sont atomiques.
-- ----------------------------------------------------------------

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_creer_exposition_avec_oeuvre$$
CREATE PROCEDURE sp_creer_exposition_avec_oeuvre(
    IN  p_gallery_id   INT,
    IN  p_titre        VARCHAR(200),
    IN  p_date_debut   DATE,
    IN  p_date_fin     DATE,
    IN  p_theme        VARCHAR(100),
    IN  p_artwork_id   INT,
    OUT p_expo_id      INT,
    OUT p_message      VARCHAR(200)
)
BEGIN
    DECLARE v_artwork_existe INT DEFAULT 0;
    DECLARE v_gallery_existe INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_expo_id = -1;
        SET p_message = 'Erreur inattendue — transaction annulée.';
        RESIGNAL;
    END;

    -- Vérifications préalables
    SELECT COUNT(*) INTO v_gallery_existe FROM galleries WHERE gallery_id = p_gallery_id;
    SELECT COUNT(*) INTO v_artwork_existe FROM artworks  WHERE artwork_id = p_artwork_id;

    IF v_gallery_existe = 0 THEN
        SET p_expo_id = -1;
        SET p_message = 'Erreur : galerie introuvable.';
    ELSEIF v_artwork_existe = 0 THEN
        SET p_expo_id = -1;
        SET p_message = 'Erreur : oeuvre introuvable.';
    ELSE
        START TRANSACTION;

            -- 1. Créer l'exposition
            INSERT INTO exhibitions (gallery_id, titre, date_debut, date_fin, theme)
            VALUES (p_gallery_id, p_titre, p_date_debut, p_date_fin, p_theme);

            SET p_expo_id = LAST_INSERT_ID();

            -- 2. Rattacher l'œuvre à cette galerie et la passer en 'exposition'
            UPDATE artworks
            SET gallery_id = p_gallery_id,
                statut     = 'exposition'
            WHERE artwork_id = p_artwork_id;

        COMMIT;

        SET p_message = CONCAT('[OK] Exposition "', p_titre,
                               '" créée (id=', p_expo_id, ').',
                               ' Oeuvre ', p_artwork_id, ' rattachée.');
    END IF;
END$$

DELIMITER ;

-- Test P1
CALL sp_creer_exposition_avec_oeuvre(
    2,                    -- Rhône Gallery
    'Sculptures du Rhône',-- titre
    '2026-08-01',         -- date_debut
    '2026-09-30',         -- date_fin
    'Sculpture contemporaine',
    2,                    -- artwork_id : Formes Urbaines (Noah Petit)
    @expo_id,
    @msg
);
SELECT @expo_id AS nouvelle_expo, @msg AS resultat;

-- Vérification
SELECT e.exhibition_id, e.titre, e.date_debut, e.date_fin, e.theme,
       a.titre AS oeuvre, a.statut
FROM exhibitions e
JOIN artworks a ON a.gallery_id = e.gallery_id
WHERE e.exhibition_id = @expo_id;


-- ----------------------------------------------------------------
-- P2 : sp_inscrire_membre_atelier
-- Inscrit un membre de la communauté à un atelier avec contrôles :
--   • membre et atelier doivent exister,
--   • atelier non passé,
--   • pas de doublon,
--   • capacité disponible.
-- ----------------------------------------------------------------

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_inscrire_membre_atelier$$
CREATE PROCEDURE sp_inscrire_membre_atelier(
    IN  p_community_id INT,
    IN  p_workshop_id  INT,
    OUT p_message      VARCHAR(200)
)
sp: BEGIN
    DECLARE v_member_ok  INT DEFAULT 0;
    DECLARE v_workshop_ok INT DEFAULT 0;
    DECLARE v_date_ws    DATE;
    DECLARE v_max        INT;
    DECLARE v_count      INT;
    DECLARE v_doublon    INT DEFAULT 0;

    -- 1. Vérification membre
    SELECT COUNT(*) INTO v_member_ok
    FROM community WHERE community_id = p_community_id;

    IF v_member_ok = 0 THEN
        SET p_message = 'Erreur : membre introuvable.';
        LEAVE sp;
    END IF;

    -- 2. Vérification atelier
    SELECT COUNT(*), date, max_places
    INTO v_workshop_ok, v_date_ws, v_max
    FROM workshops WHERE workshop_id = p_workshop_id;

    IF v_workshop_ok = 0 THEN
        SET p_message = 'Erreur : atelier introuvable.';
        LEAVE sp;
    END IF;

    -- 3. Atelier non passé
    IF v_date_ws < CURDATE() THEN
        SET p_message = 'Erreur : impossible de s inscrire a un atelier dont la date est dépassée.';
        LEAVE sp;
    END IF;

    -- 4. Doublon
    SELECT COUNT(*) INTO v_doublon
    FROM inscription_atelier
    WHERE workshop_id = p_workshop_id AND community_id = p_community_id;

    IF v_doublon > 0 THEN
        SET p_message = 'Information : ce membre est déjà inscrit à cet atelier.';
        LEAVE sp;
    END IF;

    -- 5. Capacité (le trigger T2 est aussi là en filet de sécurité)
    SELECT COUNT(*) INTO v_count
    FROM inscription_atelier WHERE workshop_id = p_workshop_id;

    IF v_count >= v_max THEN
        SET p_message = 'Erreur : capacité maximale atteinte pour cet atelier.';
        LEAVE sp;
    END IF;

    -- 6. Inscription effective
    INSERT INTO inscription_atelier (workshop_id, community_id, date_inscription)
    VALUES (p_workshop_id, p_community_id, NOW());

    SET p_message = CONCAT('[OK] Membre ', p_community_id,
                           ' inscrit à l atelier ', p_workshop_id, '.',
                           ' Places restantes : ', v_max - v_count - 1);
END$$

DELIMITER ;

-- Tests P2
CALL sp_inscrire_membre_atelier(1, 2, @msg); SELECT @msg;  -- Paris Art Hub → Sculpture moderne  ✓
CALL sp_inscrire_membre_atelier(1, 2, @msg); SELECT @msg;  -- doublon → message informatif
CALL sp_inscrire_membre_atelier(99, 1, @msg); SELECT @msg; -- membre inexistant → erreur


-- ----------------------------------------------------------------
-- P3 : sp_profil_artiste
-- Génère un rapport complet sur un artiste en 3 jeux de résultats :
--   RS1 — Informations personnelles + indicateur de valeur
--   RS2 — Œuvres (titre, type, prix formaté, statut)
--   RS3 — Ateliers animés (titre, date, niveau, inscrits/places)
-- ----------------------------------------------------------------

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_profil_artiste$$
CREATE PROCEDURE sp_profil_artiste(IN p_artist_id INT)
BEGIN
    -- RS1 : Fiche artiste
    SELECT ar.artist_id,
           ar.nom,
           ar.city,
           ar.birth_year,
           COUNT(aw.artwork_id)                           AS nb_oeuvres,
           fn_valeur_totale_galerie(
               MIN(aw.gallery_id))                        AS valeur_en_galerie
    FROM artists ar
    LEFT JOIN artworks aw ON ar.artist_id = aw.artist_id
    WHERE ar.artist_id = p_artist_id
    GROUP BY ar.artist_id, ar.nom, ar.city, ar.birth_year;

    -- RS2 : Œuvres de l'artiste
    SELECT aw.titre,
           aw.type,
           CONCAT(FORMAT(aw.prix, 2), ' €')   AS prix_formatte,
           aw.statut,
           COALESCE(g.nom, 'Sans galerie')     AS galerie
    FROM artworks aw
    LEFT JOIN galleries g ON aw.gallery_id = g.gallery_id
    WHERE aw.artist_id = p_artist_id
    ORDER BY aw.artwork_id;

    -- RS3 : Ateliers animés par l'artiste
    SELECT w.titre,
           w.date,
           w.niveau,
           CONCAT(FORMAT(w.prix, 2), ' €')    AS prix_atelier,
           fn_nb_inscrits_atelier(w.workshop_id) AS inscrits,
           w.max_places                          AS capacite
    FROM workshops w
    WHERE w.artist_id = p_artist_id
    ORDER BY w.date;
END$$

DELIMITER ;

-- Test P3
CALL sp_profil_artiste(1);  -- Profil complet d'Emma Laurent
CALL sp_profil_artiste(2);  -- Profil complet de Noah Petit


-- ================================================================
-- FIN — SECTION TRIGGERS & PROGRAMMES STOCKÉS
-- ================================================================
