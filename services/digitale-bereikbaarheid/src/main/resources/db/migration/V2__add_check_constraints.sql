ALTER TABLE bereikbaarheid ADD CONSTRAINT chk_bereikbaarheid_intrekking
    CHECK (digitaal_bereikbaar = true OR intrekkings_datum IS NOT NULL);
