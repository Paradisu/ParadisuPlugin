/*
 * The official plugin for the Paradisu server. Copyright (C) 2025 Paradisu. https://paradisu.net
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

package net.paradisu.core.packs;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.paradisu.core.ParadisuPlugin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/** Class to manage resource packs */
@Accessors(fluent = true)
@Getter
public class PackManager {
    /** The plugin instance */
    private ParadisuPlugin<?, ?> paradisu;
    /** The list of resource pack urls */
    private List<String> resourcePackUrls;
    /** The default request for the resource packs */
    private CompletableFuture<ResourcePackRequest> defaultRequest;
    /** The staging pack requests */
    private Object2ObjectMap<String, Optional<CompletableFuture<ResourcePackRequest>>> stagingPackRequests;

    /**
     * Constructor for PackManager class to manage resource packs
     *
     * @param paradisu the plugin instance
     * @param resourcePackUrls the list of resource pack urls
     */
    public PackManager(ParadisuPlugin<?, ?> paradisu, List<String> resourcePackUrls) {
        this.paradisu = paradisu;
        this.resourcePackUrls = resourcePackUrls;
        this.stagingPackRequests = new Object2ObjectOpenHashMap<>();
        this.defaultRequest = this.buildDefaultRequest(this.buildDefaultList(this.resourcePackUrls));
    }

    /** Clear the staging pack requests */
    public void clearStagingRequests() {
        this.stagingPackRequests.clear();
    }

    /** Reload the default resource packs and clear the staging requests */
    public void reload() {
        this.defaultRequest = this.buildDefaultRequest(this.buildDefaultList(this.resourcePackUrls));
        this.clearStagingRequests();
    }

    /**
     * Set the resource pack urls
     *
     * @param resourcePackUrls the list of resource pack urls
     */
    public void resourcePackUrls(List<String> resourcePackUrls) {
        this.resourcePackUrls = resourcePackUrls;
        this.defaultRequest = this.buildDefaultRequest(this.buildDefaultList(this.resourcePackUrls));
    }

    /**
     * Get the staging request for a resource pack
     *
     * @param url the url of the resource pack
     * @param tag the tag of the resource pack
     * @return the staging request
     */
    public Optional<CompletableFuture<ResourcePackRequest>> stagingRequest(String url, String tag) {
        return this.stagingPackRequests.computeIfAbsent(tag, k -> {
            return this.buildPackInfo(url).map(this::buildRequestFuture);
        });
    }

    /**
     * Get the staging request for a resource pack
     *
     * @param tag the tag of the resource pack
     * @return the staging request
     */
    public Optional<CompletableFuture<ResourcePackRequest>> stagingRequest(String tag) {
        return this.stagingPackRequests.getOrDefault(tag, Optional.empty());
    }

    /**
     * Build the default list of resource packs
     *
     * @param urls the list of resource pack urls
     * @return the list of resource pack info futures
     */
    private List<CompletableFuture<ResourcePackInfo>> buildDefaultList(List<String> urls) {
        List<CompletableFuture<ResourcePackInfo>> packs = new ArrayList<>();

        for (String url : urls) {
            buildPackInfo(url).ifPresent(packInfo -> {
                packs.add(packInfo);
            });
        }

        return packs;
    }

    private CompletableFuture<ResourcePackRequest> buildDefaultRequest(
            List<CompletableFuture<ResourcePackInfo>> packInfos) {
        return this.buildRequestFuture(packInfos).whenCompleteAsync((request, e) -> {
            if (e == null) {
                this.paradisu.logger().info("Successfully loaded the default resource packs");
            } else {
                this.paradisu.logger().error("Failed to build default request: " + e.getMessage());
            }
        });
    }

    /**
     * Build the pack info for a resource pack
     *
     * @param url the url of the resource pack
     * @return the pack info future
     */
    private Optional<CompletableFuture<ResourcePackInfo>> buildPackInfo(String url) {
        try {
            return Optional.of(
                    ResourcePackInfo.resourcePackInfo().uri(new URI(url)).computeHashAndBuild());
        } catch (URISyntaxException e) {
            this.paradisu.logger().error("Failed to load pack: " + url + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Build the request future for a resource pack
     *
     * @param packInfo the pack info future
     * @return the request future
     */
    private CompletableFuture<ResourcePackRequest> buildRequestFuture(CompletableFuture<ResourcePackInfo> packInfo) {
        return packInfo.thenApply(info -> {
            return ResourcePackRequest.resourcePackRequest().packs(info).build();
        });
    }

    /**
     * Build the request future for a list of resource packs
     *
     * @param packInfos the list of pack info futures
     * @return the request future
     */
    private CompletableFuture<ResourcePackRequest> buildRequestFuture(
            List<CompletableFuture<ResourcePackInfo>> packInfos) {
        @SuppressWarnings("unchecked")
        CompletableFuture<ResourcePackInfo>[] packFutures = packInfos.toArray(new CompletableFuture[0]);

        return CompletableFuture.allOf(packFutures).thenApply(v -> {
            List<ResourcePackInfo> packs = new ArrayList<>();

            for (CompletableFuture<ResourcePackInfo> packFuture : packFutures) {
                try {
                    packs.add(packFuture.get());
                } catch (InterruptedException | ExecutionException e) {
                    this.paradisu.logger().error("Failed to load pack: " + e.getMessage());
                }
            }
            return ResourcePackRequest.resourcePackRequest().packs(packs).build();
        });
    }
}
