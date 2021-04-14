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

import net.readonly.core.modules.commands.base.AbstractCommand;
import net.readonly.core.modules.commands.base.CommandCategory;
import net.readonly.core.modules.commands.base.CommandPermission;
import net.readonly.core.modules.commands.base.Context;

public abstract class NoArgsCommand extends AbstractCommand {
    public NoArgsCommand(CommandCategory category) {
        super(category);
    }

    public NoArgsCommand(CommandCategory category, CommandPermission permission) {
        super(category, permission);
    }

    protected abstract void call(Context context, String content);

    @Override
    public void run(Context context, String commandName, String content) {
        call(context, content);
    }
}
