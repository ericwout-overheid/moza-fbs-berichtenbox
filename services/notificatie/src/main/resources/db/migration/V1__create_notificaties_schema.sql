CREATE TABLE notificaties (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ontvanger_id_type VARCHAR(4) NOT NULL,
    ontvanger_id VARCHAR(20) NOT NULL,
    kanaal VARCHAR(5) NOT NULL,
    onderwerp VARCHAR(200) NOT NULL,
    inhoud TEXT NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'AANGEMAAKT',
    aangemaakt_op TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    verzonden_op TIMESTAMP WITH TIME ZONE,
    afgeleverd_op TIMESTAMP WITH TIME ZONE,
    foutmelding TEXT
);

CREATE INDEX idx_notificaties_ontvanger ON notificaties(ontvanger_id_type, ontvanger_id);
CREATE INDEX idx_notificaties_status ON notificaties(status);
CREATE INDEX idx_notificaties_aangemaakt ON notificaties(aangemaakt_op DESC);
