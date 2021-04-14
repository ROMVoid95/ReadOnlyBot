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

package net.readonly.core.modules.commands.help;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.readonly.BotData;

public class HelpContent {
    private final String description;
    private final Map<String, String> parameters;
    private final String usage;
    private final List<String> related;
    private final boolean seasonal;
    private final List<String> descriptionList;

    public HelpContent(String description, Map<String, String> parameters, String usage, List<String> related, List<String> descriptionList, boolean seasonal) {
        this.description = description;
        this.parameters = parameters;
        this.usage = usage;
        this.related = related;
        this.seasonal = seasonal;
        this.descriptionList = descriptionList;
    }

    public String getDescription() {
        return this.description;
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public String getUsage() {
        return this.usage;
    }

    public List<String> getRelated() {
        return this.related;
    }

    public List<String> getDescriptionList() {
        return descriptionList;
    }

    public boolean isSeasonal() {
        return this.seasonal;
    }

    public static class Builder {
        private String description = null;
        private final Map<String, String> parameters = new HashMap<>();
        private String usage = null;
        private List<String> related = new ArrayList<>();
        private boolean seasonal = false;
        private List<String> descriptionList = new ArrayList<>();

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder addParameter(String parameterName, String content) {
            parameters.put(parameterName, content);
            return this;
        }

        //I was lazy to make last one take a boolean bc that'd mean replacing existing ones, bleh.
        public Builder addParameterOptional(String parameterName, String content) {
            parameters.put(parameterName, content + " This is optional");
            return this;
        }

        public Builder setUsage(String usage) {
            this.usage = "`" + BotData.config().getPrefix() + usage + "`";
            return this;
        }

        public Builder setRelated(List<String> related) {
            this.related = related;
            return this;
        }

        public Builder setDescriptionList(List<String> descriptionList) {
            this.descriptionList = descriptionList;
            return this;
        }

        public Builder setSeasonal(boolean seasonal) {
            this.seasonal = seasonal;
            return this;
        }

        public HelpContent build() {
            return new HelpContent(description, parameters, usage, related, descriptionList, seasonal);
        }
    }
}
