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
    @Setting("message-prefix")
    private String messagePrefix = "<lang:paradisu.general.prefix>";

    private Utility utility = new Utility();
    private Commands commands = new Commands();

    @Getter
    @ConfigSerializable
    public static final class Utility {
        @Setting("message-divider")
        private String messageDivider = "<lang:paradisu.general.line> | <lang:paradisu.general.line>";

        @Setting("message-prefix")
        private String messagePrefix = "<lang:paradisu.general.prefix>";

        @Comment(
                "Prefixes to be displayed in order in various contexts. The prefix of the highest rank should be used first. Players must have the meta for the prefix to be displayed. The first entry corresponds to a meta value of meta.playerlist-index.0")
        @Setting("list-prefixes")
        private List<String> listPrefixes = List.of(
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
        private Warp warp = new Warp();
        private Warps warps = new Warps();

        @Getter
        @ConfigSerializable
        public static final class Hat {
            @Setting("help-msg")
            private String helpMsg = "<lang:paradisu.command.help.hat>";

            @Setting("output")
            private List<String> output = List.of("<lang:paradisu.command.output.hat>");
        }

        @Getter
        @ConfigSerializable
        public static final class Paradisu {
            private About about = new About();
            private Reload reload = new Reload();
            private Warp warp = new Warp();

            @Setting("help-msg")
            private String helpMsg = "<lang:paradisu.command.help.paradisu>";

            @Getter
            @ConfigSerializable
            public static final class About {
                @Setting("help-msg")
                private String helpMsg = "<lang:paradisu.command.help.paradisu.about>";

                @Setting("output")
                private List<String> output = List.of(
                        "<lang:paradisu.command.output.paradisu.about:'<gold><lang:paradisu.general.version>'>");
            }

            @Getter
            @ConfigSerializable
            public static final class Reload {
                @Setting("help-msg")
                private String helpMsg = "<lang:paradisu.command.help.paradisu.reload>";

                @Setting("output")
                private List<String> output = List.of("<lang:paradisu.command.output.paradisu.reload>");
            }

            @Getter
            @ConfigSerializable
            public static final class Warp {
                private Send send = new Send();
                private Create create = new Create();
                private Update update = new Update();
                private Delete delete = new Delete();
                private Permission permission = new Permission();
                private Aliases aliases = new Aliases();

                @Setting("help-msg")
                private String helpMsg = "<lang:paradisu.command.help.paradisu.warp>";

                @Setting("output")
                private List<String> output = List.of(
                        "<lang:paradisu.command.output.paradisu.warp.0:'<gold><warp>':'<gold><context>'>",
                        "<lang:paradisu.command.output.paradisu.warp.1>",
                        "<lang:paradisu.command.output.paradisu.warp.2:'<gold><warp>'>");

                @Getter
                @ConfigSerializable
                public static final class Send {
                    @Setting("help-msg")
                    private String helpMsg = "<lang:paradisu.command.help.paradisu.warp.send>";

                    @Setting("help-args")
                    private List<String> helpArgs = List.of(
                            "<lang:paradisu.command.help.paradisu.warp.send.0>",
                            "<lang:paradisu.command.help.paradisu.warp.send.1>");
                }

                @Getter
                @ConfigSerializable
                public static final class Create {
                    @Setting("help-msg")
                    private String helpMsg = "<lang:paradisu.command.help.paradisu.warp.create>";

                    @Setting("help-args")
                    private List<String> helpArgs = List.of(
                            "<lang:paradisu.command.help.paradisu.warp.create.0>",
                            "<lang:paradisu.command.help.paradisu.warp.create.1>");

                    @Setting("output")
                    private List<String> output = List.of(
                            "<lang:paradisu.command.output.paradisu.warp.create.0:'<gold><warp>'>",
                            "<lang:paradisu.command.output.paradisu.warp.create.1:'<gold><warp>'>",
                            "<lang:paradisu.command.output.paradisu.warp.create.2:'<gold><max>'>");
                }

                @Getter
                @ConfigSerializable
                public static final class Update {
                    @Setting("help-msg")
                    private String helpMsg = "<lang:paradisu.command.help.paradisu.warp.update>";

                    @Setting("help-args")
                    private List<String> helpArgs = List.of("<lang:paradisu.command.help.paradisu.warp.update.0>");

                    @Setting("output")
                    private List<String> output =
                            List.of("<lang:paradisu.command.output.paradisu.warp.update:'<gold><warp>'>");
                }

                @Getter
                @ConfigSerializable
                public static final class Delete {
                    @Setting("help-msg")
                    private String helpMsg = "<lang:paradisu.command.help.paradisu.warp.delete>";

                    @Setting("help-args")
                    private List<String> helpArgs = List.of("<lang:paradisu.command.help.paradisu.warp.delete.0>");

                    @Setting("output")
                    private List<String> output =
                            List.of("<lang:paradisu.command.output.paradisu.warp.delete:'<gold><warp>'>");
                }

                @Getter
                @ConfigSerializable
                public static final class Permission {
                    @Setting("help-msg")
                    private String helpMsg = "<lang:paradisu.command.help.paradisu.warp.permission>";

                    @Setting("help-args")
                    private List<String> helpArgs = List.of(
                            "<lang:paradisu.command.help.paradisu.warp.permission.0>",
                            "<lang:paradisu.command.help.paradisu.warp.permission.1>");

                    @Setting("output")
                    private List<String> output = List.of(
                            "<lang:paradisu.command.output.paradisu.warp.permission.0:'<gold><warp>':'<gold><permission>'>",
                            "<lang:paradisu.command.output.paradisu.warp.permission.1:'<gold><warp>'>");
                }

                @Getter
                @ConfigSerializable
                public static final class Aliases {
                    @Setting("help-msg")
                    private String helpMsg = "<lang:paradisu.command.help.paradisu.warp.aliases>";

                    @Setting("help-args")
                    private List<String> helpArgs = List.of(
                            "<lang:paradisu.command.help.paradisu.warp.aliases.0>",
                            "<lang:paradisu.command.help.paradisu.warp.aliases.1>");

                    @Setting("output")
                    private List<String> output = List.of(
                            "<lang:paradisu.command.output.paradisu.warp.aliases.0:'<gold><warp>':'<gold><aliases>'>",
                            "<lang:paradisu.command.output.paradisu.warp.aliases.1:'<gold><warp>'>");
                }
            }
        }

        @Getter
        @ConfigSerializable
        public static final class Warp {
            @Setting("help-msg")
            private String helpMsg = "<lang:paradisu.command.help.warp>";

            @Setting("help-args")
            private List<String> helpArgs = List.of("<lang:paradisu.command.help.warp.0>");

            @Setting("output")
            private List<String> output = List.of(
                    "<lang:paradisu.command.output.warp.0>",
                    "<lang:paradisu.command.output.warp.1:'<gold><warp>'>",
                    "<lang:paradisu.command.output.warp.2:'<gold><player>':'<gold><warp>'>",
                    "<lang:paradisu.command.output.warp.3>",
                    "<lang:paradisu.command.output.warp.4:'<gold><warp>'>",
                    "<lang:paradisu.command.output.warp.5>",
                    "<lang:paradisu.command.output.warp.6:'<gold><warp>'>",
                    "<lang:paradisu.command.output.warp.7:'<gold><seconds>'>");
        }

        @Getter
        @ConfigSerializable
        public static final class Warps {
            @Setting("help-msg")
            private String helpMsg = "<lang:paradisu.command.help.warps>";

            @Setting("output")
            private List<String> output = List.of(
                    "<lang:paradisu.command.output.warps.0:'<gold><warps>'>", "<lang:paradisu.command.output.warps.1>");
        }
    }
}
