import net from 'node:net';
import { DevError } from '../util.ts';

function connect(path: string, instance: string): Promise<net.Socket> {
  return new Promise((resolve, reject) => {
    const socket = net.createConnection({ path });
    socket.once('connect', () => resolve(socket));
    socket.once('error', (error: NodeJS.ErrnoException) => {
      reject(
        error.code === 'ENOENT' || error.code === 'ECONNREFUSED'
          ? new DevError(
              `The capture plugin is not running on ${instance} (no control socket). Is [capture] enabled in dev.toml, and was ${instance} started with \`dev start\` after that?`,
            )
          : error,
      );
    });
  });
}

/** One request line, one response line. */
export async function request(path: string, instance: string, body: Record<string, unknown>, timeoutMs = 8000): Promise<any> {
  const socket = await connect(path, instance);
  return new Promise((resolve, reject) => {
    let data = '';
    const timer = setTimeout(() => {
      socket.destroy();
      reject(new DevError(`${instance} did not answer the capture request in time`));
    }, timeoutMs);
    socket.setEncoding('utf8');
    socket.on('data', (chunk: string) => {
      data += chunk;
      const newline = data.indexOf('\n');
      if (newline !== -1) {
        clearTimeout(timer);
        socket.end();
        try {
          const response = JSON.parse(data.slice(0, newline));
          if (response.ok === false) {
            reject(new DevError(`${instance}: ${response.error}`));
          } else {
            resolve(response);
          }
        } catch (error) {
          reject(error);
        }
      }
    });
    socket.on('error', reject);
    socket.write(`${JSON.stringify(body)}\n`);
  });
}

/** Streams the lines of a `watch` until the plugin ends it (capture stopped), the signal aborts, or the socket closes. */
export async function watch(
  path: string,
  instance: string,
  filter: Record<string, unknown>,
  onLine: (line: any) => void,
  signal?: AbortSignal,
): Promise<void> {
  const socket = await connect(path, instance);
  signal?.addEventListener('abort', () => socket.destroy(), { once: true });
  return new Promise((resolve, reject) => {
    let buffer = '';
    let acknowledged = false;
    socket.setEncoding('utf8');
    socket.on('data', (chunk: string) => {
      buffer += chunk;
      let newline: number;
      while ((newline = buffer.indexOf('\n')) !== -1) {
        const line = JSON.parse(buffer.slice(0, newline));
        buffer = buffer.slice(newline + 1);
        if (!acknowledged) {
          acknowledged = true;
          if (line.ok === false) {
            socket.destroy();
            reject(new DevError(`${instance}: ${line.error}`));
            return;
          }
          continue;
        }
        if (line.t !== 'ping') {
          onLine(line);
        }
      }
    });
    socket.on('close', () => resolve());
    socket.on('error', (error) => (signal?.aborted ? resolve() : reject(error)));
    socket.write(`${JSON.stringify({ cmd: 'watch', filter })}\n`);
  });
}
