package net.paradisu.core.locale;

import com.google.common.collect.Maps;
import net.paradisu.core.ParadisuPlugin;
import net.paradisu.core.utils.FileUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TranslationManager {
    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    public static final List<Locale> BUNDLED_LOCALES = List.of(
        );

    private final ParadisuPlugin paradisu;
    private final Set<Locale> installed = ConcurrentHashMap.newKeySet();
    private final Path translationsDirectory;
    private TranslationRegistry registry;

    public TranslationManager(ParadisuPlugin paradisu) {
        this.paradisu = paradisu;
        this.translationsDirectory = this.paradisu.dataDirectory().resolve("translations");

        try {
            FileUtils.createDirectoryIfNotExists(this.translationsDirectory);
        } catch (IOException ignored) {
        }
    }

    public static boolean isTranslationFile(Path path) {
        return path.getFileName().toString().endsWith(".properties");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isAdventureDuplicatesException(Exception e) {
        return e instanceof IllegalArgumentException && (e.getMessage().startsWith("Invalid key") || e.getMessage().startsWith("Translation already exists"));
    }

    public static Locale parseLocale(String locale) {
        return locale == null ? null : Translator.parseLocale(locale);
    }

    public void reload() {
        // remove any previous registry
        if (this.registry != null) {
            GlobalTranslator.translator().removeSource(this.registry);
        }

        // create a translation registry
        this.registry = TranslationRegistry.create(Key.key("paradisu", "main"));
        this.registry.defaultLocale(DEFAULT_LOCALE);

        // load custom translations first, then the base (built-in) translations after.
        loadFromFileSystem(translationsDirectory, false);
        loadFromResourceBundle();

        // register it to the global source, so our translations can be picked up by adventure-platform
        GlobalTranslator.translator().addSource(this.registry);
    }

    /**
     * Loads the bundled translations from the jar file.
     */
    private void loadFromResourceBundle() {
        ResourceBundle defaultBundle = ResourceBundle.getBundle("messages", DEFAULT_LOCALE);
        loadLocaleBundle(defaultBundle, DEFAULT_LOCALE);

        BUNDLED_LOCALES.forEach(locale -> {
            ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
            loadLocaleBundle(bundle, locale);
        });
    }

    /**
     * Loads a locale bundle into the translation registry key by key.
     * @param bundle the bundle to load
     * @param locale the locale of the bundle
     */
    private void loadLocaleBundle(ResourceBundle bundle, Locale locale) {
        bundle.getKeys().asIterator().forEachRemaining(key -> {
            try {
                this.registry.register(key, locale, new MessageFormat(bundle.getString(key)));
            } catch (IllegalArgumentException e) {
                if (!isAdventureDuplicatesException(e)) {
                    this.paradisu.logger().warn("Error loading locale file: " + locale, e);
                }
            }
        });
    }

    /**
     * Loads any custom translations from the plugin configuration folder.
     */
    public void loadFromFileSystem(Path directory, boolean suppressDuplicatesError) {
        List<Path> translationFiles;
        try (Stream<Path> stream = Files.list(directory)) {
            translationFiles = stream.filter(TranslationManager::isTranslationFile).collect(Collectors.toList());
        } catch (IOException e) {
            translationFiles = Collections.emptyList();
        }

        if (translationFiles.isEmpty()) {
            return;
        }

        Map<Locale, ResourceBundle> loaded = new HashMap<>();
        for (Path translationFile : translationFiles) {
            try {
                Map.Entry<Locale, ResourceBundle> result = loadTranslationFile(translationFile);
                loaded.put(result.getKey(), result.getValue());
            } catch (Exception e) {
                if (!suppressDuplicatesError || !isAdventureDuplicatesException(e)) {
                    this.paradisu.logger().warn("Error loading locale file: " + translationFile.getFileName(), e);
                }
            }
        }

        loaded.forEach((locale, bundle) -> {
            if (this.installed.add(locale)) {
                try {
                    this.registry.registerAll(locale, bundle, false);
                } catch (IllegalArgumentException e) {
                    this.paradisu.logger().warn("Error loading locale file: " + locale, e);
                }
            }
        });
    }

    private Map.Entry<Locale, ResourceBundle> loadTranslationFile(Path translationFile) throws IOException {
        String fileName = translationFile.getFileName().toString();
        String localeString = fileName.substring(0, fileName.length() - ".properties".length());
        Locale locale = parseLocale(localeString);

        if (locale == null) {
            throw new IllegalStateException("Unknown locale '" + localeString + "' - unable to register.");
        }

        PropertyResourceBundle bundle;
        try (BufferedReader reader = Files.newBufferedReader(translationFile, StandardCharsets.UTF_8)) {
            bundle = new PropertyResourceBundle(reader);
        }

        this.registry.registerAll(locale, bundle, false);
        this.installed.add(locale);
        return Maps.immutableEntry(locale, bundle);
    }
}

