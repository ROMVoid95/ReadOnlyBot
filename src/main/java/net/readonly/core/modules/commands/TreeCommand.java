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

public abstract class TreeCommand extends AbstractCommand implements ITreeCommand {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    //By default let all commands pass.
    private Predicate<Context> predicate = event -> true;

    public TreeCommand(CommandCategory category) {
        super(category);
    }

    public TreeCommand(CommandCategory category, CommandPermission permission) {
        super(category, permission);
    }

    @Override
    public void run(Context context, String commandName, String content) {
        String[] args = splitArgs(content, 2);

        if (subCommands.isEmpty()) {
            throw new IllegalArgumentException("No subcommands registered!");
        }

        Command command = subCommands.get(args[0]);
        boolean isDefault = false;
        if (command == null) {
            command = defaultTrigger(context, commandName, content);
            isDefault = true;
        }
        if (command == null)
            return; //Use SimpleTreeCommand then?

        var ct = isDefault ? content : args[1];

        if (!predicate.test(context)) return;

        command.run(new Context(context.getEvent(), context.getLanguageContext(), ct, context.isMentionPrefix()),
                commandName + (isDefault ? "" : " " + args[0]), ct
        );
    }

    public TreeCommand addSubCommand(String name, BiConsumer<Context, String> command) {
        subCommands.put(name, new SubCommand() {
            @Override
            protected void call(Context context, String content) {
                command.accept(context, content);
            }
        });
        return this;
    }

    public void setPredicate(Predicate<Context> predicate) {
        this.predicate = predicate;
    }

    @Override
    public TreeCommand createSubCommandAlias(String name, String alias) {
        SubCommand cmd = subCommands.get(name);
        if (cmd == null) {
            throw new IllegalArgumentException("Cannot create an alias of a non-existent sub command!");
        }

        //Creates a fully new instance. Without this, it'd be dependant on the original instance, and changing the child status would change it's parent's status too.
        SubCommand clone = SubCommand.copy(cmd);
        clone.setChild(true);
        subCommands.put(alias, clone);

        return this;
    }

    @Override
    public ITreeCommand addSubCommand(String name, SubCommand command) {
        subCommands.put(name, command);
        return this;
    }

    @Override
    public Map<String, SubCommand> getSubCommands() {
        return subCommands;
    }
}
