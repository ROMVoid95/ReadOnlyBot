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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.readonly.core.modules.commands.base.Context;
import net.readonly.options.OptionController;
import net.readonly.options.base.Option;
import net.readonly.options.base.OptionSection;

public class Builders {

	public static class OptionsBuilder {
		public List<MessageEmbed> embeds = new ArrayList<>();
		private List<OptionSection> sections = Arrays.asList(OptionSection.values());
		
		public OptionsBuilder(Context ctx) {
			var lctx = ctx.getLanguageContext();
			EmbedBuilder builder;
			
			builder = new EmbedBuilder()
					.setTitle(lctx.get("commands.opt.list.header"))
					.setDescription(lctx.get("commands.opt.list.description"));
			embeds.add(builder.build());
			
			for(OptionSection section : sections) {
				builder = new EmbedBuilder()
				.setTitle("---------- Section: " + section.toString()  + " ----------");
				for(Option opt : OptionController.getAvaliableOptions()) {
					if(opt.getSection().equals(section)) {
						builder.addField(opt.getAsField());
					}
				}
				embeds.add(builder.build());
			}
		}
	}
}
