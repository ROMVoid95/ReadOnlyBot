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

import static com.rethinkdb.RethinkDB.r;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.rethinkdb.net.Connection;

import lombok.extern.slf4j.Slf4j;
import net.readonly.config.Config;
import net.readonly.database.ManagedDatabase;
import net.readonly.utils.data.JsonDataManager;
import redis.clients.jedis.JedisPool;

@Slf4j
public class BotData {
	
    private static final ScheduledExecutorService exec = Executors.newScheduledThreadPool(
            1, new ThreadFactoryBuilder().setNameFormat("ReadOnly-Executor Thread-%d").build()
    );
	
	private static JsonDataManager<Config> config;
    private static Connection connection;
    private static ManagedDatabase db;

	private static final JedisPool defaultJedisPool = new JedisPool(conf().get().getJedisPoolAddress(), conf().get().getJedisPoolPort());
	
    private static JsonDataManager<Config> conf() {
        if (config == null) {
            config = new JsonDataManager<>(Config.class, "config.json", Config::new);
        }

        return config;
    }
    
    public static Config config() {
    	return conf().get();
    }
    
    public static JsonDataManager<Config> configManager() {
    	return conf();
    }
    
    public static Connection conn() {
        var config = config();
        if (connection == null) {
            synchronized (BotData.class) {
                if (connection != null) {
                    return connection;
                }

                connection = r.connection()
                        .hostname(config.getDatabase().getHostname())
                        .port(config.getDatabase().getPort())
                        .db(config.getDatabase().getDatabaseName())
                        .user(config.getDatabase().getUser(), config.getDatabase().getPassword())
                        .connect();

                log.info("Established first database connection to {}:{} ({})",
                        config.getDatabase().getHostname(), config.getDatabase().getPort(), config.getDatabase().getUser()
                );
            }
        }

        return connection;
    }

    public static ManagedDatabase db() {
        if (db == null) {
            db = new ManagedDatabase(conn());
        }

        return db;
    }
    
    public static ScheduledExecutorService getExecutor() {
        return exec;
    }

    public static void queue(Callable<?> action) {
        getExecutor().submit(action);
    }

    public static void queue(Runnable runnable) {
        getExecutor().submit(runnable);
    }

    public static JedisPool getDefaultJedisPool() {
        return BotData.defaultJedisPool;
    }
}
