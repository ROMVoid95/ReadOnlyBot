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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.readonly.utils.StringUtils;

public enum CommandCategory {
    ACTION(CommandPermission.USER, "categories.action", "Action"),
    MODERATION(CommandPermission.ADMIN, "categories.moderation", "Moderation"),
    OWNER(CommandPermission.OWNER, "categories.owner", "Owner"),
    INFO(CommandPermission.USER, "categories.info", "Info"),
    UTILS(CommandPermission.USER, "categories.utils", "Utility"),
    MISC(CommandPermission.USER, "categories.misc", "Misc");

    public final CommandPermission permission;
    private final String s;
    private final String qualifiedName;

    CommandCategory(CommandPermission p, String s, String name) {
        this.permission = p;
        this.s = s;
        this.qualifiedName = name;
    }

    /**
     * Looks up the Category based on a String value, if nothing is found returns null.
     * *
     *
     * @param name The String value to match
     * @return The category, or null if nothing is found.
     */
    public static CommandCategory lookupFromString(String name) {
        for (CommandCategory cat : CommandCategory.values()) {
            if (cat.qualifiedName.equalsIgnoreCase(name)) {
                return cat;
            }
        }
        return null;
    }

    /**
     * @return The name of the category.
     */
    public static List<String> getAllNames() {
        return Stream.of(CommandCategory.values()).map(category -> StringUtils.capitalize(category.qualifiedName.toLowerCase())).collect(Collectors.toList());
    }

    /**
     * @return All categories as a List. You could do Category#values anyway, this is just for my convenience.
     */
    public static List<CommandCategory> getAllCategories() {
        return Arrays.asList(CommandCategory.values());
    }

    @Override
    public String toString() {
        return s;
    }
}
