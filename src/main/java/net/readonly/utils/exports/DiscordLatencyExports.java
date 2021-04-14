/*
 * Copyright (C) 2016-2021 David Rubio Escares / Kodehawa
 *
 *  Mantaro is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  Mantaro is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mantaro. If not, see http://www.gnu.org/licenses/
 */

package net.readonly.utils.exports;

import java.util.concurrent.TimeUnit;

import io.prometheus.client.Gauge;
import net.dv8tion.jda.api.JDA;
import net.readonly.ReadOnlyBot;
import net.readonly.utils.Prometheus;

public class DiscordLatencyExports {
    private static final double MILLISECONDS_PER_SECOND = 1000;

    private static final Gauge GATEWAY_LATENCY = Gauge.build()
            .name("gateway_latency")
            .help("Gateway latency in seconds")
            .create();
    private static final Gauge REST_LATENCY = Gauge.build()
            .name("rest_latency")
            .help("Rest latency in seconds")
            .create();

    public static void register() {
        GATEWAY_LATENCY.register();
        REST_LATENCY.register();

        ReadOnlyBot.instance().getExecutorService().scheduleAtFixedRate(() -> {
        	JDA jda = ReadOnlyBot.instance().getJDA();
        	
        	var gatewayPing = jda.getGatewayPing();
            if (gatewayPing >= 0) {
                GATEWAY_LATENCY.labels(String.valueOf(jda.getSelfUser().getId()))
                        .set(gatewayPing / MILLISECONDS_PER_SECOND);
            }
            jda.getRestPing().queue(restPing -> REST_LATENCY.set(restPing / MILLISECONDS_PER_SECOND));

        }, 0, Prometheus.UPDATE_PERIOD.toMillis(), TimeUnit.MILLISECONDS);
    }
}
