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

import net.readonly.core.modules.commands.base.AssistedCommand;
import net.readonly.core.modules.commands.base.CommandPermission;
import net.readonly.core.modules.commands.base.Context;
import net.readonly.core.modules.commands.base.InnerCommand;

public abstract class SubCommand implements InnerCommand, AssistedCommand {
    public boolean child;

    private CommandPermission permission = null;

    public SubCommand() {
    }

    public SubCommand(CommandPermission permission) {
        this.permission = permission;
    }

    /**
     * Creates a copy of a SubCommand, usually to assign child status to it.
     *
     * @param original The original SubCommand to copy.
     * @return The copy of the original SubCommand, without the description.
     */
    public static SubCommand copy(SubCommand original) {
        return new SubCommand(original.permission) {
            @Override
            protected void call(Context ctx, String content) {
                original.call(ctx, content);
            }

            @Override
            public String description() {
                return null;
            }
        };
    }

    protected abstract void call(Context ctx, String content);

    @Override
    public CommandPermission permission() {
        return permission;
    }

    @Override
    public void run(Context ctx, String commandName, String content) {
        call(ctx, content);
    }

    @Override
	public boolean isChild() {
        return this.child;
    }

    public void setChild(boolean child) {
        this.child = child;
    }
}
