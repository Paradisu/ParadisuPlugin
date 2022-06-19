package net.paradisu.paradisuplugin.velocity.locale;


import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;

public interface Messages {
    Component PREFIX = translatable("paradisu.general.prefix");
    Component ABOUT = translatable("paradisu.command.about");
    Component RELOAD = translatable("paradisu.command.reload");
    static TextComponent prefixed(ComponentLike component) {
        return text()
                .append(PREFIX)
                .append(Component.space())
                .append(component)
                .build();
    }
}
