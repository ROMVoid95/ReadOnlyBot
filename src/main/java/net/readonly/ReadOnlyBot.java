/*
 * MIT License 
 *
 * Copyright (c) 2021 ReadOnly Development
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

package net.readonly;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import net.readonly.config.Config;
import net.readonly.core.ReadOnlyCore;
import net.readonly.logs.LogFilter;
import net.readonly.logs.LogUtils;
import net.readonly.utils.Prometheus;
import net.readonly.utils.RuntimeOptions;
import net.readonly.utils.TracingPrintStream;

public class ReadOnlyBot {
	private static final Logger log = LoggerFactory.getLogger(ReadOnlyBot.class);
	private static ReadOnlyBot instance;
	
	private final static Config config = BotData.config();
	
    public static final String GITHUB_URL = "https://github.com/ROMVoid95/ReadOnlyDiscordBot";
    public static final String USER_AGENT = "%s/@version@/DiscordBot (%s)".formatted(config.getBotname(), GITHUB_URL);
    public static final String VERSION = "@version@";
    public static final String GIT_REVISION = "@revision@";

	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3,
			new ThreadFactoryBuilder().setNameFormat("ReadOnly Scheduled Executor Thread-%d").build());

	private JDA jda;

	private final ReadOnlyCore core;
	
	private void preStart() {
        log.info("Starting up %s {}, Git revision: {}", config.getBotname(), VERSION, GIT_REVISION);
        log.info("Reporting UA {} for HTTP requests.", USER_AGENT);
        
        if(RuntimeOptions.VERBOSE) {
            System.setOut(new TracingPrintStream(System.out));
            System.setErr(new TracingPrintStream(System.err));
        }
        
        RestAction.setPassContext(true);
        if (RuntimeOptions.DEBUG) {
            log.info("Running in debug mode!");
        } else {
            RestAction.setDefaultFailure(ErrorResponseException.ignore(
                    RestAction.getDefaultFailure(),
                    ErrorResponse.UNKNOWN_MESSAGE
            ));
        }

        log.info("Filtering all logs below {}", LogFilter.LEVEL);
	}

	private ReadOnlyBot() throws Exception {
		instance = this;
		core = new ReadOnlyCore(config, false);
		preStart();
		
        LogUtils.log("Startup", "Starting up %s %s (Git: %s)".formatted(config.getBotname(), VERSION, GIT_REVISION));

		core.setCommandsPackage("net.readonly.commands")
			.setOptionsPackage("net.readonly.options")
			.start();

		BotData.configManager().save();
		this.startExecutors();
		
		this.jda = core.jda();
	}

	public static void main(String[] args) {
		try {
			Prometheus.enable();
		} catch (Exception e) {
			log.error("Unable to start prometheus client!", e);
		}

		try {
			new ReadOnlyBot();
		} catch (Exception e) {
			log.error("Could not complete Main Thread routine!", e);
			log.error("Cannot continue! Exiting program...");
			System.exit(1);
		}

		Prometheus.registerPostStartup();
	}

	private void startExecutors() {
		log.info("Starting executors...");
		// Handle posting statistics.
		ScheduledExecutorService postExecutor = Executors.newSingleThreadScheduledExecutor(
				new ThreadFactoryBuilder().setNameFormat("Statistics Posting").build());
		postExecutor.scheduleAtFixedRate(() -> postStats(getJDA()), 10, 20, TimeUnit.MINUTES);
	}

	private void postStats(JDA jda) {
		if (jda.getStatus() == JDA.Status.INITIALIZED || jda.getStatus() == JDA.Status.SHUTDOWN) {
			return;
		}

		try (var jedis = BotData.getDefaultJedisPool().getResource()) {
			var json = new JSONObject().put("guild_count", jda.getGuildCache().size())
					.put("users", jda.getUserCache().size())
					.put("gateway_ping", jda.getGatewayPing())
					.put("status", jda.getStatus())
					.toString();

			jedis.hset("shardstats-" + jda.getSelfUser().getId(), String.valueOf(jda.getSelfUser().getId()), json);
		}
	}

	public static ReadOnlyBot instance() {
		return ReadOnlyBot.instance;
	}
	
	public ReadOnlyCore getCore() {
		return this.core;
	}

	public JDA getJDA() {
		return jda;
	}
	
	public SelfUser getBotUser() {
		return getJDA().getSelfUser();
	}

	public ScheduledExecutorService getExecutorService() {
		return this.executorService;
	}
}
