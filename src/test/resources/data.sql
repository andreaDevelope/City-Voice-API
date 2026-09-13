-- Seed per i test di integrazione: categorie e badge, allineati al DB di sviluppo.
-- Eseguito da Spring dopo schema.sql (spring.sql.init.mode=always).

INSERT INTO categories (id, name, color) VALUES
  (1, 'activity', '#5b9bf5'),
  (2, 'neighborhood', '#f5a45b'),
  (3, 'continuity', '#e06850'),
  (4, 'impact', '#5bf5a4');

INSERT INTO badges (id, category_id, name, description, mission_threshold, sequence_order) VALUES
  (1, 1, 'Mo Chi Sei?', 'Ti sei registrato, damose ''na possibilità', 0, 1),
  (2, 1, 'Er Primo Cinguettio', 'Prima segnalazione, hai rotto il silenzio', 1, 2),
  (3, 1, 'Pettegola Del Quartiere', '3 segnalazioni, ormai sai tutto de tutti', 3, 3),
  (4, 1, 'Vedetta', '7 segnalazioni, non te sfugge niente', 7, 4),
  (5, 1, 'Grillo Parlante Der Rione', '15 segnalazioni, mo si che inizi a fare la differenza', 15, 5),
  (6, 1, 'Voce del Popolo, Voce di Dio', '30 segnalazioni', 30, 6),
  (7, 2, 'Pure Qui?', 'Hai segnalazioni attive in 2 quartieri diversi', 2, 1),
  (8, 2, 'Nce L''hai ''Na Casa?', '4 quartieri diversi, ma quanto giri', 4, 2),
  (9, 2, 'Sai Tutti I Bar', '6 quartieri diversi, mo conosci Roma bar per bar', 6, 3),
  (10, 2, 'GRAnde Esploratore', '8 quartieri diversi, pe'' te Roma è tutta casa tua', 8, 4),
  (11, 2, 'Ambasciatore De Quartiere', '10 quartieri diversi, te chiamano da tutte le zone', 10, 5),
  (12, 2, 'Chi Conosce Roma Come Te?', '12 quartieri diversi, sei una guida turistica ambulante', 12, 6),
  (13, 3, 'Giorno 1', 'Sei passato oggi, si parte', 1, 1),
  (14, 3, 'Giorno 2', '2 giorni consecutivi', 2, 2),
  (15, 3, 'Giorno 3', '3 giorni consecutivi', 3, 3),
  (16, 3, 'Giorno 4', '4 giorni consecutivi', 4, 4),
  (17, 3, 'Giorno 5', '5 giorni consecutivi', 5, 5),
  (18, 3, 'Giorno 6', '6 giorni consecutivi', 6, 6),
  (19, 3, 'Giorno 7', 'Settimana completa, non ti sei perso un giorno', 7, 7),
  (20, 4, 'Sono Un Fantasma', 'Nessuno sa di te', 3, 1),
  (21, 4, 'Ok', 'Qualcuno ha notato quello che dici', 10, 2),
  (22, 4, 'Te Fai Sentì', 'La gente comincia a reagì a quello che scrivi', 25, 3),
  (23, 4, 'Sai Convince Le Persone', 'Le tue parole cambiano idea alla gente', 50, 4),
  (24, 4, 'Te Sei Creato ''Na Reputazione', 'Ormai quello che dici conta', 100, 5),
  (25, 4, 'Quando Parli Te, Parla Roma', 'Sei diventato la voce che tutti ascoltano', 150, 6);

-- Le tabelle usano GenerationType.IDENTITY: allineo le sequenze agli id inseriti a mano.
SELECT setval(pg_get_serial_sequence('categories', 'id'), (SELECT MAX(id) FROM categories));
SELECT setval(pg_get_serial_sequence('badges', 'id'), (SELECT MAX(id) FROM badges));
