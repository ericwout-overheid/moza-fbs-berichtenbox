CREATE TABLE bereikbaarheid (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ontvanger_id VARCHAR(20) NOT NULL,
    ontvanger_id_type VARCHAR(10) NOT NULL,
    digitaal_bereikbaar BOOLEAN NOT NULL,
    registratie_datum TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    intrekkings_datum TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX idx_bereikbaarheid_ontvanger ON bereikbaarheid(ontvanger_id, ontvanger_id_type);
