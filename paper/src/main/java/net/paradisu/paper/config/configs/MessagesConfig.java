package net.paradisu.paper.config.configs;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;

@Accessors(fluent = true)
@Getter
@ConfigSerializable
public final class MessagesConfig {
    @Setting("message-prefix") private String messagePrefix = "<lang:paradisu.general.prefix>";
    private Utility utility = new Utility();
    private Commands commands = new Commands();

    @Getter
    @ConfigSerializable
    public static final class Utility {
        @Setting("message-divider") private String messageDivider = "<lang:paradisu.general.line> | <lang:paradisu.general.line>";
        @Setting("message-prefix") private String messagePrefix = "<lang:paradisu.general.prefix>";
        @Comment("Prefixes to be displayed in order in various contexts. The prefix of the highest rank should be used first. Players must have the meta for the prefix to be displayed. The first entry corresponds to a meta value of meta.playerlist-index.0")
        @Setting("list-prefixes") private List<String> listPrefixes = List.of(
            "<#43b6c8><lang:paradisu.ranks.owner><lang:paradisu.general.arrow> ",
            "<#f8991d><lang:paradisu.ranks.dev><lang:paradisu.general.arrow> ",
            "<#f36c36><lang:paradisu.ranks.builder><lang:paradisu.general.arrow> ",
            "<#fd6868><lang:paradisu.ranks.animator><lang:paradisu.general.arrow> ",
            "<#eab134><lang:paradisu.ranks.audio_engineer><lang:paradisu.general.arrow> ",
            "<#9800a6><lang:paradisu.ranks.graphic_designer><lang:paradisu.general.arrow> ",
            "<#08a699><lang:paradisu.ranks.modeler><lang:paradisu.general.arrow> ",
            "<#00d455><lang:paradisu.ranks.moderator><lang:paradisu.general.arrow> ",
            "<#4899e0><lang:paradisu.ranks.texture_artist><lang:paradisu.general.arrow> ",
            "<#f89ede><lang:paradisu.ranks.supporter><lang:paradisu.general.arrow> ",
            "<#d1e3ff><lang:paradisu.ranks.visitor><lang:paradisu.general.arrow> ");

        public String listPrefixes(int index) {
            int size = this.listPrefixes.size();
            return index < size ? this.listPrefixes.get(index) : this.listPrefixes.get(size - 1);
        }
    }

    @Getter 
    @ConfigSerializable
    public static final class Commands {
        private Paradisu paradisu = new Paradisu();
        private Hat hat = new Hat();


        @Getter
        @ConfigSerializable
        public static final class Hat {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.hat>";
            @Setting("output") private List<String> output = List.of(
                    "<lang:paradisu.command.output.hat>");
        }


        @Getter 
        @ConfigSerializable
        public static final class Paradisu {
            private About about = new About();
            private Reload reload = new Reload();
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.paradisu>";

            @Getter 
            @ConfigSerializable
            public static final class About {
                @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.paradisu.about>";
                @Setting("output") private List<String> output = List.of(
                    "<lang:paradisu.command.output.paradisu.about:'<gold><lang:paradisu.general.version>'>");
            }

            @Getter 
            @ConfigSerializable
            public static final class Reload {
                @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.paradisu.reload>";
                @Setting("output") private List<String> output = List.of(
                    "<lang:paradisu.command.output.paradisu.reload>");
            }
        }
    }
}
