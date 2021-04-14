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

import java.util.List;

import net.readonly.core.modules.commands.base.Command;
import net.readonly.core.modules.commands.base.CommandCategory;
import net.readonly.core.modules.commands.base.CommandPermission;
import net.readonly.core.modules.commands.base.Context;
import net.readonly.core.modules.commands.help.HelpContent;

public class AliasCommand implements Command {
    private final Command command;
    private final String commandName;
    private final String originalName;
    private final List<String> aliases;

    public AliasCommand(String commandName, String originalName, Command command) {
        this.commandName = commandName;
        this.command = command;
        this.originalName = originalName;
        this.aliases = command.getAliases();
    }

    public CommandCategory parentCategory() {
        return command.category();
    }

    public String parentName() {
        return originalName;
    }

    @Override
    public CommandCategory category() {
        return null; //Alias Commands are hidden
    }

    @Override
    public CommandPermission permission() {
        return command.permission();
    }

    @Override
    public void run(Context context, String ignored, String content) {
        command.run(context, commandName, content);
    }

    @Override
    public HelpContent help() {
        return command.help();
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    public Command getCommand() {
        return this.command;
    }

    public String getCommandName() {
        return this.commandName;
    }

    public String getOriginalName() {
        return this.originalName;
    }
}
