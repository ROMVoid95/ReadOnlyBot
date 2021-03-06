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

package net.readonly.core.modules.commands.base;

import java.util.List;

import net.readonly.core.modules.commands.help.HelpContent;

/**
 * Interface used for handling commands within the bot.
 */
public interface Command {
    /**
     * The Command's {@link CommandCategory}
     *
     * @return a Nullable {@link CommandCategory}. Null means that the command should be hidden from Help.
     */
    CommandCategory category();

    CommandPermission permission();

    /**
     * Invokes the command to be executed.
     *
     * @param context     the context of the event that triggered the command
     * @param commandName the command name that was used
     * @param content     the arguments of the command
     */
    void run(Context context, String commandName, String content);

    HelpContent help();

    List<String> getAliases();

    default boolean isOwnerCommand() {
        return permission() == CommandPermission.OWNER;
    }
}
