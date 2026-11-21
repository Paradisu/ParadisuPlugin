import { defineExtension } from '@mcdevtools/cli/extension';
import { dim } from '@mcdevtools/cli/output';
import { log } from '@mcdevtools/cli/util';
import { registerContextVars } from './context.ts';
import { paradisuMcCommands } from './mc/commands.ts';
import { loadFixtures } from './mc/fixtures.ts';
import { resolvePlayer } from './mc/players.ts';
import { registerShareHooks } from './share.ts';

/**
 * Everything specific to ParadisuPlugin: the preset configs' template variables, the `vparadisu reload` after share
 * tunnels change, and the commands that read the Paradisu database schema (`mc players`, `mc inventory`, `mc dump`,
 * `mc columns`, `db fixtures`) together with the fixtures loaded after `mcdev db reset`.
 */
export default defineExtension({
  name: 'paradisu',
  setup(api) {
    registerContextVars(api);
    registerShareHooks(api);
    for (const command of paradisuMcCommands()) api.registerCommand(command);
    api.registerPlayerResolver(resolvePlayer);
    api.on('afterDbReset', async (config, through) => {
      const loaded = await loadFixtures({ config, through, replace: false });
      if (loaded === 0) log(dim('  fixtures  none loaded'));
    });
  },
});
