CREATE TABLE profielen (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ontvanger_id VARCHAR(20) NOT NULL,
    ontvanger_id_type VARCHAR(4) NOT NULL,
    email_notificaties BOOLEAN NOT NULL DEFAULT false,
    sms_notificaties BOOLEAN NOT NULL DEFAULT false,
    email_adres VARCHAR(254),
    telefoonnummer VARCHAR(20),
    frequentie VARCHAR(10) NOT NULL DEFAULT 'DIRECT',
    aangemaakt_op TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    bijgewerkt_op TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_profielen_ontvanger ON profielen(ontvanger_id, ontvanger_id_type);
