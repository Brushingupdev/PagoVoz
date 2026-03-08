-- Script de configuraciÃ³n de Supabase para PagoVoz
-- Ejecutar en el SQL Editor de Supabase

-- ============================================
-- TABLA: licenses
-- ============================================
CREATE TABLE IF NOT EXISTS licenses (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    code TEXT UNIQUE NOT NULL,
    active BOOLEAN DEFAULT true,
    used BOOLEAN DEFAULT false,
    device_id TEXT,
    user_id TEXT,
    is_premium BOOLEAN DEFAULT false,
    premium_until TIMESTAMP WITH TIME ZONE,
    gives_trial BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Ãndices para optimizaciÃ³n
CREATE INDEX IF NOT EXISTS idx_licenses_code ON licenses(code);
CREATE INDEX IF NOT EXISTS idx_licenses_device_id ON licenses(device_id);
CREATE INDEX IF NOT EXISTS idx_licenses_active ON licenses(active);
CREATE INDEX IF NOT EXISTS idx_licenses_is_premium ON licenses(is_premium);

-- ============================================
-- TABLA: app_config
-- ============================================
CREATE TABLE IF NOT EXISTS app_config (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    latest_version_code INTEGER NOT NULL,
    latest_version_name TEXT NOT NULL,
    download_url TEXT NOT NULL,
    force_update BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- DATOS INICIALES
-- ============================================

-- Insertar configuraciÃ³n de la app
INSERT INTO app_config (latest_version_code, latest_version_name, download_url, force_update)
VALUES (1, '1.0', 'https://example.com/download', false)
ON CONFLICT DO NOTHING;

-- Insertar cÃ³digos de prueba (cambiar antes de producciÃ³n)
INSERT INTO licenses (code, gives_trial, is_premium, premium_until) VALUES
('TEST123', true, true, NOW() + INTERVAL '7 days'),
('DEMO456', true, true, NOW() + INTERVAL '7 days'),
('PREMIUM789', false, true, NOW() + INTERVAL '365 days')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================

-- Habilitar RLS en ambas tablas
ALTER TABLE licenses ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_config ENABLE ROW LEVEL SECURITY;

-- PolÃ­ticas para licenses
DROP POLICY IF EXISTS "Allow public read access for license validation" ON licenses;
CREATE POLICY "Allow public read access for license validation"
ON licenses FOR SELECT USING (true);

DROP POLICY IF EXISTS "Allow authenticated updates" ON licenses;
CREATE POLICY "Allow authenticated updates"
ON licenses FOR UPDATE USING (auth.role() = 'authenticated');

DROP POLICY IF EXISTS "Allow authenticated inserts" ON licenses;
CREATE POLICY "Allow authenticated inserts"
ON licenses FOR INSERT WITH CHECK (auth.role() = 'authenticated');

-- PolÃ­ticas para app_config (solo lectura pÃºblica)
DROP POLICY IF EXISTS "Allow public read access to app config" ON app_config;
CREATE POLICY "Allow public read access to app config"
ON app_config FOR SELECT USING (true);

DROP POLICY IF EXISTS "Allow authenticated updates to app config" ON app_config;
CREATE POLICY "Allow authenticated updates to app config"
ON app_config FOR UPDATE USING (auth.role() = 'authenticated');

-- ============================================
-- FUNCIONES ÃšTILES
-- ============================================

-- FunciÃ³n para validar un cÃ³digo de licencia
CREATE OR REPLACE FUNCTION validate_license(
    p_code TEXT,
    p_device_id TEXT
)
RETURNS TABLE (
    is_valid BOOLEAN,
    is_premium BOOLEAN,
    premium_until TIMESTAMP WITH TIME ZONE,
    gives_trial BOOLEAN
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        l.active AND (NOT l.used OR l.device_id = p_device_id) AS is_valid,
        l.is_premium,
        l.premium_until,
        l.gives_trial
    FROM licenses l
    WHERE l.code = p_code;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- FunciÃ³n para activar una licencia
CREATE OR REPLACE FUNCTION activate_license(
    p_code TEXT,
    p_device_id TEXT
)
RETURNS BOOLEAN AS $$
DECLARE
    v_license_exists BOOLEAN;
    v_gives_trial BOOLEAN;
BEGIN
    -- Verificar si la licencia existe y estÃ¡ activa
    SELECT EXISTS (
        SELECT 1 FROM licenses 
        WHERE code = p_code 
        AND active = true
        AND (used = false OR device_id = p_device_id)
    ) INTO v_license_exists;
    
    IF NOT v_license_exists THEN
        RETURN false;
    END IF;
    
    -- Obtener si da trial
    SELECT gives_trial INTO v_gives_trial 
    FROM licenses WHERE code = p_code;
    
    -- Actualizar la licencia
    UPDATE licenses 
    SET 
        used = true,
        device_id = p_device_id,
        is_premium = CASE WHEN v_gives_trial THEN true ELSE is_premium END,
        premium_until = CASE 
            WHEN v_gives_trial THEN NOW() + INTERVAL '7 days'
            ELSE premium_until 
        END,
        updated_at = NOW()
    WHERE code = p_code;
    
    RETURN true;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================
-- TRIGGERS
-- ============================================

-- Trigger para actualizar updated_at automÃ¡ticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_licenses_updated_at
BEFORE UPDATE ON licenses
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_app_config_updated_at
BEFORE UPDATE ON app_config
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- VERIFICACIÃ“N
-- ============================================

-- Verificar que las tablas se crearon
SELECT 
    'licenses' as table_name,
    COUNT(*) as row_count
FROM licenses
UNION ALL
SELECT 
    'app_config' as table_name,
    COUNT(*) as row_count
FROM app_config;

-- Verificar polÃ­ticas RLS
SELECT 
    schemaname,
    tablename,
    policyname,
    permissive,
    roles,
    cmd,
    qual
FROM pg_policies
WHERE schemaname = 'public'
ORDER BY tablename, policyname;

-- ============================================
-- INSTRUCCIONES DE USO
-- ============================================

/*
INSTRUCCIONES:

1. Ejecutar este script completo en el SQL Editor de Supabase
2. Verificar que las tablas se crearon correctamente
3. Probar las funciones:

   -- Validar un cÃ³digo
   SELECT * FROM validate_license('TEST123', 'device_test');
   
   -- Activar una licencia
   SELECT activate_license('TEST123', 'device_test');
   
   -- Ver licencias activas
   SELECT * FROM licenses WHERE used = true;

4. Para producciÃ³n, eliminar los cÃ³digos de prueba y crear los reales:
   
   DELETE FROM licenses WHERE code IN ('TEST123', 'DEMO456', 'PREMIUM789');
   
   INSERT INTO licenses (code, gives_trial) VALUES
   ('CODIGO_REAL_1', true),
   ('CODIGO_REAL_2', true);

5. Configurar la URL de descarga real en app_config:
   
   UPDATE app_config SET 
   download_url = 'https://tudominio.com/app.apk',
   latest_version_code = 1,
   latest_version_name = '1.0';
*/

-- ============================================
-- MANTENIMIENTO
-- ============================================

-- Consulta para limpiar licencias expiradas (ejecutar periÃ³dicamente)
CREATE OR REPLACE VIEW expired_licenses AS
SELECT * FROM licenses 
WHERE premium_until < NOW() 
AND is_premium = true;

-- Consulta para estadÃ­sticas
CREATE OR REPLACE VIEW license_stats AS
SELECT 
    COUNT(*) as total_licenses,
    COUNT(CASE WHEN used = true THEN 1 END) as used_licenses,
    COUNT(CASE WHEN is_premium = true THEN 1 END) as premium_licenses,
    COUNT(CASE WHEN gives_trial = true THEN 1 END) as trial_licenses,
    COUNT(CASE WHEN device_id IS NOT NULL THEN 1 END) as activated_devices
FROM licenses;
