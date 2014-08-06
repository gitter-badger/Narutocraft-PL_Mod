package pl.grm.narutocraftmod.Weapons;

import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;
import net.minecraftforge.common.util.EnumHelper;
import pl.grm.narutocraftmod.NarutoCraftMod;
import pl.grm.narutocraftmod.Libs.Materials;

public class ZRekawica extends ItemSword {	
	public ZRekawica (int i) {
		super(Materials.stal);
		
		this.setUnlocalizedName("ZRekawica");
		this.setTextureName("narutocraftmod:Zelazna Rekawica");
		this.setCreativeTab(NarutoCraftMod.mTabNarutoCraftMod);

	}
}