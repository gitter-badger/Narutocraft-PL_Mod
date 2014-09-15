package pl.grm.narutocraft.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import pl.grm.narutocraft.NarutoCraft;
import pl.grm.narutocraft.libs.ExtendedProperties;
import pl.grm.narutocraft.libs.registry.RegJutsus;
import pl.grm.narutocraft.network.PacketNinjaStatsResponse;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Event Handler of NCPL mod
 * 
 * @author Admaster
 *
 */
public class NCPLEventHandler {

	@SubscribeEvent
	public void onEntityConstructing(EntityConstructing event) {
		if (event.entity instanceof EntityPlayer
				&& ExtendedProperties.get((EntityPlayer) event.entity) == null)
			ExtendedProperties.register((EntityPlayer) event.entity);
		if (event.entity instanceof EntityPlayer
				&& event.entity
						.getExtendedProperties(ExtendedProperties.EXT_PROP_NAME) == null)
			event.entity.registerExtendedProperties(
					ExtendedProperties.EXT_PROP_NAME, new ExtendedProperties(
							(EntityPlayer) event.entity));

	}

	@SubscribeEvent
	public void onLivingFallEvent(LivingFallEvent event) {
		if (event.entity instanceof EntityPlayer) {
			ExtendedProperties props = ExtendedProperties
					.get((EntityPlayer) event.entity);
			if (event.distance > 3.0F && props.getCurrentChakra() > 0) {
				System.out.println("[EVENT] Fall distance: " + event.distance);
				System.out.println("[EVENT] Current chakra: "
						+ props.getCurrentChakra());
				float reduceby = props.getCurrentChakra() < (event.distance - 3.0F)
						? props.getCurrentChakra()
						: (event.distance - 3.0F);
				event.distance -= reduceby;

				props.consumeChakra((int) reduceby);

				System.out.println("[EVENT] Adjusted fall distance: "
						+ event.distance);
			}
		}
	}

	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if (!event.entity.worldObj.isRemote
				&& event.entity instanceof EntityPlayer) {
			ExtendedProperties.loadProxyData((EntityPlayer) event.entity);
		}
	}

	@SubscribeEvent
	public void onLivingDeathEvent(LivingDeathEvent event) {
		if (!event.entity.worldObj.isRemote
				&& event.entity instanceof EntityPlayer) {
			ExtendedProperties.saveProxyData((EntityPlayer) event.entity);
		}
		//Adding XP when Killing an enemy, plus update client
		if (event.source.getEntity() instanceof EntityPlayer)
		{
			ExtendedProperties prop = ExtendedProperties.get((EntityPlayer)event.source.getEntity());
			System.out.println(prop.psa.getCurrentNinjaXp());
			prop.psa.levelUp((int)event.entityLiving.getMaxHealth() / 3);
			NarutoCraft.netHandler.sendTo(new PacketNinjaStatsResponse(prop.psa.getValues()), (EntityPlayerMP)event.source.getEntity());
			System.out.println(prop.psa.getCurrentNinjaXp());
		}
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event) {
		if (event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entity;
			// ExtendedPlayer.get(player).onUpdate();
			if (event.entity.worldObj.getWorldTime() % 100 == 0)
				ExtendedProperties.get(player).regenChakra(1);
			if (player.isPlayerFullyAsleep()) {
				System.out
						.println("After a full night's rest, you feel refreshed!");
				ExtendedProperties.get(player).replenishChakra();
			}
		}
	}

	@SubscribeEvent
	public void onLivingUpdateEvent(LivingUpdateEvent event) {
		if (event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entity;
			ItemStack heldItem = player.getHeldItem();
			if (heldItem != null
					&& heldItem.getItem() == RegJutsus.KawarimiNoJutsu) // Testing
			{
				player.capabilities.allowFlying = true;
			} else {
				player.capabilities.allowFlying = player.capabilities.isCreativeMode
						? true
						: false;
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event)
	{
		if(event.wasDeath)
		{
			if(!event.entityPlayer.worldObj.isRemote)
			{
				ExtendedProperties deadPlayer = ExtendedProperties.get(event.original);
				ExtendedProperties clonePlayer = ExtendedProperties.get(event.entityPlayer);
				clonePlayer.psa.setValues(deadPlayer.psa.getValues());
				//Need to have a packet get sent to update the client that the values are changed.
				//net.sendToServer(new RequestPacket("myStats"));
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerJoin(EntityJoinWorldEvent e)
	{
		if (e.entity instanceof EntityPlayer && !e.world.isRemote)
			NarutoCraft.netHandler.sendTo(new PacketNinjaStatsResponse(ExtendedProperties.get((EntityPlayer) e.entity).psa.getValues()), (EntityPlayerMP) e.entity);
	}
}