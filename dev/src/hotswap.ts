import { readFile } from 'node:fs/promises';
import { join } from 'node:path';
import { parse } from 'smol-toml';
import type { Platform } from './config.ts';
import { ROOT } from './util.ts';

/**
 * Every plugin bundled in JetBrains Runtime's HotswapAgent (JBR 25). The agent only has a deny list
 * (`-Dhotswapagent.disablePlugin`, case-insensitive), so the plugins to keep are subtracted from this list.
 * A plugin missing here (a newer JBR) simply stays enabled.
 */
export const AGENT_PLUGINS = [
  'AnonymousClassPatch', 'ClassInitPlugin', 'CxfJAXRS', 'Deltaspike', 'DeltaspikeJakarta', 'ELResolver', 'FreeMarker',
  'GlassFish', 'Hibernate', 'Hibernate3', 'Hibernate3JPA', 'HibernateJakarta', 'Hotswapper', 'IBatis', 'Idea',
  'JBossModules', 'JacksonPlugin', 'JdkPlugin', 'Jersey1', 'Jersey2', 'Jetty', 'Log4j2', 'Logback', 'Mojarra', 'MyBatis',
  'MyBatisPlus', 'MyFaces', 'Omnifaces', 'OsgiEquinox', 'Owb', 'OwbJakarta', 'Proxy', 'ResteasyRegistry', 'Sponge',
  'Spring', 'SpringBoot', 'Thymeleaf', 'Tomcat', 'Undertow', 'Vaadin', 'Velocity', 'WatchResources', 'WebObjects',
  'Weblogic', 'Weld', 'WeldJakarta', 'Wicket', 'WildFlyELResolver', 'ZK',
];

/** JVM-level fixes that apply to any code: anonymous/lambda class naming after a redefine, and JDK cache clearing. */
const ALWAYS = ['AnonymousClassPatch', 'JdkPlugin'];

/** Plugins worth loading when the build depends on a Maven group (hibernate 7 is Jakarta, so not the javax plugin). */
const BY_GROUP: Record<string, string[]> = {
  'org.hibernate.orm': ['HibernateJakarta'],
};

/** Maven groups of every library in gradle/libs.versions.toml. */
export async function catalogGroups(): Promise<Set<string>> {
  const toml = parse(await readFile(join(ROOT, 'gradle', 'libs.versions.toml'), 'utf8')) as {
    libraries?: Record<string, string | { group?: string; module?: string }>;
  };
  const groups = new Set<string>();
  for (const library of Object.values(toml.libraries ?? {})) {
    const group = typeof library === 'string' ? library.split(':')[0] : (library.group ?? library.module?.split(':')[0]);
    if (group) {
      groups.add(group);
    }
  }
  return groups;
}

/**
 * JVM flags that enable HotswapAgent with only the plugins this project can use. Limbo runs none of our code,
 * so it gets no agent. `extraPlugins` are kept in addition (`hotswap_plugins` in dev.toml).
 */
export function hotswapFlags(platform: Platform, groups: Iterable<string>, extraPlugins: string[]): string[] {
  if (platform === 'limbo') {
    return [];
  }
  const keep = new Set([...ALWAYS, ...extraPlugins, ...[...groups].flatMap((group) => BY_GROUP[group] ?? [])].map((name) => name.toLowerCase()));
  const disabled = AGENT_PLUGINS.filter((name) => !keep.has(name.toLowerCase()));
  return ['-XX:+AllowEnhancedClassRedefinition', '-XX:HotswapAgent=fatjar', `-Dhotswapagent.disablePlugin=${disabled.join(',')}`];
}
