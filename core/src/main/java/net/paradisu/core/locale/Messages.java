package net.paradisu.core.locale;


import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;

public interface Messages {
    Component GENERAL_PREFIX = translatable("paradisu.general.prefix");

    /**
     * Take an Adeventure text component as an inuput and prepends the specified prefix to it.
     * @param component Adventure text component to be prefixed
     * @return A built Adventure text component with the prefix
     */
    static TextComponent prefixed(ComponentLike component) {
        return text()
                .append(GENERAL_PREFIX)
                .append(Component.space())
                .append(component)
                .build();
    }
}
