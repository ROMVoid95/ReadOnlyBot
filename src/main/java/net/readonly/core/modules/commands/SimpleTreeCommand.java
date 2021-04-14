/*
 * Copyright (c) 2021 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.readonly.core.modules.commands;

import static net.readonly.utils.StringUtils.splitArgs;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import net.readonly.core.modules.commands.base.AbstractCommand;
import net.readonly.core.modules.commands.base.Command;
import net.readonly.core.modules.commands.base.CommandCategory;
import net.readonly.core.modules.commands.base.CommandPermission;
import net.readonly.core.modules.commands.base.Context;
import net.readonly.core.modules.commands.base.ITreeCommand;
import net.readonly.utils.EmoteReference;

public abstract class SimpleTreeCommand extends AbstractCommand implements ITreeCommand {
    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private Predicate<Context> predicate = event -> true;

    public SimpleTreeCommand(CommandCategory category) {
        super(category);
    }

    public SimpleTreeCommand(CommandCategory category, CommandPermission permission) {
        super(category, permission);
    }

    /**
     * Invokes the command to be executed.
     *
     * @param context     the context of the event that triggered the command
     * @param commandName the command name that was used
     * @param content     the arguments of the command
     */
    @Override
    public void run(Context context, String commandName, String content) {
        var args = splitArgs(content, 2);

        if (subCommands.isEmpty()) {
            throw new IllegalArgumentException("No subcommands registered!");
        }

        var command= subCommands.get(args[0]);

        if (command == null) {
            defaultTrigger(context, commandName, args[0]);
            return;
        }

        if (!predicate.test(context)) {
            return;
        }

        command.run(new Context(context.getEvent(), context.getLanguageContext(), args[1], context.isMentionPrefix()), commandName + " " + args[0], args[1]);
    }

    public void setPredicate(Predicate<Context> predicate) {
        this.predicate = predicate;
    }

    public SimpleTreeCommand addSubCommand(String name, String description, BiConsumer<Context, String> command) {
        subCommands.put(name, new SubCommand() {
            @Override
            public String description() {
                return description;
            }

            @Override
            protected void call(Context context, String content) {
                command.accept(context, content);
            }
        });

        return this;
    }

    public SimpleTreeCommand addSubCommand(String name, BiConsumer<Context, String> command) {
        return addSubCommand(name, null, command);
    }

    @Override
	public SimpleTreeCommand addSubCommand(String name, SubCommand command) {
        subCommands.put(name, command);
        return this;
    }

    @Override
    public SimpleTreeCommand createSubCommandAlias(String name, String alias) {
        var cmd = subCommands.get(name);
        if (cmd == null) {
            throw new IllegalArgumentException("Cannot create an alias of a non-existent sub command!");
        }

        //Creates a fully new instance. Without this, it'd be dependant on the original instance, and changing the child status would change it's parent's status too.
        var clone = SubCommand.copy(cmd);
        clone.setChild(true);
        subCommands.put(alias, clone);

        return this;
    }

    @Override
    public Map<String, SubCommand> getSubCommands() {
        return subCommands;
    }

    /**
     * Handling for when the Sub-Command isn't found.
     *
     * @param ctx         the context of the event that triggered the command
     * @param commandName the Name of the not-found command.
     */
    @Override
	public Command defaultTrigger(Context ctx, String mainCommand, String commandName) {
        ctx.sendStripped(String.format("%1$sI didn't find this subcommand. Check `~>help %2$s` for a list of available subcommands", EmoteReference.ERROR, mainCommand));
        return null;
    }
}
