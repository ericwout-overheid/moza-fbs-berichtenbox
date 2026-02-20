CREATE TABLE berichten (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    afzender_oin VARCHAR(20) NOT NULL,
    ontvanger_id_type VARCHAR(4) NOT NULL,
    ontvanger_id VARCHAR(20) NOT NULL,
    onderwerp VARCHAR(500) NOT NULL,
    inhoud TEXT NOT NULL,
    status VARCHAR(15) NOT NULL DEFAULT 'NIEUW',
    aangemaakt_op TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    gelezen_op TIMESTAMP WITH TIME ZONE
);

CREATE TABLE bijlagen (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bericht_id UUID NOT NULL REFERENCES berichten(id) ON DELETE CASCADE,
    bestandsnaam VARCHAR(255) NOT NULL,
    media_type VARCHAR(255) NOT NULL,
    grootte BIGINT NOT NULL,
    object_key VARCHAR(512) NOT NULL,
    aangemaakt_op TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_berichten_ontvanger ON berichten(ontvanger_id_type, ontvanger_id);
CREATE INDEX idx_berichten_status ON berichten(status);
CREATE INDEX idx_berichten_aangemaakt ON berichten(aangemaakt_op DESC);
CREATE INDEX idx_bijlagen_bericht ON bijlagen(bericht_id);
