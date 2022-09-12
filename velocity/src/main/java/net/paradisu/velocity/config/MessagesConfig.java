package net.paradisu.velocity.config;

import java.util.List;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public final class MessagesConfig {
    @Setting("message-prefix") private String messagePrefix = "<lang:paradisu.general.prefix>";
    private Utility utility = new Utility();
    private Commands commands = new Commands();

    public Utility utility() {
        return this.utility;
    }

    public Commands commands() {
        return this.commands;
    }

    @ConfigSerializable
    public static final class Utility {
        @Setting("message-divider") private String messageDivider = "<lang:paradisu.general.line> | <lang:paradisu.general.line>";
        @Setting("message-prefix") private String messagePrefix = "<lang:paradisu.general.prefix>";
        @Comment("Prefixes to be displayed when /list is run. The prefix of the highest rank should be used first. Players must have the meta for the prefix to be displayed. The first entry corresponds to a meta value of meta.playerlist-index.0")
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

        public String messageDivider() {
            return this.messageDivider;
        }

        public String messagePrefix() {
            return this.messagePrefix;
        }

        public String listPrefixes(int index) {
            int size = this.listPrefixes.size();
            return index < size ? this.listPrefixes.get(index) : this.listPrefixes.get(size - 1);
        }
    }

    @ConfigSerializable
    public static final class Commands {
        private Locate locate = new Locate();
        private Ls ls = new Ls();
        private Back back = new Back();
        private Tp tp = new Tp();
        private Tpa tpa = new Tpa();
        private Tpc tpc = new Tpc();
        private Tpd tpd = new Tpd();
        private Tph tph = new Tph();
        private Tppos tppos = new Tppos();
        private Tpr tpr = new Tpr();
        private Tprh tprh = new Tprh();
        private Vparadisu vparadisu = new Vparadisu();

        public Locate locate() {
            return this.locate;
        }

        public Ls ls() {
            return this.ls;
        }

        public Back back() {
            return this.back;
        }

        public Tp tp() {
            return this.tp;
        }

        public Tpa tpa() {
            return this.tpa;
        }

        public Tpc tpc() {
            return this.tpc;
        }

        public Tpd tpd() {
            return this.tpd;
        }

        public Tph tph() {
            return this.tph;
        }

        public Tppos tppos() {
            return this.tppos;
        }

        public Tpr tpr() {
            return this.tpr;
        }

        public Tprh tprh() {
            return this.tprh;
        }

        public Vparadisu vparadisu() {
            return this.vparadisu;
        }

        @ConfigSerializable
        public static final class Back {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.back>";
            @Setting("help-args") private List<String> helpArgs = List.of(
                "<lang:paradisu.command.help.back.0>");
            @Setting("output") private List<String> output = List.of(
                "<lang:paradisu.command.output.back.0:'<gold><player>'>",
                "<lang:paradisu.command.output.back.1:'<gold><player>'>");

            public String helpMsg() {
                return this.helpMsg;
            }

            public String helpArgs(int index) {
                return this.helpArgs.get(index);
            }

            public String output(int index) {
                return this.output.get(index);
            }
        }

        @ConfigSerializable
        public static final class Locate {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.locate>";
            @Setting("help-args") private List<String> helpArgs = List.of(
                "<lang:paradisu.command.help.locate.0>");
            @Setting("output") private List<String> output = List.of(
                "<lang:paradisu.command.output.locate:'<gold><player>':'<gold><server>':'<gold><posx>':'<gold><posy>':'<gold><posz>'>");
            
            public String helpMsg() {
                return this.helpMsg;
            }

            public String helpArgs(int index) {
                return this.helpArgs.get(index);
            }

            public String output(int index) {
                return this.output.get(index);
            }
        }

        @ConfigSerializable
        public static final class Ls {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.ls>";
            @Setting("output") private List<String> output = List.of(
                "<lang:paradisu.command.output.ls.0><br>",
                "<lang:paradisu.command.output.ls.1><br>",
                "<lang:paradisu.command.output.ls.2:'<gold><count>'><br>");

            public String helpMsg() {
                return this.helpMsg;
            }

            public String output(int index) {
                return this.output.get(index);
            }
        }

        @ConfigSerializable
        public static final class Tp {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.tp>";
            @Setting("help-args") private List<String> helpArgs = List.of(
                "<lang:paradisu.command.help.tp.0>",
                "<lang:paradisu.command.help.tp.1>");
            @Setting("output") private List<String> output = List.of(
                "<lang:paradisu.command.output.tp:'<gold><player>':'<gold><target>'>");
            
            public String helpMsg() {
                return this.helpMsg;
            }

            public String helpArgs(int index) {
                return this.helpArgs.get(index);
            }

            public String output(int index) {
                return this.output.get(index);
            }
        }
        @ConfigSerializable
        public static final class Tpa {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.tpa>";
            @Setting("help-args") private List<String> helpArgs = List.of(
                "<lang:paradisu.command.help.tpa.0>");
            @Setting("output") private List<String> output = List.of(
                "<lang:paradisu.command.output.tpa.0:'<gold><player>'>",
                "<lang:paradisu.command.output.tpa.1:'<gold><player>'>",
                "<lang:paradisu.command.output.tpa.2>",
                "<lang:paradisu.command.output.tpa.3:'<gold><player>'>");
            
            public String helpMsg() {
                return this.helpMsg;
            }

            public String helpArgs(int index) {
                return this.helpArgs.get(index);
            }

            public String output(int index) {
                return this.output.get(index);
            }
        }

        @ConfigSerializable
        public static final class Tpc {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.tpc>";
            @Setting("help-args") private List<String> helpArgs = List.of(
                "<lang:paradisu.command.help.tpc.0>");
            @Setting("output") private List<String> output = List.of(
                "<lang:paradisu.command.output.tpc.0:'<gold><player>'>",
                "<lang:paradisu.command.output.tpc.1:'<gold><player>'>",
                "<lang:paradisu.command.output.tpc.2>",
                "<lang:paradisu.command.output.tpc.3:'<gold><player>'>");
            
            public String helpMsg() {
                return this.helpMsg;
            }

            public String helpArgs(int index) {
                return this.helpArgs.get(index);
            }

            public String output(int index) {
                return this.output.get(index);
            }
        }

        @ConfigSerializable
        public static final class Tpd {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.tpd>";
            @Setting("help-args") private List<String> helpArgs = List.of(
                "<lang:paradisu.command.help.tpd.0>");
            @Setting("output") private List<String> output = List.of(
                "<lang:paradisu.command.output.tpd.0:'<gold><player>'>",
                "<lang:paradisu.command.output.tpd.1:'<gold><player>'>",
                "<lang:paradisu.command.output.tpd.2>",
                "<lang:paradisu.command.output.tpc.3:'<gold><player>'>");
            
            public String helpMsg() {
                return this.helpMsg;
            }

            public String helpArgs(int index) {
                return this.helpArgs.get(index);
            }

            public String output(int index) {
                return this.output.get(index);
            }
        }

        @ConfigSerializable
        public static final class Tph {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.tph>";
            @Setting("help-args") private List<String> helpArgs = List.of(
                "<lang:paradisu.command.help.tph.0>");
            @Setting("output") private List<String> output = List.of(
                "<lang:paradisu.command.output.tph:'<gold><player>'>");
            
            public String helpMsg() {
                return this.helpMsg;
            }

            public String helpArgs(int index) {
                return this.helpArgs.get(index);
            }

            public String output(int index) {
                return this.output.get(index);
            }
        }

        @ConfigSerializable
        public static final class Tppos {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.tppos>";
            @Setting("help-args") private List<String> helpArgs = List.of(
                "<lang:paradisu.command.help.tppos.0>",
                "<lang:paradisu.command.help.tppos.1>",
                "<lang:paradisu.command.help.tppos.2>",
                "<lang:paradisu.command.help.tppos.3>",
                "<lang:paradisu.command.help.tppos.4>");
            @Setting("output") private List<String> output = List.of(
                "<lang:paradisu.command.output.tppos:'<gold><player>':'<gold><posx>':'<gold><posy>':'<gold><posz>':'<gold><server>'>"
                );
            
            public String helpMsg() {
                return this.helpMsg;
            }

            public String helpArgs(int index) {
                return this.helpArgs.get(index);
            }
            
            public String output(int index) {
                return this.output.get(index);
            }
        }

        @ConfigSerializable
        public static final class Tpr {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.tpr>";
            @Setting("help-args") private List<String> helpArgs = List.of(
                "<lang:paradisu.command.help.tpr.0>");
            @Setting("output") private List<String> output = List.of(
                "<lang:paradisu.command.output.tpr.0:'<gold><player>'><br><lang:paradisu.command.output.tpr.1:\"<gold><command>\">",
                "<lang:paradisu.command.output.tpr.2:'<gold><player>'>",
                "<lang:paradisu.command.output.tpr.3:'<gold><player>'>");
            
            public String helpMsg() {
                return this.helpMsg;
            }

            public String helpArgs(int index) {
                return this.helpArgs.get(index);
            }

            public String output(int index) {
                return this.output.get(index);
            }
        }

        @ConfigSerializable
        public static final class Tprh {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.tprh>";
            @Setting("help-args") private List<String> helpArgs = List.of(
                "<lang:paradisu.command.help.tprh.0>");
            @Setting("output") private List<String> output = List.of(
                "<lang:paradisu.command.output.tprh.0:'<gold><player>'><br><lang:paradisu.command.output.tprh.1:\"<gold><command>\">",
                "<lang:paradisu.command.output.tprh.2:'<gold><player>'>",
                "<lang:paradisu.command.output.tprh.3:'<gold><player>'>");
            
            public String helpMsg() {
                return this.helpMsg;
            }

            public String helpArgs(int index) {
                return this.helpArgs.get(index);
            }
            
            public String output(int index) {
                return this.output.get(index);
            }
        }

        @ConfigSerializable
        public static final class Vparadisu {
            private About about = new About();
            private Reload reload = new Reload();

            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.vparadisu>";

            public About about() {
                return this.about;
            }

            public Reload reload() {
                return this.reload;
            }

            public String helpMsg() {
                return this.helpMsg;
            }

            @ConfigSerializable
            public static final class About {
                @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.vparadisu.about>";
                @Setting("output") private List<String> output = List.of(
                    "<lang:paradisu.command.output.vparadisu.about:'<gold><lang:paradisu.general.version>'>");

                public String helpMsg() {
                    return this.helpMsg;
                }

                public String output(int index) {
                    return this.output.get(index);
                }
            }
            @ConfigSerializable
            public static final class Reload {
                @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.vparadisu.reload>";
                @Setting("output") private List<String> output = List.of(
                    "<lang:paradisu.command.output.vparadisu.reload>");
                
                public String helpMsg() {
                    return this.helpMsg;
                }

                public String output(int index) {
                    return this.output.get(index);
                }
            }
        }
    }
}
