/*
 */
package com.infinityraider.agricraft.apiimpl.v3;

import com.infinityraider.agricraft.api.v3.registry.IPlantRegistry;
import com.infinityraider.agricraft.farming.CropPlantHandler;
import com.infinityraider.agricraft.utility.exception.DuplicateCropPlantException;
import java.util.List;
import net.minecraft.item.ItemStack;
import com.infinityraider.agricraft.api.v3.core.IAgriPlant;

/**
 *
 * @author RlonRyan
 */
public class PlantRegistry implements IPlantRegistry {

	@Override
	public boolean isPlant(ItemStack seed) {
		return CropPlantHandler.isValidSeed(seed);
	}

	@Override
	public boolean isPlant(IAgriPlant plant) {
		return CropPlantHandler.getPlantIds().contains(plant.getId());
	}

	@Override
	public boolean addPlant(IAgriPlant plant) {
		try {
			CropPlantHandler.registerPlant(plant);
			return true;
		} catch (DuplicateCropPlantException e) {
			return false;
		}
	}

	@Override
	public IAgriPlant getPlant(ItemStack seed) {
		return CropPlantHandler.getPlantFromStack(seed);
	}

	@Override
	public List<IAgriPlant> getPlants() {
		return CropPlantHandler.getPlants();
	}

	@Override
	public List<String> getPlantIds() {
		return CropPlantHandler.getPlantIds();
	}
	
}
