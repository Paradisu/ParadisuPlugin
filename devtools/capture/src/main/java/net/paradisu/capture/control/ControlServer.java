/*
 * The official plugin for the Paradisu server. Copyright (C) 2026 Paradisu. https://paradisu.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.paradisu.capture.control;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.paradisu.capture.core.CaptureEngine;
import net.paradisu.capture.core.ConnectionInfo;
import net.paradisu.capture.core.LiveHub;
import net.paradisu.capture.core.PacketKey;
import net.paradisu.capture.core.Session;
import net.paradisu.capture.core.SessionSpec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JSON-lines control socket for the dev CLI: one request line, one response line; {@code watch} then streams. Only Gson
 * APIs that exist in old versions (Velocity bundles 2.8.0) are used here.
 */
public final class ControlServer implements AutoCloseable {
    private static final Gson GSON = new Gson();

    private final CaptureEngine engine;
    private final Path path;
    private ServerSocketChannel server;
    private Thread acceptor;
    private volatile boolean closed;

    public ControlServer(CaptureEngine engine, Path path) {
        this.engine = engine;
        this.path = path;
    }

    public void start() throws IOException {
        Files.deleteIfExists(path);
        server = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
        server.bind(UnixDomainSocketAddress.of(path));
        acceptor = new Thread(this::acceptLoop, "ParadisuCapture-control");
        acceptor.setDaemon(true);
        acceptor.start();
    }

    private void acceptLoop() {
        while (!closed) {
            try {
                SocketChannel client = server.accept();
                Thread.ofVirtual().name("ParadisuCapture-client").start(() -> serve(client));
            } catch (IOException e) {
                if (!closed) {
                    engine.log().warn("capture control socket failed: " + e);
                }
                return;
            }
        }
    }

    private void serve(SocketChannel client) {
        try (client;
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(Channels.newInputStream(client), StandardCharsets.UTF_8));
                OutputStream out = Channels.newOutputStream(client)) {
            String line = in.readLine();
            if (line == null) {
                return;
            }
            JsonObject request;
            try {
                request = GSON.fromJson(line, JsonObject.class);
            } catch (RuntimeException e) {
                reply(out, error("invalid JSON request"));
                return;
            }
            String cmd =
                    request != null && request.has("cmd") ? request.get("cmd").getAsString() : "";
            switch (cmd) {
                case "status" -> reply(out, status());
                case "start" -> reply(out, start(request));
                case "stop" -> reply(out, stop(request));
                case "pipeline" -> reply(out, pipeline());
                case "watch" -> watch(request, out);
                default -> reply(out, error("unknown command: " + cmd));
            }
        } catch (IOException | RuntimeException e) {
            // client went away or sent garbage; nothing to report
        }
    }

    private static Map<String, Object> error(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", false);
        m.put("error", message);
        return m;
    }

    private static Map<String, Object> ok() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        return m;
    }

    private static void reply(OutputStream out, Object body) throws IOException {
        out.write((GSON.toJson(body) + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private Map<String, Object> status() {
        Map<String, Object> m = ok();
        m.put("platform", engine.platform());
        m.put("pluginVersion", engine.pluginVersion());
        Map<String, Object> hints = new LinkedHashMap<>();
        hints.put("protocol", engine.hints().protocol);
        hints.put("loaded", engine.hints().loaded);
        hints.put("complete", engine.hints().complete());
        m.put("protocolHints", hints);
        Session s = engine.active();
        if (s == null) {
            m.put("session", null);
        } else {
            Map<String, Object> session = new LinkedHashMap<>();
            session.put("id", s.spec.id);
            session.put("packets", s.packets.get());
            session.put("bytes", s.bytes.get());
            session.put("dropped", s.dropped.get());
            session.put("startedAt", Instant.ofEpochMilli(s.startEpochMs).toString());
            session.put("waitingForJoin", s.spec.nextJoin && !s.bound.get());
            session.put("spec", s.spec);
            m.put("session", session);
        }
        List<Object> conns = new ArrayList<>();
        for (ConnectionInfo c : engine.snapshotConnections()) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("id", c.id);
            o.put("leg", c.leg.code());
            o.put("remote", c.remote);
            o.put("protocol", c.protocol);
            o.put("name", c.name);
            o.put("uuid", c.uuid == null ? null : c.uuid.toString());
            o.put("state", c.state & 0xFF);
            o.put("ignored", c.ignored);
            o.put("recording", s != null && c.bound == s);
            conns.add(o);
        }
        m.put("connections", conns);
        return m;
    }

    private Map<String, Object> start(JsonObject request) {
        if (request == null || !request.has("spec")) {
            return error("missing spec");
        }
        SessionSpec spec;
        try {
            spec = GSON.fromJson(request.get("spec"), SessionSpec.class);
        } catch (RuntimeException e) {
            return error("invalid spec: " + e.getMessage());
        }
        if (spec == null || spec.id == null || spec.id.isEmpty()) {
            return error("spec.id is required");
        }
        if (spec.players == null) {
            spec.players = new ArrayList<>();
        }
        if (spec.until == null) {
            spec.until = new ArrayList<>();
        }
        String failure = engine.start(spec);
        if (failure != null) {
            return error(failure);
        }
        Map<String, Object> m = ok();
        m.put("id", spec.id);
        return m;
    }

    private Map<String, Object> stop(JsonObject request) {
        String reason =
                request != null && request.has("reason") ? request.get("reason").getAsString() : "command";
        Session s = engine.stopAndWait(reason);
        if (s == null) {
            return error("no capture is running");
        }
        Map<String, Object> m = ok();
        m.put("id", s.spec.id);
        m.put("reason", s.stopReason);
        m.put("packets", s.packets.get());
        m.put("bytes", s.bytes.get());
        m.put("dropped", s.dropped.get());
        return m;
    }

    private Map<String, Object> pipeline() {
        Map<String, Object> m = ok();
        List<Object> conns = new ArrayList<>();
        for (ConnectionInfo c : engine.snapshotConnections()) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("id", c.id);
            o.put("name", c.name);
            o.put("leg", c.leg.code());
            List<String> handlers = null;
            if (c.pipelineNames != null) {
                try {
                    handlers = c.pipelineNames.get();
                } catch (RuntimeException e) {
                    handlers = List.of("(unavailable: " + e + ")");
                }
            }
            o.put("handlers", handlers);
            conns.add(o);
        }
        m.put("connections", conns);
        return m;
    }

    private void watch(JsonObject request, OutputStream out) throws IOException {
        LiveHub.Filter filter = new LiveHub.Filter();
        if (request.has("filter") && request.get("filter").isJsonObject()) {
            JsonObject f = request.getAsJsonObject("filter");
            filter.conns = ints(f, "conns");
            filter.dirs = ints(f, "dirs");
            if (f.has("keys") && f.get("keys").isJsonArray()) {
                filter.keys = new HashSet<>();
                for (JsonElement e : f.getAsJsonArray("keys")) {
                    filter.keys.add(GSON.fromJson(e, PacketKey.class));
                }
            }
            if (f.has("payloadBytes")) {
                filter.payloadBytes =
                        Math.max(0, Math.min(256, f.get("payloadBytes").getAsInt()));
            }
        }
        LiveHub.Subscriber sub = engine.hub().subscribe(filter);
        try {
            reply(out, ok());
            while (!closed) {
                String line;
                try {
                    line = sub.lines.poll(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    return;
                }
                if (line == null) {
                    line = "{\"t\":\"ping\"}";
                }
                out.write(line.getBytes(StandardCharsets.UTF_8));
                out.write('\n');
                out.flush();
                long lost = sub.dropped.getAndSet(0);
                if (lost > 0) {
                    out.write(("{\"t\":\"dropped\",\"n\":" + lost + "}\n").getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }
                if (line.startsWith("{\"t\":\"stopped\"")) {
                    return;
                }
            }
        } finally {
            engine.hub().unsubscribe(sub);
        }
    }

    private static java.util.Set<Integer> ints(JsonObject o, String key) {
        if (!o.has(key) || !o.get(key).isJsonArray()) {
            return null;
        }
        java.util.Set<Integer> set = new HashSet<>();
        JsonArray array = o.getAsJsonArray(key);
        for (JsonElement e : array) {
            set.add(e.getAsInt());
        }
        return set;
    }

    @Override
    public void close() {
        closed = true;
        try {
            if (server != null) {
                server.close();
            }
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // shutting down anyway
        }
    }
}
