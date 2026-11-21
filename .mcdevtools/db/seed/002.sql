-- Seed data for the schema as of changelog 002: changeset 6 added the nullable "world" and "aliases"
-- columns to WARPS. About 80% of warps get values; the rest are deliberately left NULL so changelog 003
-- (which backfills them with defaultNullValue and then adds NOT NULL) runs against realistic data.

BEGIN;

CREATE OR REPLACE FUNCTION pg_temp.h(key text, salt text) RETURNS double precision AS $$
    SELECT (('x' || substr(md5(key || ':' || salt), 1, 8))::bit(32)::bigint)::double precision / 4294967296.0
$$ LANGUAGE sql IMMUTABLE;

WITH numbered AS (
    SELECT id, name, row_number() OVER (ORDER BY context, name) AS rn FROM WARPS
)
UPDATE WARPS w
SET world = CASE WHEN w.name = 'Winter Wonderland' THEN 'world_nether' ELSE 'world' END,
    aliases = (
        SELECT coalesce(array_agg(a ORDER BY k), ARRAY[]::varchar[])
        FROM (VALUES
            (1, lower(replace(w.name, ' ', ''))),
            (2, lower(regexp_replace(w.name, '(\w)\w*\s*', '\1', 'g'))),
            (3, lower(split_part(w.name, ' ', 1)))
        ) v(k, a)
        WHERE k <= floor(pg_temp.h(w.id::text, 'na') * 4)::int
    )
FROM numbered n
WHERE n.id = w.id
  AND (n.rn % 5 <> 0 OR w.name = 'Winter Wonderland');

COMMIT;
