ALTER TABLE notificaties ADD CONSTRAINT chk_notificaties_foutmelding
    CHECK ((status = 'MISLUKT' AND foutmelding IS NOT NULL)
        OR (status != 'MISLUKT' AND foutmelding IS NULL));
