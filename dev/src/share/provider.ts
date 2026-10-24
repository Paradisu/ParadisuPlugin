/**
 * The contract between `dev share` and the services that provide tunnels.
 *
 * To add a provider (ngrok, Pangolin, ...): implement `ProviderFactory` in `providers/<name>.ts` (import the
 * service's SDK lazily inside `create`, so it is only loaded when selected, and make it an optional dependency),
 * register it in `providers/index.ts`, and document its options under `[share.providers.<name>]` in dev.toml.
 * Nothing else in `dev` knows about a specific service. For ngrok, `open` would be roughly:
 *
 *   const listener = await ngrok.forward({ addr: port, proto: 'tcp', authtoken });
 *   const url = listener.url(); // tcp://0.tcp.ngrok.io:12345
 */

export type TunnelProtocol = 'tcp' | 'http';

export type ProxyProtocol = 'v1' | 'v2';

export interface TunnelRequest {
  /** Target name, used in logs: proxy | limbo | web */
  name: string;
  protocol: TunnelProtocol;
  localHost: string;
  localPort: number;
  /** Ask the provider to prefix each connection with a PROXY protocol header carrying the real client address (tcp only) */
  proxyProtocol?: ProxyProtocol;
}

export interface TunnelAddress {
  /** What to show users: tcp is "host:port", http is "https://host" */
  display: string;
  host: string;
  port: number;
  /** http only: the https base URL without a trailing slash */
  url?: string;
}

export interface TunnelEvents {
  /** The tunnel ended without stop() being called (expired, dropped, server error) */
  onClosed(reason: string): void;
}

export interface TunnelHandle {
  address: TunnelAddress;
  /** Epoch ms when the provider will end this tunnel; undefined = no known limit */
  expiresAt?: number;
  stop(): Promise<void>;
}

export interface ProviderCapabilities {
  protocols: TunnelProtocol[];
  /** Whether tcp tunnels can send a PROXY protocol header */
  proxyProtocol?: boolean;
  /** Tunnels this provider/account allows at once from this machine; undefined = no known limit */
  maxConcurrent?: number;
  /** false when every new tunnel gets a new random address (free tiers) */
  stableAddresses: boolean;
}

export interface TunnelProvider {
  name: string;
  capabilities: ProviderCapabilities;
  open(request: TunnelRequest, events: TunnelEvents): Promise<TunnelHandle>;
}

export interface ProviderFactory {
  name: string;
  /** One line for `dev share providers` */
  summary: string;
  /** `options` is the [share.providers.<name>] table; validate it here and fail() with a clear message */
  create(options: Record<string, unknown>): Promise<TunnelProvider>;
}
