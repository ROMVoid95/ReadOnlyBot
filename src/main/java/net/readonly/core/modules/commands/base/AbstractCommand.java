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

import java.util.ArrayList;
import java.util.List;

import net.readonly.core.modules.commands.help.HelpContent;

public abstract class AbstractCommand implements AssistedCommand {
    private final CommandCategory category;
    private final CommandPermission permission;
    public final List<String> aliases = new ArrayList<>();

    public AbstractCommand(CommandCategory category) {
        this(category, CommandPermission.USER);
    }

    public AbstractCommand(CommandCategory category, CommandPermission permission) {
        this.category = category;
        this.permission = permission;
    }

    @Override
    public CommandCategory category() {
        return category;
    }

    @Override
    public CommandPermission permission() {
        return permission;
    }

    //defaults to empty for now
    @Override
    public HelpContent help() {
        return new HelpContent.Builder().build();
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }
}
