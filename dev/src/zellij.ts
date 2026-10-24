import { spawnSync } from 'node:child_process';
import { cpSync, existsSync, mkdirSync, readFileSync, readdirSync, writeFileSync } from 'node:fs';
import { isIP } from 'node:net';
import { join } from 'node:path';
import type { Command } from './command.ts';
import type { DevConfig } from './config.ts';
import { askOptional, interactive, pickFrom } from './prompt.ts';
import { type Instance, instancesOf } from './templates.ts';
import { DEV, ROOT, RUN, capture, fail, log, render, run, warn } from './util.ts';

const DIR = join(RUN, '.zellij');
const LAYOUT_DIR = join(DIR, 'layouts');
const TLS_DIR = join(DIR, 'tls');
const CONFIG_FILE = join(DIR, 'config.kdl');
const DEV_BIN = join(DEV, 'dev');

function requireZellij(): void {
  if (capture('zellij', ['--version']).status !== 0) {
    fail('zellij is required (it is installed in the devcontainer image).');
  }
}

/** zellij on this machine, always with the generated config (outside zellij the user's own config is left alone). */
function zellij(config: DevConfig, args: string[]) {
  return capture('zellij', ['--config', CONFIG_FILE, ...args], { unsetEnv: ['ZELLIJ', 'ZELLIJ_SESSION_NAME'] });
}

function kdl(value: string): string {
  return `"${value.replaceAll('\\', '\\\\').replaceAll('"', '\\"')}"`;
}

function webTls(config: DevConfig): string {
  if (config.web.bind === '127.0.0.1') {
    return '';
  }
  return `web_server_cert ${kdl(join(TLS_DIR, 'cert.pem'))}\nweb_server_key ${kdl(join(TLS_DIR, 'key.pem'))}`;
}

function writeConfig(config: DevConfig): void {
  mkdirSync(LAYOUT_DIR, { recursive: true });
  const template = readFileSync(join(DEV, 'zellij', 'config.kdl'), 'utf8');
  writeFileSync(
    CONFIG_FILE,
    render(template, {
      DEFAULT_LAYOUT: config.ui.default_layout,
      LAYOUT_DIR,
      WEB_IP: config.web.bind,
      WEB_PORT: String(config.web.port),
      WEB_TLS: webTls(config),
    }),
  );
}

const CHROME = `    default_tab_template {
        pane size=1 borderless=true {
            plugin location="zellij:tab-bar"
        }
        children
        pane size=2 borderless=true {
            plugin location="zellij:status-bar"
        }
    }`;

function consolePane(instance: Instance): string {
  return `pane name=${kdl(instance.name)} command=${kdl(DEV_BIN)} {
    args "console" ${kdl(instance.name)} "--wait"
}`;
}

function dbPane(): string {
  return `pane name="psql" command=${kdl(DEV_BIN)} {
    args "db" "psql"
}`;
}

function shellTab(): string {
  return `    tab name="shell" {
        pane cwd=${kdl(ROOT)}
    }`;
}

/** Proxy and Limbo stacked in the first column, then one column per enabled backend. */
function networkLayout(instances: Instance[]): string {
  const infra = instances.filter((i) => i.platform !== 'paper');
  const backends = instances.filter((i) => i.platform === 'paper');
  const columns: string[] = [];
  if (infra.length > 0) {
    columns.push(`pane split_direction="horizontal" {\n${infra.map(consolePane).join('\n')}\n}`);
  }
  columns.push(...backends.map(consolePane));
  return `layout {
${CHROME}
    tab name="consoles" focus=true {
        pane split_direction="vertical" {
${columns.join('\n')}
        }
    }
    tab name="db" {
${dbPane()}
    }
${shellTab()}
}
`;
}

function tabsLayout(instances: Instance[]): string {
  const tabs = instances.map((i) => `    tab name=${kdl(i.name)} {\n${consolePane(i)}\n    }`);
  return `layout {
${CHROME}
${tabs.join('\n')}
    tab name="db" {
${dbPane()}
    }
${shellTab()}
}
`;
}

function writeLayouts(config: DevConfig): string[] {
  mkdirSync(LAYOUT_DIR, { recursive: true });
  const running = instancesOf(config).filter((i) => i.enabled);
  writeFileSync(join(LAYOUT_DIR, 'network.kdl'), networkLayout(running));
  writeFileSync(join(LAYOUT_DIR, 'tabs.kdl'), tabsLayout(running));
  // User layouts: committed examples first, local ones override them
  for (const source of [join(DEV, 'layouts'), join(DEV, 'layouts.local')]) {
    if (existsSync(source)) {
      cpSync(source, LAYOUT_DIR, { recursive: true });
    }
  }
  return readdirSync(LAYOUT_DIR)
    .filter((file) => file.endsWith('.kdl'))
    .map((file) => file.slice(0, -'.kdl'.length));
}

function sessionExists(config: DevConfig, name: string): boolean {
  return zellij(config, ['list-sessions', '--short', '--no-formatting']).stdout.split('\n').includes(name);
}

function sessionName(config: DevConfig, layout: string): string {
  return layout === config.ui.default_layout ? config.ui.session : `${config.ui.session}-${layout}`;
}

export interface UiOptions {
  /** Create the session detached instead of attaching (default layout only) */
  background: boolean;
  /** Prompt for a layout when none is given (default true) */
  ask?: boolean;
}

/** Opens (or creates) the zellij workspace of consoles. */
export async function ui(config: DevConfig, layout: string | undefined, options: UiOptions): Promise<void> {
  requireZellij();
  writeConfig(config);
  const layouts = writeLayouts(config);
  if (!layout) {
    // Only an explicit `dev ui` asks; background sessions (dev web start) and `dev start --ui` use the default
    layout = !options.background && options.ask !== false && layouts.length > 1 && interactive() ? await pickFrom('Open which layout?', layouts.map((l) => ({ name: l, value: l }))) : config.ui.default_layout;
  }
  if (!layouts.includes(layout)) {
    fail(`Unknown layout "${layout}". Available: ${layouts.join(', ')}`);
  }
  const file = join(LAYOUT_DIR, `${layout}.kdl`);
  const name = sessionName(config, layout);

  if (process.env.ZELLIJ) {
    // Already inside zellij: add the layout as a tab instead of nesting
    run('zellij', ['action', 'new-tab', '--layout', file]);
    return;
  }
  if (options.background) {
    if (layout !== config.ui.default_layout) {
      fail('--background only supports the default layout (ui.default_layout in dev.toml)');
    }
    if (!sessionExists(config, name)) {
      const result = zellij(config, ['attach', '--create-background', name]);
      if (result.status !== 0) {
        fail(`Could not create the zellij session: ${result.stderr.trim()}`);
      }
    }
    return;
  }
  const env = { unsetEnv: ['ZELLIJ', 'ZELLIJ_SESSION_NAME'] };
  if (sessionExists(config, name)) {
    run('zellij', ['--config', CONFIG_FILE, 'attach', name], env);
  } else {
    // --layout together with --session would add the layout to an existing session; -n always starts a new one
    run('zellij', ['--config', CONFIG_FILE, '--session', name, '--new-session-with-layout', file], env);
  }
}

export function killUi(config: DevConfig, layout: string | undefined): void {
  requireZellij();
  writeConfig(config);
  const name = sessionName(config, layout ?? config.ui.default_layout);
  if (!sessionExists(config, name)) {
    log(`No zellij session "${name}"`);
    return;
  }
  zellij(config, ['kill-session', name]);
  zellij(config, ['delete-session', name]);
  log(`Closed ${name} (servers keep running)`);
}

// ---------------------------------------------------------------------------------------------------------------
// Web client
// ---------------------------------------------------------------------------------------------------------------

function webUrl(config: DevConfig): string {
  const scheme = config.web.bind === '127.0.0.1' ? 'http' : 'https';
  const host = config.web.bind === '127.0.0.1' ? 'localhost' : config.public_host;
  return `${scheme}://${host}:${config.web.port}/${config.ui.session}`;
}

function ensureCertificate(config: DevConfig): void {
  if (config.web.bind === '127.0.0.1' || existsSync(join(TLS_DIR, 'cert.pem'))) {
    return;
  }
  mkdirSync(TLS_DIR, { recursive: true });
  const names = ['DNS:localhost', 'IP:127.0.0.1'];
  names.push(`${isIP(config.public_host) ? 'IP' : 'DNS'}:${config.public_host}`);
  run('openssl', [
    'req', '-x509', '-newkey', 'rsa:2048', '-nodes', '-days', '825',
    '-subj', '/CN=paradisu-dev',
    '-addext', `subjectAltName=${[...new Set(names)].join(',')}`,
    '-keyout', join(TLS_DIR, 'key.pem'),
    '-out', join(TLS_DIR, 'cert.pem'),
  ]);
  log('Generated a self-signed certificate (browsers will warn once)');
}

function webArgs(config: DevConfig): string[] {
  const args = ['--ip', config.web.bind, '--port', String(config.web.port)];
  if (config.web.bind !== '127.0.0.1') {
    args.push('--cert', join(TLS_DIR, 'cert.pem'), '--key', join(TLS_DIR, 'key.pem'));
  }
  return args;
}

/** `zellij web --status` exits 0 even when the server is offline, so read its message. */
export function webRunning(config: DevConfig): boolean {
  const result = zellij(config, ['web', '--status', '--timeout', '2', ...webArgs(config)]);
  return result.status === 0 && !/offline/i.test(result.stdout + result.stderr);
}

function createToken(config: DevConfig, readOnly: boolean): void {
  const result = zellij(config, ['web', readOnly ? '--create-read-only-token' : '--create-token']);
  if (result.status !== 0) {
    fail(`Could not create a token: ${result.stderr.trim()}`);
  }
  log(result.stdout.trim());
  log('This token is shown only once; create another with `dev web token` if you lose it.');
}

export async function webStart(config: DevConfig): Promise<void> {
  requireZellij();
  writeConfig(config);
  writeLayouts(config);
  ensureCertificate(config);
  if (webRunning(config)) {
    log('The web server is already running');
  } else {
    const started = spawnSync('zellij', ['--config', CONFIG_FILE, 'web', '--daemonize', ...webArgs(config)], {
      stdio: 'inherit',
      env: { ...process.env, ZELLIJ: undefined },
    });
    if (started.status !== 0) {
      fail('Could not start the zellij web server');
    }
  }
  await ui(config, undefined, { background: true });
  log(`Web console: ${webUrl(config)}`);
  const tokens = zellij(config, ['web', '--list-tokens']).stdout.trim();
  if (!tokens || /no tokens/i.test(tokens)) {
    createToken(config, false);
  } else {
    log('Log in with an existing token, or create one with `dev web token`.');
  }
  if (config.web.bind !== '127.0.0.1') {
    warn('The web client is a shell in this container: anyone with a token has full access. Prefer `dev web token --read-only` for observers.');
  }
}

const WEB_COMMANDS: Command[] = [
  { name: 'web start', usage: '', summary: 'Serve the consoles to a browser (zellij web client)', run: ({ config }) => webStart(config) },
  {
    name: 'web stop',
    usage: '',
    summary: 'Stop the zellij web server',
    run: ({ config }) => {
      requireZellij();
      writeConfig(config);
      run('zellij', ['--config', CONFIG_FILE, 'web', '--stop']);
    },
  },
  {
    name: 'web status',
    usage: '',
    summary: 'Show whether the web server is running and its URL',
    run: ({ config }) => {
      requireZellij();
      writeConfig(config);
      log(webRunning(config) ? `running: ${webUrl(config)}` : 'not running');
    },
  },
  {
    name: 'web token',
    usage: '[--read-only]',
    summary: 'Create a login token (shown once)',
    run: ({ config }, args) => {
      requireZellij();
      writeConfig(config);
      createToken(config, args.includes('--read-only'));
    },
  },
];

export const COMMANDS: Command[] = [
  {
    name: 'ui',
    usage: '[layout|kill] [--background]',
    summary: 'Open the zellij workspace of server consoles (kill: close it; servers keep running)',
    run: async ({ config }, args) => {
      const background = args.includes('--background');
      const positionals = args.filter((arg) => !arg.startsWith('--'));
      if (positionals[0] === 'kill') {
        return killUi(config, positionals[1]);
      }
      return ui(config, positionals[0], { background });
    },
  },
  ...WEB_COMMANDS,
];

/** After `dev start`: offer to open the consoles (never prompts without a terminal). */
export async function offerUi(config: DevConfig, requested: boolean): Promise<void> {
  if (requested || (await askOptional('Open the consoles in zellij?', false))) {
    await ui(config, undefined, { background: false, ask: false });
  }
}

