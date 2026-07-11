create database Art_connect;
use Art_connect;
CREATE TABLE artists (
 artist_id INT PRIMARY KEY AUTO_INCREMENT,
 nom VARCHAR(100) NOT NULL,
 city VARCHAR(100),
 email VARCHAR(150) UNIQUE NOT NULL,
 birth_year YEAR
);
CREATE TABLE community (
 community_id INT PRIMARY KEY AUTO_INCREMENT,
 name VARCHAR(100) NOT NULL,
 email VARCHAR(150) UNIQUE NOT NULL,
 city VARCHAR(100)
);
CREATE TABLE galleries (
 gallery_id INT PRIMARY KEY AUTO_INCREMENT,
 community_id INT UNIQUE NOT NULL,
 nom VARCHAR(100) NOT NULL,
 localisation VARCHAR(200),
 note DECIMAL(3,1) CHECK (note BETWEEN 0 AND 5)
);
CREATE TABLE artworks (
 artwork_id INT PRIMARY KEY AUTO_INCREMENT,
 artist_id INT NOT NULL,
 titre VARCHAR(200) NOT NULL,
 type VARCHAR(50),
 prix DECIMAL(10,2) CHECK (prix >= 0),
 statut ENUM('for_sale','sold','exposition')
 DEFAULT 'for_sale',
 FOREIGN KEY (artist_id) REFERENCES artists(artist_id)
 ON DELETE CASCADE
);
CREATE TABLE exhibitions (
 exhibition_id INT PRIMARY KEY AUTO_INCREMENT,
 gallery_id INT NOT NULL,
 titre VARCHAR(200) NOT NULL,
 date_debut DATE,
 theme VARCHAR(100),
 FOREIGN KEY (gallery_id) REFERENCES galleries(gallery_id)
 ON DELETE CASCADE
);
CREATE TABLE workshops (
 workshop_id INT PRIMARY KEY AUTO_INCREMENT,
 artist_id INT NOT NULL,
 titre VARCHAR(200) NOT NULL,
 date DATE,
 prix DECIMAL(8,2) CHECK (prix >= 0),
 niveau ENUM('debutant','intermediaire','avance'),
 FOREIGN KEY (artist_id) REFERENCES artists(artist_id)
 ON DELETE CASCADE
);
ALTER TABLE artworks
ADD COLUMN gallery_id INT,
ADD CONSTRAINT fk_artworks_gallery
FOREIGN KEY (gallery_id)
REFERENCES galleries(gallery_id)
ON DELETE SET NULL;
   
