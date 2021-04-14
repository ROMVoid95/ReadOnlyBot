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

package net.readonly.utils;

import java.awt.Color;
import java.time.OffsetDateTime;

import net.dv8tion.jda.api.EmbedBuilder;
import net.readonly.BotData;
import net.readonly.ReadOnlyBot;

public class Builders {

	private final static String AVATAR_URL = BotData.config().getAvatarUrl();
	private final static String BOT_NAME = BotData.config().getBotname();

	public static EmbedBuilder embed() {
		return embed(false);
	}

	public static EmbedBuilder embed(boolean error) {
		EmbedBuilder builder = new EmbedBuilder();

		if (error) {
			builder.setColor(new Color(235, 64, 52));
			builder.setTitle("An error occurred.");
		}

		builder.setTimestamp(OffsetDateTime.now());
		builder.setFooter("%s (%s)".formatted(BOT_NAME, ReadOnlyBot.VERSION), AVATAR_URL);

		return builder;
	}
}
