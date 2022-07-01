package net.paradisu.paradisuplugin.velocity.config;

import java.util.List;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public final class MessagesConfig {
    @Setting("message-prefix") private String messagePrefix = "<lang:paradisu.general.prefix>";
    private Commands commands = new Commands();

    public Commands commands() {
        return this.commands;
    }

    @ConfigSerializable
    public static final class Commands {
        private Locate locate = new Locate();
        private Vparadisu vparadisu = new Vparadisu();
        private Vtp tp = new Vtp();
        private Vtpa tpa = new Vtpa();
        private Vtph tph = new Vtph();
        private Vtppos tppos = new Vtppos();
        private Vtpr tpr = new Vtpr();
        private Vtprh tprh = new Vtprh();

        public Locate locate() {
            return this.locate;
        }

        public Vparadisu vparadisu() {
            return this.vparadisu;
        }

        public Vtp tp() {
            return this.tp;
        }

        public Vtpa tpa() {
            return this.tpa;
        }

        public Vtph tph() {
            return this.tph;
        }

        public Vtppos tppos() {
            return this.tppos;
        }

        public Vtpr tpr() {
            return this.tpr;
        }

        public Vtprh tprh() {
            return this.tprh;
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

        @ConfigSerializable
        public static final class Vtp {
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
        public static final class Vtpa {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.tpa>";
            @Setting("help-args") private List<String> helpArgs = List.of(
                "<lang:paradisu.command.help.tpa.0>");
            @Setting("output") private List<String> output = List.of(
                "<lang:paradisu.command.output.tpa.0:'<gold><player>'>",
                "<lang:paradisu.command.output.tpa.1:'<gold><player>'>",
                "<lang:paradisu.command.output.tpa.2>");
            
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
        public static final class Vtph {
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
        public static final class Vtppos {
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
        public static final class Vtpr {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.tpr>";
            @Setting("help-args") private List<String> helpArgs = List.of(
                "<lang:paradisu.command.help.tpr.0>");
            @Setting("output") private List<String> output = List.of(
                "<lang:paradisu.command.output.tpr.0:'<gold><player>'><br><lang:paradisu.command.output.tpr.1:\"<click:run_command:/tpa><hover:show_text:'<green>Accept'><gold>/tpa\">",
                "<lang:paradisu.command.output.tpr.2:'<gold><player>'>");
            
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
        public static final class Vtprh {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.tprh>";
            @Setting("help-args") private List<String> helpArgs = List.of(
                "<lang:paradisu.command.help.tprh.0>");
            @Setting("output") private List<String> output = List.of(
                "<lang:paradisu.command.output.tprh.0:'<gold><player>'><br><lang:paradisu.command.output.tprh.1:\"<click:run_command:/tpa><hover:show_text:'<green>Accept'><gold>/tpa\">",
                "<lang:paradisu.command.output.tprh.2:'<gold><player>'>");
            
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
    }
}
