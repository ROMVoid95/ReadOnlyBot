package net.readonly.utils.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.readonly.utils.FinderUtil;

public class Finder {

	private final static Pattern DISCORD_ID = Pattern.compile("\\d{17,20}"); // ID
	private final static Pattern FULL_USER_REF = Pattern.compile("(\\S.{0,30}\\S)\\s*#(\\d{4})"); // $1 -> username, $2
																									// -> discriminator
	private final static Pattern USER_MENTION = Pattern.compile("<@!?(\\d{17,20})>"); // $1 -> ID
	private final static Pattern CHANNEL_MENTION = Pattern.compile("<#(\\d{17,20})>"); // $1 -> ID
	private final static Pattern ROLE_MENTION = Pattern.compile("<@&(\\d{17,20})>"); // $1 -> ID
	private final static Pattern EMOTE_MENTION = Pattern.compile("<:(.{2,32}):(\\d{17,20})>");

	public static class JDAUser {
		/**
		 * Queries a provided instance of {@link net.dv8tion.jda.api.JDA JDA} for
		 * {@link net.dv8tion.jda.api.entities.User User}s.
		 * <p>
		 *
		 * If a {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} is
		 * available this will query across that instead of the JDA instance.
		 *
		 * <p>
		 * The following special cases are applied in order of listing before the
		 * standard search is done:
		 * <ul>
		 * <li>User Mention: Query provided matches an @user mention (more specifically
		 * {@literal <@userID>}).</li>
		 * <li>Full User Reference: Query provided matches a full Username#XXXX
		 * reference. <br>
		 * <b>NOTE:</b> this can return a list with more than one entity.</li>
		 * </ul>
		 *
		 * @param query The String query to search by
		 * @param jda   The instance of JDA to search from
		 *
		 * @return A possibly-empty {@link java.util.List List} of Users found by the
		 *         query from the provided JDA instance.
		 */
		public static List<User> findUsers(String query, JDA jda) {
			return jdaUserSearch(query, jda, true);
		}

		/**
		 * Queries a provided instance of {@link net.dv8tion.jda.api.JDA JDA} for
		 * {@link net.dv8tion.jda.api.entities.User User}s.
		 * <p>
		 *
		 * This only queries the instance of JDA, regardless of whether or not a
		 * {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} is available.
		 *
		 * <p>
		 * The following special cases are applied in order of listing before the
		 * standard search is done:
		 * <ul>
		 * <li>User Mention: Query provided matches an @user mention (more specifically
		 * {@literal <@userID>}).</li>
		 * <li>Full User Reference: Query provided matches a full Username#XXXX
		 * reference. <br>
		 * <b>NOTE:</b> this can return a list with more than one entity.</li>
		 * </ul>
		 *
		 * @param query The String query to search by
		 * @param jda   The instance of JDA to search from
		 *
		 * @return A possibly-empty {@link java.util.List List} of Users found by the
		 *         query from the provided JDA instance.
		 */
		public static List<User> findShardUsers(String query, JDA jda) {
			return jdaUserSearch(query, jda, false);
		}

		private static List<User> jdaUserSearch(String query, JDA jda, boolean useShardManager) {
			Matcher userMention = USER_MENTION.matcher(query);
			Matcher fullRefMatch = FULL_USER_REF.matcher(query);

			ShardManager manager = useShardManager ? jda.getShardManager() : null;

			if (userMention.matches()) {
				User user = manager != null ? manager.getUserById(userMention.group(1))
						: jda.getUserById(userMention.group(1));
				if (user != null)
					return Collections.singletonList(user);
			} else if (fullRefMatch.matches()) {
				String lowerName = fullRefMatch.group(1).toLowerCase();
				String discrim = fullRefMatch.group(2);
				List<User> users = (manager != null ? manager.getUserCache() : jda.getUserCache()).stream()
						.filter(user -> user.getName().toLowerCase().equals(lowerName)
								&& user.getDiscriminator().equals(discrim))
						.collect(Collectors.toList());
				if (!users.isEmpty())
					return users;
			} else if (DISCORD_ID.matcher(query).matches()) {
				User user = (manager != null ? manager.getUserById(query) : jda.getUserById(query));
				if (user != null)
					return Collections.singletonList(user);
			}

			ArrayList<User> exact = new ArrayList<>();
			ArrayList<User> wrongcase = new ArrayList<>();
			ArrayList<User> startswith = new ArrayList<>();
			ArrayList<User> contains = new ArrayList<>();
			String lowerquery = query.toLowerCase();
			(manager != null ? manager.getUserCache() : jda.getUserCache()).forEach(user -> {
				String name = user.getName();
				if (name.equals(query))
					exact.add(user);
				else if (name.equalsIgnoreCase(query) && exact.isEmpty())
					wrongcase.add(user);
				else if (name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
					startswith.add(user);
				else if (name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
					contains.add(user);
			});
			if (!exact.isEmpty())
				return Collections.unmodifiableList(exact);
			if (!wrongcase.isEmpty())
				return Collections.unmodifiableList(wrongcase);
			if (!startswith.isEmpty())
				return Collections.unmodifiableList(startswith);
			return Collections.unmodifiableList(contains);
		}
	}

	public static class GuildMember {

		/**
		 * Queries a provided {@link net.dv8tion.jda.api.entities.Guild Guild} for
		 * {@link net.dv8tion.jda.api.entities.Member Member}s.
		 *
		 * <p>
		 * The following special cases are applied in order of listing before the
		 * standard search is done:
		 * <ul>
		 * <li>User Mention: Query provided matches an @user mention (more specifically
		 * {@literal <@userID> or <@!userID>}).</li>
		 * <li>Full User Reference: Query provided matches a full Username#XXXX
		 * reference. <br>
		 * <b>NOTE:</b> this can return a list with more than one entity.</li>
		 * </ul>
		 *
		 * <p>
		 * Unlike {@link FinderUtil#findUsers(String, JDA) FinderUtil.findUsers(String,
		 * JDA)}, this method queries based on two different names: user name and
		 * effective name (excluding special cases in which it queries solely based on
		 * user name). <br>
		 * Each standard check looks at the user name, then the member name, and if
		 * either one's criteria is met the Member is added to the returned list. This
		 * is important to note, because the returned list may contain exact matches for
		 * User's name as well as exact matches for a Member's effective name, with
		 * nothing guaranteeing the returns will be exclusively containing matches for
		 * one or the other. <br>
		 * Information on effective name can be found in
		 * {@link net.dv8tion.jda.api.entities.Member#getEffectiveName()
		 * Member#getEffectiveName()}.
		 *
		 * @param query The String query to search by
		 * @param guild The Guild to search from
		 *
		 * @return A possibly empty {@link java.util.List List} of Members found by the
		 *         query from the provided Guild.
		 */
		public static List<Member> findMembers(String query, Guild guild) {
			Matcher userMention = USER_MENTION.matcher(query);
			Matcher fullRefMatch = FULL_USER_REF.matcher(query);
			if (userMention.matches()) {
				Member member = guild.getMemberById(userMention.group(1));
				if (member != null)
					return Collections.singletonList(member);
			} else if (fullRefMatch.matches()) {
				String lowerName = fullRefMatch.group(1).toLowerCase();
				String discrim = fullRefMatch.group(2);
				List<Member> members = guild.getMemberCache().stream()
						.filter(member -> member.getUser().getName().toLowerCase().equals(lowerName)
								&& member.getUser().getDiscriminator().equals(discrim))
						.collect(Collectors.toList());
				if (!members.isEmpty())
					return members;
			} else if (DISCORD_ID.matcher(query).matches()) {
				Member member = guild.getMemberById(query);
				if (member != null)
					return Collections.singletonList(member);
			}
			ArrayList<Member> exact = new ArrayList<>();
			ArrayList<Member> wrongcase = new ArrayList<>();
			ArrayList<Member> startswith = new ArrayList<>();
			ArrayList<Member> contains = new ArrayList<>();
			String lowerquery = query.toLowerCase();
			guild.getMemberCache().forEach(member -> {
				String name = member.getUser().getName();
				String effName = member.getEffectiveName();
				if (name.equals(query) || effName.equals(query))
					exact.add(member);
				else if ((name.equalsIgnoreCase(query) || effName.equalsIgnoreCase(query)) && exact.isEmpty())
					wrongcase.add(member);
				else if ((name.toLowerCase().startsWith(lowerquery) || effName.toLowerCase().startsWith(lowerquery))
						&& wrongcase.isEmpty())
					startswith.add(member);
				else if ((name.toLowerCase().contains(lowerquery) || effName.toLowerCase().contains(lowerquery))
						&& startswith.isEmpty())
					contains.add(member);
			});
			if (!exact.isEmpty())
				return Collections.unmodifiableList(exact);
			if (!wrongcase.isEmpty())
				return Collections.unmodifiableList(wrongcase);
			if (!startswith.isEmpty())
				return Collections.unmodifiableList(startswith);
			return Collections.unmodifiableList(contains);
		}

	}

	public static class GuildTextChannel {
		/**
		 * Queries a provided instance of {@link net.dv8tion.jda.api.JDA JDA} for
		 * {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}s.
		 * <p>
		 *
		 * If a {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} is
		 * available this will query across that instead of the JDA instance.
		 *
		 * <p>
		 * The following special case is applied before the standard search is done:
		 * <ul>
		 * <li>Channel Mention: Query provided matches a #channel mention (more
		 * specifically {@literal <#channelID>})</li>
		 * </ul>
		 *
		 * @param query The String query to search by
		 * @param jda   The instance of JDA to search from
		 *
		 * @return A possibly-empty {@link java.util.List List} of TextChannels found by
		 *         the query from the provided JDA instance.
		 */
		public static List<TextChannel> findTextChannels(String query, JDA jda) {
			return jdaTextChannelSearch(query, jda, true);
		}

		/**
		 * Queries a provided instance of {@link net.dv8tion.jda.api.JDA JDA} for
		 * {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}s.
		 * <p>
		 *
		 * This only queries the instance of JDA, regardless of whether or not a
		 * {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} is available.
		 *
		 * <p>
		 * The following special case is applied before the standard search is done:
		 * <ul>
		 * <li>Channel Mention: Query provided matches a #channel mention (more
		 * specifically {@literal <#channelID>})</li>
		 * </ul>
		 *
		 * @param query The String query to search by
		 * @param jda   The instance of JDA to search from
		 *
		 * @return A possibly-empty {@link java.util.List List} of TextChannels found by
		 *         the query from the provided JDA instance.
		 */
		public static List<TextChannel> findShardTextChannels(String query, JDA jda) {
			return jdaTextChannelSearch(query, jda, false);
		}

		/**
		 * Queries a provided {@link net.dv8tion.jda.api.entities.Guild Guild} for
		 * {@link net.dv8tion.jda.api.entities.TextChannel TextChannel}s.
		 *
		 * <p>
		 * The following special case is applied before the standard search is done:
		 * <ul>
		 * <li>Channel Mention: Query provided matches a #channel mention (more
		 * specifically {@literal <#channelID>})</li>
		 * </ul>
		 *
		 * @param query The String query to search by
		 * @param guild The Guild to search from
		 *
		 * @return A possibly-empty {@link java.util.List List} of TextChannels found by
		 *         the query from the provided Guild.
		 */
		public static List<TextChannel> findTextChannels(String query, Guild guild) {
			Matcher channelMention = CHANNEL_MENTION.matcher(query);
			if (channelMention.matches()) {
				TextChannel tc = guild.getTextChannelById(channelMention.group(1));
				if (tc != null)
					return Collections.singletonList(tc);
			} else if (DISCORD_ID.matcher(query).matches()) {
				TextChannel tc = guild.getTextChannelById(query);
				if (tc != null)
					return Collections.singletonList(tc);
			}

			return genericTextChannelSearch(query, guild.getTextChannelCache());
		}

		private static List<TextChannel> jdaTextChannelSearch(String query, JDA jda, boolean useShardManager) {
			Matcher channelMention = CHANNEL_MENTION.matcher(query);

			ShardManager manager = useShardManager ? jda.getShardManager() : null;

			if (channelMention.matches()) {
				TextChannel tc = manager != null ? manager.getTextChannelById(channelMention.group(1))
						: jda.getTextChannelById(channelMention.group(1));
				if (tc != null)
					return Collections.singletonList(tc);
			} else if (DISCORD_ID.matcher(query).matches()) {
				TextChannel tc = manager != null ? manager.getTextChannelById(query) : jda.getTextChannelById(query);
				if (tc != null)
					return Collections.singletonList(tc);
			}

			return genericTextChannelSearch(query,
					manager != null ? manager.getTextChannelCache() : jda.getTextChannelCache());
		}

		private static List<TextChannel> genericTextChannelSearch(String query, SnowflakeCacheView<TextChannel> cache) {
			ArrayList<TextChannel> exact = new ArrayList<>();
			ArrayList<TextChannel> wrongcase = new ArrayList<>();
			ArrayList<TextChannel> startswith = new ArrayList<>();
			ArrayList<TextChannel> contains = new ArrayList<>();
			String lowerquery = query.toLowerCase();
			cache.forEach((tc) -> {
				String name = tc.getName();
				if (name.equals(query))
					exact.add(tc);
				else if (name.equalsIgnoreCase(query) && exact.isEmpty())
					wrongcase.add(tc);
				else if (name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
					startswith.add(tc);
				else if (name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
					contains.add(tc);
			});
			if (!exact.isEmpty())
				return Collections.unmodifiableList(exact);
			if (!wrongcase.isEmpty())
				return Collections.unmodifiableList(wrongcase);
			if (!startswith.isEmpty())
				return Collections.unmodifiableList(startswith);
			return Collections.unmodifiableList(contains);
		}
	}

	public static class GuildCategory {
	    /**
	     * Queries a provided instance of {@link net.dv8tion.jda.api.JDA JDA} for
	     * {@link net.dv8tion.jda.api.entities.Category Categories}.<p>
	     *
	     * If a {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} is available this will query across that
	     * instead of the JDA instance.
	     *
	     * <p>The standard search does not follow any special cases.
	     *
	     * @param  query
	     *         The String query to search by
	     * @param  jda
	     *         The instance of JDA to search from
	     *
	     * @return A possibly-empty {@link java.util.List List} of Categories found by the query from the provided JDA instance.
	     */
	    public static List<Category> findCategories(String query, JDA jda)
	    {
	        return jdaCategorySearch(query, jda, true);
	    }

	    /**
	     * Queries a provided instance of {@link net.dv8tion.jda.api.JDA JDA} for
	     * {@link net.dv8tion.jda.api.entities.Category Categories}.<p>
	     *
	     * This only queries the instance of JDA, regardless of whether or not a
	     * {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} is available.
	     *
	     * <p>The standard search does not follow any special cases.
	     *
	     * @param  query
	     *         The String query to search by
	     * @param  jda
	     *         The instance of JDA to search from
	     *
	     * @return A possibly-empty {@link java.util.List List} of Categories found by the query from the provided JDA instance.
	     */
	    public static List<Category> findShardCategories(String query, JDA jda)
	    {
	        return jdaCategorySearch(query, jda, false);
	    }

	    /**
	     * Queries a provided {@link net.dv8tion.jda.api.entities.Guild Guild} for
	     * {@link net.dv8tion.jda.api.entities.Category Categories}.
	     *
	     * <p>The standard search does not follow any special cases.
	     *
	     * @param  query
	     *         The String query to search by
	     * @param  guild
	     *         The Guild to search from
	     *
	     * @return A possibly-empty {@link java.util.List List} of Categories found by the query from the provided Guild.
	     */
	    public static List<Category> findCategories(String query, Guild guild)
	    {
	        if(DISCORD_ID.matcher(query).matches())
	        {
	            Category cat = guild.getCategoryById(query);
	            if(cat != null)
	                return Collections.singletonList(cat);
	        }

	        return genericCategorySearch(query, guild.getCategoryCache());
	    }

	    private static List<Category> jdaCategorySearch(String query, JDA jda, boolean useShardManager)
	    {
	        ShardManager manager = useShardManager? jda.getShardManager() : null;

	        if(DISCORD_ID.matcher(query).matches())
	        {
	            Category cat = manager != null? manager.getCategoryById(query) : jda.getCategoryById(query);
	            if(cat != null)
	                return Collections.singletonList(cat);
	        }

	        return genericCategorySearch(query, jda.getCategoryCache());
	    }

	    private static List<Category> genericCategorySearch(String query, SnowflakeCacheView<Category> cache)
	    {
	        ArrayList<Category> exact = new ArrayList<>();
	        ArrayList<Category> wrongcase = new ArrayList<>();
	        ArrayList<Category> startswith = new ArrayList<>();
	        ArrayList<Category> contains = new ArrayList<>();
	        String lowerquery = query.toLowerCase();
	        cache.forEach(cat -> {
	            String name = cat.getName();
	            if(name.equals(query))
	                exact.add(cat);
	            else if(name.equalsIgnoreCase(query) && exact.isEmpty())
	                wrongcase.add(cat);
	            else if(name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
	                startswith.add(cat);
	            else if(name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
	                contains.add(cat);
	        });
	        if(!exact.isEmpty())
	            return Collections.unmodifiableList(exact);
	        if(!wrongcase.isEmpty())
	            return Collections.unmodifiableList(wrongcase);
	        if(!startswith.isEmpty())
	            return Collections.unmodifiableList(startswith);
	        return Collections.unmodifiableList(contains);
	    }
	}
	
	public static class GuildRole {
	    /**
	     * Queries a provided {@link net.dv8tion.jda.api.entities.Guild Guild} for {@link net.dv8tion.jda.api.entities.Role Role}s.
	     *
	     * <p>The following special case is applied before the standard search is done:
	     * <ul>
	     *     <li>Role Mention: Query provided matches a @role mention (more specifically {@literal <@&roleID>})</li>
	     * </ul>
	     *
	     * @param  query
	     *         The String query to search by
	     * @param  guild
	     *         The Guild to search from
	     *
	     * @return A possibly-empty {@link java.util.List List} of Roles found by the query from the provided Guild.
	     */
	    public static List<Role> findRoles(String query, Guild guild)
	    {
	        Matcher roleMention = ROLE_MENTION.matcher(query);
	        if(roleMention.matches())
	        {
	            Role role = guild.getRoleById(roleMention.group(1));
	            if(role!=null)
	                return Collections.singletonList(role);
	        }
	        else if(DISCORD_ID.matcher(query).matches())
	        {
	            Role role = guild.getRoleById(query);
	            if(role!=null)
	                return Collections.singletonList(role);
	        }
	        ArrayList<Role> exact = new ArrayList<>();
	        ArrayList<Role> wrongcase = new ArrayList<>();
	        ArrayList<Role> startswith = new ArrayList<>();
	        ArrayList<Role> contains = new ArrayList<>();
	        String lowerquery = query.toLowerCase();
	        guild.getRoleCache().forEach((role) -> {
	            String name = role.getName();
	            if(name.equals(query))
	                exact.add(role);
	            else if(name.equalsIgnoreCase(query) && exact.isEmpty())
	                wrongcase.add(role);
	            else if(name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
	                startswith.add(role);
	            else if(name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
	                contains.add(role);
	        });
	        if(!exact.isEmpty())
	            return Collections.unmodifiableList(exact);
	        if(!wrongcase.isEmpty())
	            return Collections.unmodifiableList(wrongcase);
	        if(!startswith.isEmpty())
	            return Collections.unmodifiableList(startswith);
	        return Collections.unmodifiableList(contains);
	    }
	}
	
	public static class GuildEmote  {
	    /**
	     * Queries a provided instance of {@link net.dv8tion.jda.api.JDA JDA} for
	     * {@link net.dv8tion.jda.api.entities.Emote Emote}s.<p>
	     *
	     * If a {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} is available this will query across that
	     * instead of the JDA instance.
	     *
	     * <p>The following special case is applied before the standard search is done:
	     * <ul>
	     *     <li>Emote Mention: Query provided matches a :emote: mention (more specifically {@literal <:emoteName:emoteID>}).
	     *     <br>Note: This only returns here if the emote is <b>valid</b>. Validity being the ID retrieves a non-null
	     *     Emote and that the {@link net.dv8tion.jda.api.entities.Emote#getName() name} of the Emote is equal to the
	     *     name found in the query.</li>
	     * </ul>
	     *
	     * @param  query
	     *         The String query to search by
	     * @param  jda
	     *         The instance of JDA to search from
	     *
	     * @return A possibly-empty {@link java.util.List List} of Emotes found by the query from the provided JDA instance.
	     */
	    public static List<Emote> findEmotes(String query, JDA jda)
	    {
	        return jdaFindEmotes(query, jda, true);
	    }

	    /**
	     * Queries a provided instance of {@link net.dv8tion.jda.api.JDA JDA} for
	     * {@link net.dv8tion.jda.api.entities.Emote Emote}s.<p>
	     *
	     * This only queries the instance of JDA, regardless of whether or not a
	     * {@link net.dv8tion.jda.api.sharding.ShardManager ShardManager} is available.
	     *
	     * <p>The following special case is applied before the standard search is done:
	     * <ul>
	     *     <li>Emote Mention: Query provided matches a :emote: mention (more specifically {@literal <:emoteName:emoteID>}).
	     *     <br>Note: This only returns here if the emote is <b>valid</b>. Validity being the ID retrieves a non-null
	     *     Emote and that the {@link net.dv8tion.jda.api.entities.Emote#getName() name} of the Emote is equal to the
	     *     name found in the query.</li>
	     * </ul>
	     *
	     * @param  query
	     *         The String query to search by
	     * @param  jda
	     *         The instance of JDA to search from
	     *
	     * @return A possibly-empty {@link java.util.List List} of Emotes found by the query from the provided JDA instance.
	     */
	    public static List<Emote> findShardEmotes(String query, JDA jda)
	    {
	        return jdaFindEmotes(query, jda, false);
	    }

	    /**
	     * Queries a provided {@link net.dv8tion.jda.api.entities.Guild Guild} for
	     * {@link net.dv8tion.jda.api.entities.Emote Emote}s.
	     *
	     * <p>The following special case is applied before the standard search is done:
	     * <ul>
	     *     <li>Emote Mention: Query provided matches a :emote: mention (more specifically {@literal <:emoteName:emoteID>}).
	     *     <br>Note: This only returns here if the emote is <b>valid</b>. Validity being the ID retrieves a non-null
	     *     Emote and that the {@link net.dv8tion.jda.api.entities.Emote#getName() name} of the Emote is equal to the
	     *     name found in the query.</li>
	     * </ul>
	     *
	     * @param  query
	     *         The String query to search by
	     * @param  guild
	     *         The Guild to search from
	     *
	     * @return A possibly-empty {@link java.util.List List} of Emotes found by the query from the provided Guild.
	     */
	    public static List<Emote> findEmotes(String query, Guild guild)
	    {
	        Matcher mentionMatcher = EMOTE_MENTION.matcher(query);
	        if(DISCORD_ID.matcher(query).matches())
	        {
	            Emote emote = guild.getEmoteById(query);
	            if(emote != null)
	                return Collections.singletonList(emote);
	        }
	        else if(mentionMatcher.matches())
	        {
	            String emoteName = mentionMatcher.group(1);
	            String emoteId = mentionMatcher.group(2);
	            Emote emote = guild.getEmoteById(emoteId);
	            if(emote != null && emote.getName().equals(emoteName))
	                return Collections.singletonList(emote);
	        }

	        return genericEmoteSearch(query, guild.getEmoteCache());
	    }

	    private static List<Emote> jdaFindEmotes(String query, JDA jda, boolean useShardManager)
	    {
	        Matcher mentionMatcher = EMOTE_MENTION.matcher(query);

	        ShardManager manager = useShardManager? jda.getShardManager() : null;

	        if(DISCORD_ID.matcher(query).matches())
	        {
	            Emote emote = manager != null? manager.getEmoteById(query) : jda.getEmoteById(query);
	            if(emote != null)
	                return Collections.singletonList(emote);
	        }
	        else if(mentionMatcher.matches())
	        {
	            String emoteName = mentionMatcher.group(1);
	            String emoteId = mentionMatcher.group(2);
	            Emote emote = manager != null? manager.getEmoteById(emoteId) : jda.getEmoteById(emoteId);
	            if(emote != null && emote.getName().equals(emoteName))
	                return Collections.singletonList(emote);
	        }

	        return genericEmoteSearch(query, jda.getEmoteCache());
	    }

	    private static List<Emote> genericEmoteSearch(String query, SnowflakeCacheView<Emote> cache)
	    {
	        ArrayList<Emote> exact = new ArrayList<>();
	        ArrayList<Emote> wrongcase = new ArrayList<>();
	        ArrayList<Emote> startswith = new ArrayList<>();
	        ArrayList<Emote> contains = new ArrayList<>();
	        String lowerquery = query.toLowerCase();
	        cache.forEach(emote -> {
	            String name = emote.getName();
	            if(name.equals(query))
	                exact.add(emote);
	            else if(name.equalsIgnoreCase(query) && exact.isEmpty())
	                wrongcase.add(emote);
	            else if(name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
	                startswith.add(emote);
	            else if(name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
	                contains.add(emote);
	        });
	        if(!exact.isEmpty())
	            return Collections.unmodifiableList(exact);
	        if(!wrongcase.isEmpty())
	            return Collections.unmodifiableList(wrongcase);
	        if(!startswith.isEmpty())
	            return Collections.unmodifiableList(startswith);
	        return Collections.unmodifiableList(contains);
	    }
	}
}
