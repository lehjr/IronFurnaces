package ironfurnaces.items;

import ironfurnaces.init.ModBlocks;
import ironfurnaces.tileentity.BlockIronFurnaceTileBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemUpgrade extends Item {

    private int[] available;

    public ItemUpgrade(Properties properties, int[] available) {
        super(properties);
        this.available = available;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new StringTextComponent("Sneak & right-click to upgrade"));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getPos();
        boolean playSound = false;
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            BlockItemUseContext ctx2 = new BlockItemUseContext(ctx);
            if (te instanceof FurnaceTileEntity || te instanceof BlockIronFurnaceTileBase) {
                int cooktime = 0;
                int currentItemBurnTime = 0;
                int furnaceBurnTime = 0;
                if (te instanceof BlockIronFurnaceTileBase) {
                    furnaceBurnTime = ((BlockIronFurnaceTileBase)te).fields.get(0);
                    currentItemBurnTime = ((BlockIronFurnaceTileBase)te).fields.get(1);
                    cooktime = ((BlockIronFurnaceTileBase)te).fields.get(2);
                }

                BlockState state =  world.getBlockState(pos);
                Direction facing = state.has(BlockStateProperties.HORIZONTAL_FACING) ? state.get(BlockStateProperties.HORIZONTAL_FACING) : Direction.NORTH;

                BlockState next = this.getNextTierBlock(te, available).getStateForPlacement(ctx2) != null ? this.getNextTierBlock(te, available).getStateForPlacement(ctx2) : null;
                if (next == null) {
                    return ActionResultType.PASS;
                }
                next = next.with(BlockStateProperties.HORIZONTAL_FACING, facing);

                ItemStack input = ((IInventory) te).getStackInSlot(0).copy();
                ItemStack fuel  = ((IInventory) te).getStackInSlot(1).copy();
                ItemStack output  = ((IInventory) te).getStackInSlot(2).copy();
                world.removeTileEntity(te.getPos());
                world.setBlockState(pos, next, 3);
                TileEntity newTe = world.getTileEntity(pos);
                ((IInventory)newTe).setInventorySlotContents(0, input);
                ((IInventory)newTe).setInventorySlotContents(1, fuel);
                ((IInventory)newTe).setInventorySlotContents(2, output);
                if (newTe instanceof BlockIronFurnaceTileBase) {
                    ((BlockIronFurnaceTileBase)newTe).fields.set(0, furnaceBurnTime);
                    ((BlockIronFurnaceTileBase)newTe).fields.set(1, currentItemBurnTime);
                    ((BlockIronFurnaceTileBase)newTe).fields.set(2, cooktime);
                }
                if (!ctx.getPlayer().isCreative()) {
                    ctx.getItem().shrink(1);
                }
            }
        }
        return super.onItemUse(ctx);
    }

    public static Block getNextTierBlock(TileEntity te, int[] available) {
        Block block = te.getBlockState().getBlock();
        if (block == Blocks.FURNACE && available[0] == 1) {
            return ModBlocks.iron_furnace;
        } else
        if (block == ModBlocks.iron_furnace && available[1] == 1) {
            return ModBlocks.gold_furnace;
        } else
        if (block == ModBlocks.gold_furnace && available[2] == 1) {
            return ModBlocks.diamond_furnace;
        } else
        if (block == ModBlocks.diamond_furnace && available[3] == 1) {
            return ModBlocks.emerald_furnace;
        } else
        if (block == ModBlocks.emerald_furnace && available[4] == 1) {
            return ModBlocks.obsidian_furnace;
        }
        return null;
    }
}
