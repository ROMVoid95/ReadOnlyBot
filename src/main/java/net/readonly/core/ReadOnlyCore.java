package net.readonly.core;

import static net.readonly.core.LoadState.*;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.readonly.BotData;
import net.readonly.ReadOnlyBot;
import net.readonly.config.Config;
import net.readonly.core.command.processor.CommandProcessor;
import net.readonly.core.listener.BotListener;
import net.readonly.core.listener.event.LoadingEvent;
import net.readonly.core.modules.Module;
import net.readonly.logs.LogUtils;
import net.readonly.options.annotations.Optionable;
import net.readonly.options.event.OptionRegisterEvent;
import net.readonly.utils.DateFormatting;
import net.readonly.utils.exports.Metrics;

public class ReadOnlyCore {
    private static final Logger log = LoggerFactory.getLogger(ReadOnlyCore.class);
    
    private final ExecutorService threadPool = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("ReadOnly Thread-%d").build()
    );
    
    private static LoadState loadState = PRELOAD;
    private final Config config;
    @SuppressWarnings("unused")
	private final boolean isDebug;
    
    private String commandsPackage;
    private String optionsPackage;

    private final CommandProcessor commandProcessor = new CommandProcessor();
    private EventBus eventBus;
    private JDA jdaInstance;
    
    
    public ReadOnlyCore(Config config, boolean isDebug) {
        this.config = config;
        this.isDebug = isDebug;
        Metrics.THREAD_POOL_COLLECTOR.add("read-only-executor", threadPool);
    }
    
    public static boolean hasLoadedCompletely() {
        return getLoadState().equals(POSTLOAD);
    }

    public static LoadState getLoadState() {
        return loadState;
    }

    public static void setLoadState(LoadState loadState) {
    	ReadOnlyCore.loadState = loadState;
    }
    
    public ReadOnlyCore setOptionsPackage(String optionsPackage) {
        this.optionsPackage = optionsPackage;
        return this;
    }

    public ReadOnlyCore setCommandsPackage(String commandsPackage) {
        this.commandsPackage = commandsPackage;
        return this;
    }
    
    private void startInstance() {
    	var start = System.currentTimeMillis();
    	loadState = LOADING;

        var gatewayThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("GatewayThread-%d")
                .setDaemon(true)
                .setPriority(Thread.MAX_PRIORITY)
                .build();
        var requesterThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("RequesterThread-%d")
                .setDaemon(true)
                .build();
        
        try {
            // Don't allow mentioning @everyone, @here
            MessageAction.setDefaultMentions(EnumSet.of(Message.MentionType.EVERYONE, Message.MentionType.HERE));
            // Gateway Intents to enable.
            GatewayIntent[] toEnable = {
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.GUILD_MESSAGE_REACTIONS,
                    GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.GUILD_PRESENCES
            };

            log.info("Using intents {}", Arrays.stream(toEnable)
                    .map(Enum::name)
                    .collect(Collectors.joining(", "))
            );
            
            var startListener = new StartListener();
            
            var jdaInstance = JDABuilder.create(config.getToken(), Arrays.asList(toEnable))
            		.setChunkingFilter(ChunkingFilter.NONE)
            		.addEventListeners(
            				new BotListener(commandProcessor, threadPool),
            				startListener
            		)
					.disableCache(EnumSet.of(CacheFlag.ACTIVITY, CacheFlag.EMOTE, CacheFlag.CLIENT_STATUS, CacheFlag.VOICE_STATE))
					.setActivity(Activity.playing("Hold on to your seatbelts!"));
            
            var gatewayThreads = Math.max(1, 1 / 16);
            var rateLimitThreads = Math.max(2, 1 * 5 / 4);

            log.info("Gateway pool: {} threads", gatewayThreads);
            log.info("Rate limit pool: {} threads", rateLimitThreads);
            
            jdaInstance.setGatewayPool(Executors.newScheduledThreadPool(gatewayThreads, gatewayThreadFactory), true);
            jdaInstance.setRateLimitPool(Executors.newScheduledThreadPool(rateLimitThreads, requesterThreadFactory), true);
            		
            this.jdaInstance = jdaInstance.build();
            
            var elapsed = System.currentTimeMillis() - start;
            
            startPostLoadProcedure(elapsed);
        } catch (LoginException e) {
            throw new IllegalStateException(e);
        }
        loadState = LOADED;
    }
    
    public void start() {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null!");
        }

        if (commandsPackage == null) {
            throw new IllegalArgumentException("Cannot look for commands if you don't specify where!");
        }
        
        if (optionsPackage == null) {
            throw new IllegalArgumentException("Cannot look for options if you don't specify where!");
        }

        var commands = lookForAnnotatedOn(commandsPackage, Module.class);
        var options = lookForAnnotatedOn(optionsPackage, Optionable.class);
        
        eventBus = new EventBus();
        
        startInstance();
        
        for (var commandClass : commands) {
            try {
            	eventBus.register(commandClass.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                log.error("Invalid module: no zero arg public constructor found for " + commandClass);
            }
        }
        
        for (var optionClass : options) {
            try {
            	log.info("Constructor: %s".formatted(optionClass.getDeclaredConstructor().toGenericString()));
            	eventBus.register(optionClass.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
            	e.printStackTrace();
                log.error("Invalid module: no zero arg public constructor found for " + optionClass);
            }
        }
        
        new Thread(() -> {
        	eventBus.post(new LoadingEvent.Pre());
        	
            log.info("Registering all commands (@Module)");
            eventBus.post(CommandProcessor.REGISTRY);
            log.info("Registered all commands (@Module)");
            
            log.info("Registering all options (@Option)");
            eventBus.post(new OptionRegisterEvent());
            log.info("Registered all options (@Option)");
        }, "Event-Bus-Post").start();
    }
    
    public void markAsReady() {
        loadState = POSTLOAD;
    }
    
    public JDA jda() {
    	return this.jdaInstance;
    }
    
    public EventBus getEventBus() {
        return this.eventBus;
    }
    
    private Set<Class<?>> lookForAnnotatedOn(String packageName, Class<? extends Annotation> annotation) {
        return new ClassGraph()
                .acceptPackages(packageName)
                .enableAnnotationInfo()
                .scan(2)
                .getAllClasses().stream().filter(classInfo -> classInfo.hasAnnotation(annotation.getName())).map(ClassInfo::loadClass)
                .collect(Collectors.toSet());
    }
    
    private void startPostLoadProcedure(long elapsed) {
        var bot = ReadOnlyBot.instance();

        // Start the reconnect queue.
        bot.getCore().markAsReady();

        // Get the amount of clusters
        int clusterTotal = 1;
        try(var jedis = BotData.getDefaultJedisPool().getResource()) {
            var clusters = jedis.hgetAll("node-stats-" + config.getClientId());
            clusterTotal = clusters.size();
        }

        log.info("Not aware of anything holding off boot now, considering bot as started");
        LogUtils.log(
                """
                Loaded %d commands.
                Took %s to start. Total nodes: %d.""".formatted(
                        CommandProcessor.REGISTRY.commands().size(),
                        DateFormatting.formatDuration(elapsed), clusterTotal
                )
        );

        log.info("Loaded successfully! Current status: {}", ReadOnlyCore.getLoadState());

        log.info("Firing PostLoadEvent...");
        bot.getCore().getEventBus().post(new LoadingEvent.Post());

        startUpdaters();
    }
    
    private void startUpdaters() {
        log.info("Starting list count executor...");
        Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "Server Count Update")).scheduleAtFixedRate(() -> {
            try {
                var serverCount = 0L;
                //Fetch actual guild count.
                try(var jedis = BotData.getDefaultJedisPool().getResource()) {
                    var stats = jedis.hgetAll("stats-" + config.getClientId());
                    for (var g : stats.entrySet()) {
                        var json = new JSONObject(g.getValue());
                        serverCount += json.getLong("guild_count");
                    }
                }

                log.debug("Updated server count ({})", serverCount);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 0, 10, TimeUnit.MINUTES);
    }
    
    private static class StartListener implements EventListener {

        @Override
        public void onEvent(@Nonnull GenericEvent event) {
            if (event instanceof ReadyEvent) {
                var sm = event.getJDA();
                if (sm == null) { // We have a big problem if this happens.
                    throw new AssertionError();
                }
            }
        }
    }
}
