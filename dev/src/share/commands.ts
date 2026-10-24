import { spawn } from 'node:child_process';
import { existsSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';
import type { Command } from '../command.ts';
import type { DevConfig, ShareTarget } from '../config.ts';
import { isRunning, rerender, select } from '../instances.ts';
import { interactive, pickMany } from '../prompt.ts';
import { sendLine } from '../supervisor.ts';
import { ROOT, fail, log, run, waitFor, warn } from '../util.ts';
import { webRunning, webStart } from '../zellij.ts';
import { runDaemon } from './daemon.ts';
import { createProvider, providers } from './providers/index.ts';
import { LOG_FILE, clearState, daemonPid, readState, summaryLines } from './state.ts';
import { TARGETS, parseTargets } from './targets.ts';

const CLI_PATH = fileURLToPath(new URL('../cli.ts', import.meta.url));

async function chooseTargets(config: DevConfig, named: string[]): Promise<ShareTarget[]> {
  const requested =
    named.length > 0
      ? named
      : interactive()
        ? await pickMany(
            'Share which targets?',
            (Object.keys(TARGETS) as ShareTarget[]).map((name) => ({ name, value: name, checked: config.share.targets.includes(name) })),
          )
        : config.share.targets;
  const targets: ShareTarget[] = [];
  for (const name of parseTargets(requested)) {
    const reason = TARGETS[name].unavailable(config);
    if (reason) {
      warn(`Not sharing ${name}: ${reason}`);
    } else {
      targets.push(name);
    }
  }
  if (targets.length === 0) {
    fail('Nothing to share');
  }
  return targets;
}

function printSummary(config: DevConfig): void {
  const state = readState();
  if (!state) {
    return;
  }
  log(`Sharing through ${state.provider}`);
  const expiries: number[] = [];
  for (const [name, tunnel] of Object.entries(state.tunnels) as [ShareTarget, NonNullable<(typeof state.tunnels)[ShareTarget]>][]) {
    if (tunnel.address) {
      log(`  ${name.padEnd(6)} ${TARGETS[name].describe(tunnel.address, config)}`);
    } else {
      warn(`${name} is ${tunnel.status}${tunnel.error ? `: ${tunnel.error}` : ''} (see dev share logs)`);
    }
    if (tunnel.expiresAt) {
      expiries.push(Date.parse(tunnel.expiresAt));
    }
  }
  if (expiries.length > 0) {
    const next = new Date(Math.min(...expiries) - config.share.renew_before_seconds * 1000);
    log(
      `This provider limits tunnel lifetime: the next renewal is at ${next.toLocaleTimeString()}; addresses will change and connected players will be disconnected.`,
    );
  }
  if (state.tunnels.proxy?.proxyProtocol) {
    warn(
      `The proxy now expects the PROXY protocol (${state.tunnels.proxy.proxyProtocol}), so direct connections to port ${config.proxy.port} (localhost, LAN) are refused while sharing: join through the tunnel address, or set share.proxy_protocol = "off".`,
    );
  }
  if (state.tunnels.proxy && !state.tunnels.limbo && config.limbo.enabled) {
    warn('Limbo is not shared: remote players cannot reach it, so /vparadisu maintenance will lose them. Add the limbo target.');
  }
  if (state.tunnels.web) {
    warn('Anyone with a normal web token has a shell in this container; hand out read-only tokens (`dev web token --read-only`).');
  }
  if (!isRunning(select(config, ['proxy'])[0]!)) {
    log('The proxy is not running; it picks up the addresses on `dev start`.');
  }
  log('Follow: dev share logs -f    Stop: dev share stop');
}

async function start(config: DevConfig, args: string[]): Promise<void> {
  const { values, positionals } = parseArgs({
    args,
    options: { provider: { type: 'string' }, foreground: { type: 'boolean' } },
    allowPositionals: true,
  });
  if (daemonPid() !== undefined) {
    printSummary(config);
    fail('Already sharing; run `dev share stop` first');
  }
  const providerName = values.provider ?? config.share.provider;
  const targets = await chooseTargets(config, positionals);
  // Creating the provider validates its options and loads its SDK before anything is started
  const provider = await createProvider(config, providerName);
  const unsupported = targets.filter((name) => !provider.capabilities.protocols.includes(TARGETS[name].protocol));
  if (unsupported.length > 0) {
    fail(`${provider.name} cannot tunnel: ${unsupported.join(', ')}`);
  }
  const limit = provider.capabilities.maxConcurrent;
  if (limit !== undefined && targets.length > limit) {
    fail(
      `${provider.name} allows ${limit} tunnel${limit === 1 ? '' : 's'} at once on this account. Share fewer targets ` +
        `(for example \`dev share start proxy\`) or configure an account token under [share.providers.${provider.name}] in dev.local.toml.`,
    );
  }
  if (targets.includes('web') && !webRunning(config)) {
    await webStart(config);
  }
  if (values.foreground) {
    return runDaemon(config, targets, providerName, true);
  }
  const child = spawn(
    process.execPath,
    ['--disable-warning=ExperimentalWarning', CLI_PATH, '_share', providerName, ...targets],
    { detached: true, stdio: 'ignore', cwd: ROOT, env: process.env },
  );
  child.unref();
  let exited = false;
  child.once('exit', () => {
    exited = true;
  });
  const settled = () => {
    const state = readState();
    return exited || (state !== undefined && targets.every((name) => state.tunnels[name] && state.tunnels[name].status !== 'starting'));
  };
  if (!(await waitFor(settled, 60_000, 500)) || exited) {
    const tail = existsSync(LOG_FILE) ? readFileSync(LOG_FILE, 'utf8').split('\n').slice(-15).join('\n') : '(no log)';
    fail(`The share daemon did not start.\n${tail}`);
  }
  printSummary(config);
}

/** Stops the share daemon (if any) and restores the configured addresses. */
export async function stopShare(config: DevConfig): Promise<void> {
  const pid = daemonPid();
  if (pid === undefined) {
    log('Not sharing');
    return;
  }
  process.kill(pid, 'SIGTERM');
  if (await waitFor(() => daemonPid() === undefined, 15_000, 250)) {
    log('Stopped sharing');
    return;
  }
  warn('The share daemon did not stop in 15s; killing it');
  process.kill(pid, 'SIGKILL');
  clearState();
  // It never got to restore the proxy's configured addresses
  if (await rerender(config, 'proxy')) {
    const proxy = select(config, ['proxy'])[0]!;
    if (isRunning(proxy)) {
      await sendLine(proxy, 'vparadisu reload');
      await sendLine(proxy, 'velocity reload');
    }
  }
}

export const SHARE_COMMANDS: Command[] = [
  {
    name: '_share',
    usage: '<provider> <targets...>',
    summary: 'Internal: run the tunnel daemon',
    hidden: true,
    run: ({ config }, args) => runDaemon(config, parseTargets(args.slice(1)), args[0] ?? config.share.provider, false),
  },
  {
    name: 'share start',
    usage: '[targets...] [--provider <name>] [--foreground]',
    summary: 'Open public tunnels (targets: proxy, limbo, web) and keep them renewed',
    run: ({ config }, args) => start(config, args),
  },
  { name: 'share stop', usage: '', summary: 'Close the tunnels and restore the configured addresses', run: ({ config }) => stopShare(config) },
  { name: 'share status', usage: '', summary: 'Show the tunnels, their addresses and renewal times', run: () => summaryLines(readState()).forEach((line) => log(line)) },
  {
    name: 'share logs',
    usage: '[-f]',
    summary: 'Show the tunnel daemon log',
    run: (_ctx, args) => {
      if (!existsSync(LOG_FILE)) {
        fail('No share log yet');
      }
      run('tail', ['-n', '200', ...(args.includes('-f') ? ['-f'] : []), LOG_FILE]);
    },
  },
  {
    name: 'share providers',
    usage: '',
    summary: 'List the available tunnel providers',
    run: ({ config }) => {
      for (const provider of providers()) {
        log(`${provider.name === config.share.provider ? '*' : ' '} ${provider.name.padEnd(10)} ${provider.summary}`);
      }
    },
  },
];
