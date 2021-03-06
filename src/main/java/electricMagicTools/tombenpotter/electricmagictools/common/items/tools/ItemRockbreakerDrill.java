/*******************************************************************************
 * Copyright (c) 2014 Tombenpotter.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at http://www.gnu.org/licenses/gpl.html
 * 
 * This class was made by Tombenpotter and is distributed as a part of the Electro-Magic Tools mod.
 * Electro-Magic Tools is a derivative work on Thaumcraft 4 (c) Azanor 2012.
 * http://www.minecraftforum.net/topic/1585216-
 * 
 * This class originally belongs to Azanor, but with his permission I took it, and modified it to make it work as I wanted to.
 ******************************************************************************/
package electricMagicTools.tombenpotter.electricmagictools.common.items.tools;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.EntityFollowingItem;
import thaumcraft.common.lib.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electricMagicTools.tombenpotter.electricmagictools.common.Config;
import electricMagicTools.tombenpotter.electricmagictools.common.CreativeTab;

public class ItemRockbreakerDrill extends ItemPickaxe implements IElectricItem
{

	public Icon icon;
	int side;

	public ItemRockbreakerDrill(int id)
	{
		super(id, EnumToolMaterial.EMERALD);
		side = 0;
		setCreativeTab(CreativeTab.tabTombenpotter);
		this.efficiencyOnProperMaterial = 25F;
		this.setMaxStackSize(1);
		if (Config.toolsInBore == false)
		{
			this.setMaxDamage(27);
		} else
		{
			this.setMaxDamage(2571);
		}
	}

	public int maxCharge = 900000;
	private int cost;
	private final int searchCost = 1000;
	private final int hitCost = 400;

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconRegister) {
		this.itemIcon = iconRegister.registerIcon("electricmagictools:rockbreakerdrill");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@SideOnly(Side.CLIENT)
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List itemList) {
		ItemStack itemStack = new ItemStack(this, 1);

		if (getChargedItemId(itemStack) == this.itemID)
		{
			ItemStack charged = new ItemStack(this, 1);
			ElectricItem.manager.charge(charged, 2147483647, 2147483647, true, false);
			itemList.add(charged);
		}

		if (getEmptyItemId(itemStack) == this.itemID)
			itemList.add(new ItemStack(this, 1, getMaxDamage()));
	}

	@SuppressWarnings("unused")
	private boolean isEffectiveAgainst(Block block) {
		label0:
		{
			int var3 = 0;
			do
			{
				ItemRockbreakerDrill _tmp = this;
				if (var3 >= ItemPickaxe.blocksEffectiveAgainst.length && var3 >= ItemSpade.blocksEffectiveAgainst.length)
				{
					break label0;
				}
				ItemRockbreakerDrill _tmp1 = this;
				if (ItemPickaxe.blocksEffectiveAgainst[var3] == block)
				{
					return true;
				}
				var3++;
			} while (true);
		}
		return false;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, int X, int Y, int Z, EntityPlayer player) {
		MovingObjectPosition movingobjectposition = Utils.getTargetBlock(((Entity) (player)).worldObj, player, true);
		if (movingobjectposition != null && movingobjectposition.typeOfHit == EnumMovingObjectType.TILE)
		{
			side = movingobjectposition.sideHit;
		}
		return super.onBlockStartBreak(itemstack, X, Y, Z, player);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean onBlockDestroyed(ItemStack stack, World world, int bi, int x, int y, int z, EntityLivingBase ent) {
		if (Config.toolsInBore == false)
		{
			cost = 350;
		} else
		{
			cost = 1;
		}
		if (ent.isSneaking())
		{
			return super.onBlockDestroyed(stack, world, bi, x, y, z, ent);
		}
		int md = world.getBlockMetadata(x, y, z);
		if (ForgeHooks.isToolEffective(stack, Block.blocksList[bi], md) || isEffectiveAgainst(Block.blocksList[bi]))
		{
			for (int aa = -1; aa <= 1; aa++)
			{
				for (int bb = -1; bb <= 1; bb++)
				{
					int xx = 0;
					int yy = 0;
					int zz = 0;
					if (side <= 1)
					{
						xx = aa;
						zz = bb;
					} else if (side <= 3)
					{
						xx = aa;
						yy = bb;
					} else
					{
						zz = aa;
						yy = bb;
					}
					int bl = world.getBlockId(x + xx, y + yy, z + zz);
					md = world.getBlockMetadata(x + xx, y + yy, z + zz);
					if (!ForgeHooks.isToolEffective(stack, Block.blocksList[bl], md) && !isEffectiveAgainst(Block.blocksList[bl]))
					{
						continue;
					}
					if (ElectricItem.manager.canUse(stack, cost))
					{
						ElectricItem.manager.use(stack, cost, ent);
					}
					if (((Entity) (ent)).worldObj.isRemote)
					{
						world.playAuxSFX(2001, x + xx, y + yy, z + zz, bl + (md << 12));
					}
					int fortune = EnchantmentHelper.getFortuneModifier(ent);
					world.setBlock(x + xx, y + yy, z + zz, 0, 0, 3);
					ArrayList ret = Block.blocksList[bl].getBlockDropped(world, x + xx, y + yy, z + zz, md, fortune);
					boolean creative = false;
					if ((ent instanceof EntityPlayer) && ((EntityPlayer) ent).capabilities.isCreativeMode)
					{
						creative = true;
					}
					if (ret.size() <= 0 || creative || ((Entity) (ent)).worldObj.isRemote)
					{
						continue;
					}
					ItemStack is;
					for (Iterator i$ = ret.iterator(); i$.hasNext(); world.spawnEntityInWorld(new EntityFollowingItem(world, (double) x + (double) xx + 0.5D, (double) y + (double) yy + 0.5D, (double) z + (double) zz + 0.5D, is, ent, 3)))
					{
						is = (ItemStack) i$.next();
					}
				}
			}
		}
		return true;
	}

	@SuppressWarnings("unused")
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float xOffset, float yOffset, float zOffset) {
		if (!player.isSneaking())
		{
			for (int i = 0; i < player.inventory.mainInventory.length; i++)
			{
				ItemStack torchStack = player.inventory.mainInventory[i];
				if (torchStack == null || !torchStack.getUnlocalizedName().toLowerCase().contains("torch"))
				{
					continue;
				}
				Item item = torchStack.getItem();
				if (!(item instanceof ItemBlock))
				{
					continue;
				}
				int oldMeta = torchStack.getItemDamage();
				int oldSize = torchStack.stackSize;
				boolean result = torchStack.tryPlaceItemIntoWorld(player, world, x, y, z, side, xOffset, yOffset, zOffset);
				if (player.capabilities.isCreativeMode)
				{
					torchStack.setItemDamage(oldMeta);
					torchStack.stackSize = oldSize;
				} else if (torchStack.stackSize <= 0)
				{
					ForgeEventFactory.onPlayerDestroyItem(player, torchStack);
					player.inventory.mainInventory[i] = null;
				}
				if (result)
				{
					return true;
				}
			}
		} else
		{
			ElectricItem.manager.use(stack, searchCost, player);
			if (!world.isRemote)
			{
				world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "thaumcraft:wandfail", 0.2F, 0.2F + world.rand.nextFloat() * 0.2F);
				return super.onItemUse(stack, player, world, x, y, z, side, xOffset, xOffset, zOffset);
			}
			Minecraft mc = Minecraft.getMinecraft();
			Thaumcraft.instance.renderEventHandler.startScan(player, x, y, z, System.currentTimeMillis() + 5000L);

			player.swingItem();
			return super.onItemUse(stack, player, world, x, y, z, side, xOffset, yOffset, zOffset);
		}

		return super.onItemUse(stack, player, world, x, y, z, side, xOffset, yOffset, zOffset);
	}

	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!((Entity) (player)).worldObj.isRemote && (!(entity instanceof EntityPlayer) || MinecraftServer.getServer().isPVPEnabled()))
		{
			entity.setFire(2);
		}
		return super.onLeftClickEntity(stack, player, entity);
	}

	@Override
	public boolean hitEntity(ItemStack itemstack, EntityLivingBase entityliving, EntityLivingBase attacker) {
		if (ElectricItem.manager.use(itemstack, hitCost, attacker))
		{
			entityliving.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) attacker), 12F);
		}
		return false;
	}

	@Override
	public boolean canProvideEnergy(ItemStack itemStack) {
		return false;
	}

	@Override
	public int getChargedItemId(ItemStack itemStack) {
		return itemID;
	}

	@Override
	public int getEmptyItemId(ItemStack itemStack) {
		return itemID;
	}

	@Override
	public int getMaxCharge(ItemStack itemStack) {
		return maxCharge;
	}

	@Override
	public int getTier(ItemStack itemStack) {
		return 3;
	}

	@Override
	public int getTransferLimit(ItemStack itemStack) {
		return 900;
	}

	@Override
	public boolean canHarvestBlock(Block block, ItemStack stack) {
		return Item.pickaxeDiamond.canHarvestBlock(block) || Item.shovelDiamond.canHarvestBlock(block);
	}

	@Override
	public float getStrVsBlock(ItemStack stack, Block block, int meta) {
		if (!ElectricItem.manager.canUse(stack, cost))
		{
			return 1.0F;
		}

		if (Item.pickaxeWood.getStrVsBlock(stack, block, meta) > 1.0F || Item.shovelWood.getStrVsBlock(stack, block, meta) > 1.0F)
		{
			return efficiencyOnProperMaterial;
		} else
		{
			return super.getStrVsBlock(stack, block, meta);
		}
	}

	@Override
	public boolean isRepairable() {
		return false;
	}

	@Override
	public int getItemEnchantability() {
		if (Config.enchanting == false)
		{
			return 0;
		} else
		{
			return 4;
		}
	}

	@Override
	public boolean isBookEnchantable(ItemStack itemstack1, ItemStack itemstack2) {
		if (Config.enchanting == false)
		{
			return false;
		} else
		{
			return true;
		}
	}
}
