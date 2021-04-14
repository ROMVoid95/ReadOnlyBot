/*
 * Copyright (C) 2016-2021 David Rubio Escares / Kodehawa
 *
 *  Mantaro is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  Mantaro is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mantaro. If not, see http://www.gnu.org/licenses/
 */

package net.readonly.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.readonly.commands.info.stats.CategoryStatsManager;
import net.readonly.commands.info.stats.CommandStatsManager;
import net.readonly.core.command.CommandManager;
import net.readonly.core.command.NewCommand;
import net.readonly.core.command.NewContext;
import net.readonly.core.command.argument.ArgumentParseError;
import net.readonly.core.modules.commands.AliasCommand;
import net.readonly.core.modules.commands.base.Command;
import net.readonly.core.modules.commands.base.CommandCategory;
import net.readonly.core.modules.commands.base.CommandPermission;
import net.readonly.core.modules.commands.base.Context;
import net.readonly.core.modules.commands.help.HelpContent;
import net.readonly.core.modules.commands.i18n.I18nContext;
import net.readonly.utils.EmoteReference;
import net.readonly.utils.exports.Metrics;

public class CommandRegistry {
    private static final Logger log = LoggerFactory.getLogger(CommandRegistry.class);

    private final Map<String, Command> commands;
    private final CommandManager newCommands = new CommandManager();

    public CommandRegistry(Map<String, Command> commands) {
        this.commands = Preconditions.checkNotNull(commands);
    }

    public CommandRegistry() {
        this(new HashMap<>());
    }

    public Map<String, Command> commands() {
        return commands;
    }

    public Map<String, Command> getCommandsForCategory(CommandCategory category) {
        return commands.entrySet().stream()
                .filter(cmd -> cmd.getValue().category() == category)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void process(GuildMessageReceivedEvent event, String cmdName, String content, String prefix, boolean isMention) {
    	final var start = System.currentTimeMillis();
        var command = commands.get(cmdName.toLowerCase());

        if (command == null) {
            return;
        }

        final var author = event.getAuthor();
        final var channel = event.getChannel();
        // Variable used in lambda expression should be final or effectively final...
        final var cmd = command;
        final var guild = event.getGuild();

        final var member = event.getMember();
        
        if (!cmd.permission().test(member)) {
            channel.sendMessage(EmoteReference.STOP + "You have no permissions to trigger this command :(").queue();
            return;
        }
        
        // Used a command on the new system?
        boolean executedNew;
        try {
            executedNew = newCommands.execute(new NewContext(event.getMessage(),
            		new I18nContext(),
                    event.getMessage().getContentRaw().substring(prefix.length()))
            );
        } catch (ArgumentParseError e) {
            if (e.getMessage() != null) {
                channel.sendMessage(EmoteReference.ERROR + e.getMessage()).queue();
            } else {
                e.printStackTrace();
                channel.sendMessage(
                        EmoteReference.ERROR + "There was an error parsing the arguments for this command. Please report this to the developers"
                ).queue();
            }

            return;
        }

        if (!executedNew) {
            cmd.run(new Context(event, new I18nContext(), content, isMention), cmdName, content);
        }

        log.debug("!! COMMAND INVOKE: command:{}, user:{} ({}), guild:{}, channel:{}",
                cmdName, author.getAsTag(), author.getId(), guild.getId(), channel.getId()
        );
        
        final var end = System.currentTimeMillis();
        final var category = root(cmd).category().name().toLowerCase();
        
        CommandStatsManager.log(name(cmd, cmdName));
        CategoryStatsManager.log(category);
        
        Metrics.CATEGORY_COUNTER.labels(category).inc();
        Metrics.COMMAND_COUNTER.labels(name(cmd, cmdName)).inc();
        Metrics.COMMAND_LATENCY.observe(end - start);
    }

    public void register(Class<? extends NewCommand> clazz) {
        var cmd = newCommands.register(clazz);
        var p = new ProxyCommand(cmd);
        commands.put(cmd.name(), p);
        cmd.aliases().forEach(a -> commands.put(a, new AliasProxyCommand(p).get()));
    }

    public <T extends Command> T register(String name, T command) {
        commands.putIfAbsent(name, command);
        log.debug("Registered command " + name);
        return command;
    }

    public void registerAlias(String command, String alias) {
        if (!commands.containsKey(command)) {
            log.error(command + " isn't in the command map...");
        }

        Command parent = commands.get(command);
        if (parent instanceof ProxyCommand) {
            throw new IllegalArgumentException("Use @Alias instead");
        }
        parent.getAliases().add(alias);

        register(alias, new AliasCommand(alias, command, parent));
    }
    
    private static String name(Command c, String userInput) {
        if (c instanceof AliasCommand) {
            return ((AliasCommand) c).getOriginalName();
        }

        if (c instanceof ProxyCommand) {
            return ((ProxyCommand) c).c.name();
        }

        return userInput.toLowerCase();
    }

    private Command root(Command c) {
        if (c instanceof AliasCommand) {
            return commands.get(((AliasCommand) c).parentName());
        }

        if (c instanceof AliasProxyCommand) {
            return ((AliasProxyCommand) c).p;
        }
        return c;
    }
    
    private static class ProxyCommand implements Command {
        private final NewCommand c;

        private ProxyCommand(NewCommand c) {
            this.c = c;
        }

        @Override
        public CommandCategory category() {
            return c.category();
        }

        @Override
        public CommandPermission permission() {
            return c.permission();
        }

        @Override
        public void run(Context context, String commandName, String content) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HelpContent help() {
            return c.help();
        }

        @Override
        public List<String> getAliases() {
            return c.aliases();
        }
    }

    private static class AliasProxyCommand extends ProxyCommand {
        private final ProxyCommand p;
        private AliasProxyCommand(ProxyCommand p) {
            super(p.c);
            this.p = p;
        }
        
        public ProxyCommand get() {
        	return p;
        }

        @Override
        public CommandCategory category() {
            return null;
        }
    }

    enum CommandDisableLevel {
        NONE("None"),
        CATEGORY("Disabled category on server"),
        SPECIFIC_CATEGORY("Disabled category on specific channel"),
        COMMAND("Disabled command"),
        COMMAND_SPECIFIC("Disabled command on specific channel"),
        GUILD("Disabled command on this server"),
        ROLE("Disabled role on this server"),
        ROLE_CATEGORY("Disabled role for this category in this server"),
        SPECIFIC_ROLE("Disabled role for this command in this server"),
        SPECIFIC_ROLE_CATEGORY("Disabled role for this category in this server"),
        CHANNEL("Disabled channel"),
        USER("Disabled user");

        final String name;

        CommandDisableLevel(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

}
