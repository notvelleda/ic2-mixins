package xyz.velleda.ic2mixins.mixins;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import ic2.core.block.machine.tileentity.TileEntityRecycler;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(targets = "ic2/core/block/machine/tileentity/TileEntityRecycler$RecyclerRecipeManager", remap = false)
public abstract class RecyclerRecipeManager {
    private static Collection<ItemStack> output = Collections.singletonList(ItemName.crafting.getItemStack(CraftingItemType.scrap));

    // wrapper because ItemStack doesn't hash or implement equals() properly
    private class ItemStackKey {
        public ItemStack itemStack;

        public ItemStackKey(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        @Override
        public int hashCode() {
            // hash the properties that ItemStack.areItemStacksEqual() compares
            return Arrays.hashCode(new Object[] {
                itemStack.getCount(), Item.getIdFromItem(itemStack.getItem()), itemStack.getItemDamage(), itemStack.getTagCompound(),
            });
        }

        @Override
        public boolean equals(Object other) {
            return ItemStack.areItemStacksEqual(this.itemStack, ((ItemStackKey)other).itemStack);
        }
    }

    private Hashtable<ItemStackKey, MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack>> resultCache = new Hashtable();

    @Overwrite
    private static Collection<ItemStack> getOutput(ItemStack input) {
        return TileEntityRecycler.getIsItemBlacklisted(input) ? Collections.emptyList() : RecyclerRecipeManager.output;
    }

    private static MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> createResultFor(ItemStack input, Collection<ItemStack> output) {
        return new MachineRecipe<IRecipeInput, Collection<ItemStack>>(Recipes.inputFactory.forStack(input, 1), output).getResult(StackUtil.copyWithSize(input, StackUtil.getSize(input) - 1));
    }

    @Overwrite
    public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(ItemStack input, boolean acceptTest) {
        if (StackUtil.isEmpty(input)) {
            return null;
        }

        // blacklisted check here to avoid caching invalid items
        if (TileEntityRecycler.getIsItemBlacklisted(input)) {
            // TODO: maybe speed this up somehow?
            return this.createResultFor(input, Collections.emptyList());
        }

        ItemStackKey key = new ItemStackKey(input); // really hate creating a new object here since it'll just be freed anyways but hopefully it's fine?
        MachineRecipeResult cacheResult = this.resultCache.get(key);
        if (cacheResult != null) {
            return cacheResult;
        }

        MachineRecipeResult newResult = this.createResultFor(input, this.output);
        this.resultCache.put(key, newResult);
        return newResult;
    }
}
