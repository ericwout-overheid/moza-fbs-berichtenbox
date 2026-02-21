ALTER TABLE profielen ADD CONSTRAINT chk_profielen_email
    CHECK (email_notificaties = false OR (email_adres IS NOT NULL AND email_adres != ''));

ALTER TABLE profielen ADD CONSTRAINT chk_profielen_sms
    CHECK (sms_notificaties = false OR (telefoonnummer IS NOT NULL AND telefoonnummer != ''));
