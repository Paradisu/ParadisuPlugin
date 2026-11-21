-- Seed data for the schema as of changelog 001 (changesets 1-5). Deterministic: everything derives from
-- hashes of row numbers; only timestamps move, because they are relative to "now".
--
-- PLAYER_INVENTORIES stays empty on purpose: inventories are Paper item-stack blobs that only a running
-- server can produce. Join a dev server to create some.

BEGIN;

TRUNCATE PLAYERS, WARPS CASCADE;

-- Pseudo-random value in [0, 1) from a key and a salt
CREATE OR REPLACE FUNCTION pg_temp.h(key text, salt text) RETURNS double precision AS $$
    SELECT (('x' || substr(md5(key || ':' || salt), 1, 8))::bit(32)::bigint)::double precision / 4294967296.0
$$ LANGUAGE sql IMMUTABLE;

CREATE OR REPLACE FUNCTION pg_temp.utc_now() RETURNS timestamp AS $$
    SELECT now() AT TIME ZONE 'UTC'
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION pg_temp.pick(options text[], key text, salt text) RETURNS text AS $$
    SELECT options[1 + floor(pg_temp.h(key, salt) * array_length(options, 1))::int]
$$ LANGUAGE sql IMMUTABLE;

-- 500 players. Rows 1-3 are name edge cases (3 characters, 16 characters, underscore).
INSERT INTO PLAYERS (uuid, display_name, online, first_joined, last_joined, last_seen, last_server, playtime)
SELECT
    md5('player-' || n)::uuid,
    CASE n
        WHEN 1 THEN 'Abc'
        WHEN 2 THEN 'Max_Length_16_ch'
        WHEN 3 THEN 'under_score'
        ELSE pg_temp.pick(ARRAY['Swift','Brave','Calm','Dark','Epic','Fuzzy','Giant','Happy','Icy','Jolly','Lucky','Mighty','Noble','Quick','Royal','Sunny'], n::text, 'adj')
          || pg_temp.pick(ARRAY['Fox','Wolf','Bear','Hawk','Lynx','Moth','Newt','Owl','Pike','Seal','Toad','Wren','Yak','Crab','Deer','Frog'], n::text, 'noun')
          || n
    END,
    n IN (10, 20, 30, 40, 50),
    first_joined,
    last_joined,
    least(last_joined + (1 + floor(pg_temp.h(n::text, 'ls') * 600)) * interval '1 minute', pg_temp.utc_now()),
    pg_temp.pick(ARRAY['lobby','creative','survival',NULL], n::text, 'srv'),
    floor(power(pg_temp.h(n::text, 'pt'), 4) * 2000000)::bigint + 600
FROM generate_series(1, 500) AS n
CROSS JOIN LATERAL (SELECT pg_temp.utc_now() - (0.05 + 0.95 * pg_temp.h(n::text, 'fj')) * interval '730 days' AS first_joined) f
CROSS JOIN LATERAL (SELECT f.first_joined + (pg_temp.utc_now() - f.first_joined) * pg_temp.h(n::text, 'lj') AS last_joined) l;

-- Proxy sessions: 0-29 per player, skewed towards few
INSERT INTO PLAYER_PROXY_SESSIONS (id, id_player, joined_at, left_at)
SELECT
    md5('pps-' || p.uuid || '-' || s)::uuid,
    p.uuid,
    j.joined_at,
    least(j.joined_at + (1 + floor(pg_temp.h(p.uuid::text || s, 'dur') * 240)) * interval '1 minute', pg_temp.utc_now())
FROM PLAYERS p
CROSS JOIN LATERAL generate_series(1, floor(power(pg_temp.h(p.uuid::text, 'ns'), 2) * 30)::int) AS s
CROSS JOIN LATERAL (
    SELECT p.first_joined + (p.last_joined - p.first_joined) * pg_temp.h(p.uuid::text || s, 'js') AS joined_at
) j;

-- Open sessions: the latest session of every online player, plus a few stale ones from crashes
UPDATE PLAYER_PROXY_SESSIONS s SET left_at = NULL
WHERE s.joined_at = (SELECT max(joined_at) FROM PLAYER_PROXY_SESSIONS WHERE id_player = s.id_player)
  AND s.id_player IN (SELECT uuid FROM PLAYERS WHERE online OR display_name ~ '[0-9]*(7|9)7$');

-- Server sessions: 1-3 per proxy session, inside it
INSERT INTO PLAYER_SERVER_SESSIONS (id, id_player, server, joined_at, left_at)
SELECT
    md5('pss-' || ps.id || '-' || k)::uuid,
    ps.id_player,
    pg_temp.pick(ARRAY['lobby','creative','survival'], ps.id::text || k, 'srv'),
    ps.joined_at + (ps.left_at - ps.joined_at) * ((k - 1) / n.count::double precision),
    ps.joined_at + (ps.left_at - ps.joined_at) * (k / n.count::double precision)
FROM PLAYER_PROXY_SESSIONS ps
CROSS JOIN LATERAL (SELECT 1 + floor(pg_temp.h(ps.id::text, 'nk') * 3)::int AS count) n
CROSS JOIN LATERAL generate_series(1, n.count) AS k
WHERE ps.left_at IS NOT NULL;

-- Warps in three contexts. "events" has no dev server (exercises the no-server path), "Spawn" and
-- "Main Street" exist in several contexts, and the names exercise fuzzy matching (partial, squashed,
-- typo, one-letter-apart pairs). (name, context) stays unique because changelog 002 enforces it.
INSERT INTO WARPS (id, name, context, x, y, z, yaw, pitch, permission)
SELECT
    md5('warp-' || context || '-' || name)::uuid,
    name,
    context,
    round((pg_temp.h(context || name, 'x') * 400 - 200)::numeric, 1),
    56,
    round((pg_temp.h(context || name, 'z') * 400 - 200)::numeric, 1),
    round((pg_temp.h(context || name, 'yaw') * 360 - 180)::numeric, 1),
    round((pg_temp.h(context || name, 'pitch') * 30 - 10)::numeric, 1),
    CASE
        WHEN name = 'Staff Plaza' THEN 'paradisu.warp.staff'
        WHEN pg_temp.h(context || name, 'perm') < 0.15 THEN 'paradisu.warp.vip'
    END
FROM (
    SELECT 'paradisu' AS context, unnest(ARRAY[
        'Spawn','Main Street','Main Street Station','Cinderella Castle','Castle Courtyard','Castle Gardens',
        'Adventure Land','Fantasy Land','Tomorrow Land','Frontier Town','Haunted Manor','Pirate Cove','Pirate Cave',
        'Rocket Port','Ice Palace','Rainbow Bridge','Market Square','Lighthouse','Harbor','Train Station',
        'Fireworks Plaza','Clock Tower','Carousel','Ferris Wheel','Grand Hotel','Town Hall','Library','Observatory',
        'Windmill','Secret Garden','Mystic Falls','Crystal Cave','Sunset Beach','Old Mill','Bakery']) AS name
    UNION ALL
    SELECT 'creative', unnest(ARRAY[
        'Spawn','Plot World','Redstone Lab','Build Arena','Gallery','Pixel Art Hall','Terraforming',
        'Skyblock Island','Parkour Course','Mega Build','Showcase','Tutorial','Contraption Lab','Main Street','Staff Plaza'])
    UNION ALL
    SELECT 'events', unnest(ARRAY[
        'Spawn','Halloween Hollow','Winter Wonderland','Summer Splash','Easter Egg Hunt','Spooky Maze',
        'Snowball Arena','Fireworks Show','Anniversary Hall','Treasure Hunt'])
) w;

COMMIT;
