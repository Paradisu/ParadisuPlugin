import { queryJson } from '../db.ts';
import { interactive, pickFrom, searchFrom } from '../prompt.ts';
import { DevError, fail } from '../util.ts';
import { looksLikeUuid, offlineUuid, parseUuid } from './uuid.ts';

export interface PlayerRow {
  uuid: string;
  display_name: string | null;
  online: boolean | null;
  last_server: string | null;
  playtime: number | null;
  inventories: number;
}

const SELECT = `
  select p.uuid::text as uuid, p.display_name, p.online, p.last_server, p.playtime,
         (select count(*) from player_inventories i where i.id_player = p.uuid)::int as inventories
  from players p`;

/** Players whose name starts with (or whose uuid equals) the search; no search lists the first 200. */
export function listPlayers(search = '', onlineOnly = false): PlayerRow[] {
  return queryJson<PlayerRow>(
    `${SELECT}
     where (:'q' = '' or p.display_name ilike :'q' || '%' or p.uuid::text = :'q')
       and (not :online::boolean or p.online)
     order by lower(p.display_name), p.uuid
     limit 200`,
    { q: search, online: String(onlineOnly) },
  );
}

export interface ResolvedPlayer {
  uuid: string;
  name: string;
  /** Not in the database (only possible for `offline:Name`) */
  missing: boolean;
}

function byUuid(uuid: string): PlayerRow | undefined {
  return queryJson<PlayerRow>(`${SELECT} where p.uuid::text = :'u'`, { u: uuid })[0];
}

/**
 * Resolves a name (case-insensitive exact, then prefix), a UUID, or `offline:Name` (the UUID a server in offline
 * mode assigns). Without input it prompts on a terminal.
 */
export async function resolvePlayer(input?: string): Promise<ResolvedPlayer> {
  if (!input) {
    if (!interactive()) {
      fail('A player name or UUID is required');
    }
    const uuid = await searchFrom('Player', (term) =>
      listPlayers(term).map((p) => ({
        name: `${p.display_name ?? '(unnamed)'}  ${p.uuid}  ${p.inventories} inv`,
        value: p.uuid,
      })),
    );
    return resolvePlayer(uuid);
  }
  if (input.toLowerCase().startsWith('offline:')) {
    const name = input.slice('offline:'.length);
    const uuid = offlineUuid(name);
    const row = byUuid(uuid);
    return { uuid, name: row?.display_name ?? name, missing: !row };
  }
  if (looksLikeUuid(input)) {
    const uuid = parseUuid(input);
    const row = byUuid(uuid);
    return { uuid, name: row?.display_name ?? uuid, missing: !row };
  }
  const exact = queryJson<PlayerRow>(`${SELECT} where lower(p.display_name) = lower(:'n')`, { n: input });
  const matches = exact.length > 0 ? exact : listPlayers(input);
  if (matches.length === 0) {
    throw new DevError(`No player "${input}" in the dev database (use offline:${input} for the offline-mode UUID)`);
  }
  if (matches.length === 1) {
    return { uuid: matches[0]!.uuid, name: matches[0]!.display_name ?? matches[0]!.uuid, missing: false };
  }
  if (!interactive()) {
    const sample = matches.slice(0, 10).map((p) => `  ${p.display_name} ${p.uuid}`).join('\n');
    throw new DevError(`"${input}" matches ${matches.length} players; be more specific or use the UUID:\n${sample}`);
  }
  const uuid = await pickFrom(
    `Several players match "${input}"`,
    matches.map((p) => ({ name: `${p.display_name ?? '(unnamed)'}  ${p.uuid}`, value: p.uuid })),
  );
  return resolvePlayer(uuid);
}
