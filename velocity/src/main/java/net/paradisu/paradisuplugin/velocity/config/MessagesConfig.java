package net.paradisu.paradisuplugin.velocity.config;

import java.util.List;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public final class MessagesConfig {
    @Setting("message-prefix") private String messagePrefix = "<lang:paradisu.general.prefix>";
    private Commands commands = new Commands();

    @ConfigSerializable
    public static final class Commands {
        private Locate locate = new Locate();
        private Vparadisu vparadisu = new Vparadisu();
        private Vtp vtp = new Vtp();
        private Vtpa vtpa = new Vtpa();
        private Vtphere vtphere = new Vtphere();
        private Vtppos vtppos = new Vtppos();
        private Vtpr vtpr = new Vtpr();

        @ConfigSerializable
        public static final class Locate {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.locate>";
            @Setting("help-args") private List<String> helpArgs = List.of(
                "<lang:paradisu.command.help.locate.0>");
            @Setting("output") private List<String> output = List.of(
                "<lang:paradisu.command.output.locate:'<player>':'<server>':'<posx>':'<posy>':'<posz>'>");
        }
        @ConfigSerializable
        public static final class Vparadisu {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.vparadisu>";
            private About about = new About();
            private Reload reload = new Reload();
            @ConfigSerializable
            public static final class About {
                @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.vparadisu.about>";
                @Setting("output") private List<String> output = List.of(
                    "<lang:paradisu.command.output.vparadisu.about:'<version>'>");
            }
            @ConfigSerializable
            public static final class Reload {
                @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.vparadisu.reload>";
                @Setting("output") private List<String> output = List.of(
                    "<lang:paradisu.command.output.vparadisu.reload>"
                    );
            }
        }
        @ConfigSerializable
        public static final class Vtp {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.vtp>";
            @Setting("help-args") private List<String> helpArgs = List.of(
                "<lang:paradisu.command.help.vtp.0>",
                "<lang:paradisu.command.help.vtp.1>");
            @Setting("output") private List<String> output = List.of(
                "<lang:paradisu.command.output.vtp:'<player>':'<target>'>");
        }
        @ConfigSerializable
        public static final class Vtpa {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.vtpa>";
            @Setting("help-args") private List<String> helpArgs = List.of(
                "<lang:paradisu.command.help.vtpa.0>");
            @Setting("output") private List<String> output = List.of(
                "<lang:paradisu.command.output.vtpa.0:'<player>'>",
                "<lang:paradisu.command.output.vtpa.1:'<player>'>",
                "<lang:paradisu.command.output.vtpa.2>");
        }
        @ConfigSerializable
        public static final class Vtphere {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.vtphere>";
            @Setting("help-args") private List<String> helpArgs = List.of(
                "<lang:paradisu.command.help.vtphere.0>");
            @Setting("output") private List<String> output = List.of(
                "<lang:paradisu.command.output.vtphere:'<player>'>");
        }
        @ConfigSerializable
        public static final class Vtppos {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.vtppos>";
            @Setting("help-args") private List<String> helpArgs = List.of(
                "<lang:paradisu.command.help.vtppos.0>",
                "<lang:paradisu.command.help.vtppos.1>",
                "<lang:paradisu.command.help.vtppos.2>",
                "<lang:paradisu.command.help.vtppos.3>",
                "<lang:paradisu.command.help.vtppos.4>");
            @Setting("output") private List<String> output = List.of(
                "<lang:paradisu.command.output.vtppos:'<player>':'<posx>':'<posy>':'<posz>':'<server>'>"
                );
        }
        @ConfigSerializable
        public static final class Vtpr {
            @Setting("help-msg") private String helpMsg = "<lang:paradisu.command.help.vtpr>";
            @Setting("help-args") private List<String> helpArgs = List.of(
                "<lang:paradisu.command.help.vtpr.0>",
                "<lang:paradisu.command.help.vtpr.1>");
            @Setting("output") private List<String> output = List.of(
                "<lang:paradisu.command.output.vtpr.0:'<player>'>",
                "<lang:paradisu.command.output.vtpr.1:'<player>'>",
                "<lang:paradisu.command.output.vtpr.2:'<player>'>",
                "<lang:paradisu.command.output.vtpr.3:'<player>'>");
        }
    }
}
