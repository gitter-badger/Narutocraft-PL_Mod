package pl.grm.narutocraft.libs;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import pl.grm.narutocraft.ProxyCommon;
import pl.grm.narutocraft.gui.JutsuInv;
import pl.grm.narutocraft.jutsu.JutsuManager;
import pl.grm.narutocraft.network.DataWriter;

public class ExtendedProperties implements IExtendedEntityProperties {
	public final static String		EXT_PROP_NAME		= "NCPLExtPlayer";
	private final EntityPlayer		player;
	public final JutsuInv			inventory			= new JutsuInv();
	public JutsuManager				jManager			= new JutsuManager();
	public PlayerSkillsAtrributes	psa					= new PlayerSkillsAtrributes();
	private int						maxChakra, maxChakraCap = 500, maxChakraBase = 50;
	private int						AuraIndex;
	private int						AuraBehaviour;
	private float					AuraScale;
	private float					AuraAlpha;
	private boolean					AuraColorRandomize	= true;
	private boolean					AuraColorDefault	= true;
	private int						AuraColor;
	private int						AuraQuantity;
	private float					AuraSpeed;
	public float					TK_Distance			= 8.0F;
	public static final int			CHAKRA_WATCHER		= 20;
	public static List<int[]>		activeJutsus		= new ArrayList<int[]>();
	
	public ExtendedProperties(EntityPlayer player) {
		this.player = player;
		this.maxChakra = maxChakraBase;
		this.player.getDataWatcher().addObject(CHAKRA_WATCHER, this.maxChakra);
	}
	
	public static ExtendedProperties For(EntityLivingBase living) {
		return (ExtendedProperties) living.getExtendedProperties("NarutoCraftExProps");
	}
	
	public static final ExtendedProperties get(EntityPlayer player) {
		return (ExtendedProperties) player.getExtendedProperties(EXT_PROP_NAME);
	}
	
	public static final void loadProxyData(EntityPlayer player) {
		ExtendedProperties playerData = ExtendedProperties.get(player);
		NBTTagCompound savedData = ProxyCommon.getEntityData(getSaveKey(player));
		if (savedData != null) {
			playerData.loadNBTData(savedData);
		}
	}
	
	public static final void register(EntityPlayer player) {
		player.registerExtendedProperties(ExtendedProperties.EXT_PROP_NAME,
				new ExtendedProperties(player));
	}
	
	public static final void saveProxyData(EntityPlayer player) {
		NBTTagCompound savedData = new NBTTagCompound();
		ExtendedProperties.get(player).saveNBTData(savedData);
		ProxyCommon.storeEntityData(getSaveKey(player), savedData);
	}
	
	private static final String getSaveKey(EntityPlayer player) {
		return player.getCommandSenderName() + ":" + EXT_PROP_NAME;
	}
	
	/**
	 * Consumes chakra
	 *
	 * @param value
	 * @return sufficient
	 */
	public final boolean consumeChakra(int value) {
		boolean sufficient = value <= getCurrentChakra();
		setCurrentChakra(getCurrentChakra() - value);
		return sufficient;
	}
	
	public byte[] getAuraData() {
		DataWriter writer = new DataWriter();
		writer.add(this.AuraIndex);
		writer.add(this.AuraBehaviour);
		writer.add(this.AuraScale);
		writer.add(this.AuraAlpha);
		writer.add(this.AuraColorRandomize);
		writer.add(this.AuraColorDefault);
		writer.add(this.AuraColor);
		writer.add(this.AuraQuantity);
		writer.add(this.AuraSpeed);
		
		return writer.generate();
	}
	
	public final int getCurrentChakra() {
		return this.player.getDataWatcher().getWatchableObjectInt(CHAKRA_WATCHER);
	}
	
	public final int getMaxChakra() {
		return this.maxChakra;
	}
	
	@Override
	public void init(Entity entity, World world) {
		
	}
	
	@Override
	public final void saveNBTData(NBTTagCompound compound) {
		NBTTagCompound properties = new NBTTagCompound();
		// this.inventory.writeToNBT(properties);
		this.jManager.writeToNBT(properties);
		properties.setInteger("CurrentChakra", this.player.getDataWatcher()
				.getWatchableObjectInt(CHAKRA_WATCHER));
		properties.setInteger("MaxChakra", this.maxChakra);
		
		// New PSA data saving
		properties.setIntArray("psaStats", this.psa.getValues());
		// Save stats
		/*
		 * NBTTagList stats = new NBTTagList();
		 * for (int i = 0; i < psa.getValues().length; ++i) { NBTTagCompound
		 * stat = new NBTTagCompound(); stat.setInteger("psaStat" + i,
		 * psa.getValues()[i]); stats.appendTag(stat); }
		 * properties.setTag("psaStats", stats);
		 */
		compound.setTag(EXT_PROP_NAME, properties);
	}
	
	@Override
	public final void loadNBTData(NBTTagCompound compound) {
		NBTTagCompound properties = (NBTTagCompound) compound.getTag(EXT_PROP_NAME);
		// this.inventory.readFromNBT(properties);
		this.jManager.readFromNBT(properties);
		this.player.getDataWatcher().updateObject(CHAKRA_WATCHER,
				properties.getInteger("CurrentChakra"));
		this.maxChakra = properties.getInteger("MaxChakra");
		
		// NBTTagList stats = properties .getTagList("psaStats",
		// properties.getId()); int[] statList = new int[stats.tagCount()]; for
		// (int i = 0; i < stats.tagCount(); ++i) { NBTTagCompound stat =
		// stats.getCompoundTagAt(i); statList[i] = stat.getInteger("psaStat" +
		// i); }
		// New PSA loading
		this.psa.setValues(properties.getIntArray("psaStats"));
		
		System.out.println("[NCPL Chakra] Chakra from NBT: "
				+ this.player.getDataWatcher().getWatchableObjectInt(CHAKRA_WATCHER)
				+ "/" + this.maxChakra);
	}
	
	/**
	 * Add amount of chakra to currentChakra.
	 *
	 * @param amount
	 */
	public final void regenChakra(int amount) {
		setCurrentChakra(getCurrentChakra() + amount);
	}
	
	/**
	 * Sets currentChakra to maxChakra.
	 */
	public final void replenishChakra() {
		this.player.getDataWatcher().updateObject(CHAKRA_WATCHER, this.maxChakra);
	}
	
	public final void setCurrentChakra(int value) {
		this.player.getDataWatcher().updateObject(CHAKRA_WATCHER,
				value > 0 ? (value < this.maxChakra ? value : this.maxChakra) : 0);
	}
	
	/** Sets the max chakra the player can have
	 * @param amount what number + maxChakraBonus the max chakra will be NOTE: only works if overridden
	 * @param overrideBaseValue this will use the amount if true, else it will only update the baseChakra + bonus Chakra **/
	public final void setMaxChakra(int amount, boolean overrideBaseValue) {
		if (overrideBaseValue)
			this.maxChakra = MathHelper.clamp_int(amount + psa.getMaxChakraMod(), 0, maxChakraCap);
		else
			this.maxChakra = MathHelper.clamp_int(maxChakraBase + psa.getMaxChakraMod(), 0, maxChakraCap);		
	}
	
	/** Force set the maxChakra + bonus **/
	public final void setMaxChakra(int amount) {
		setMaxChakra(amount,true);		
	}
	
	/** This is basically an update max chakra method **/
	public final void setMaxChakra(boolean overrideBaseValue) {
		setMaxChakra(0,false);		
	}
	
	public void updateAuraData(int index, int behaviour, float scale, float alpha,
			boolean randomColor, boolean defaultColor, int color, int quantity,
			float speed) {
		this.AuraIndex = index;
		this.AuraBehaviour = behaviour;
		this.AuraScale = scale;
		this.AuraAlpha = alpha;
		this.AuraColorRandomize = randomColor;
		this.AuraColorDefault = defaultColor;
		this.AuraColor = color;
		this.AuraQuantity = quantity;
		this.AuraSpeed = speed;
	}
	
	public void updateMoveSpeed()
	{
		PlayerCapabilities pc = player.capabilities;
		try {
			Field walkSpeed = PlayerCapabilities.class.getDeclaredField("walkSpeed");
			walkSpeed.setAccessible(true);
			walkSpeed.setFloat(pc, MathHelper.clamp_float(0.1F + ((float) psa.getAgility() / 142), 0.04f, 0.3f));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
}