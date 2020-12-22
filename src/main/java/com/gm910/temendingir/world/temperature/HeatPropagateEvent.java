package com.gm910.temendingir.world.temperature;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.BlockEvent;

/**
 * This is the server-side event fired when heat travels between blocks. It is
 * called after all calculations have been finished and the value is the final
 * heat value; however, this is before the blockstates actually receive/lose
 * heat. "AffectedBlock" is the block that had heat sent to it. heatAttained is
 * the heat amount that is received; changing it changes the amount of heat
 * traveling. heatLost is the heat amount that was spent by the first block.
 * returnHeat is the amount of heat that does not pass through when the heat
 * travels and is givne back to the first block. Direction is the block face it
 * travels through.
 * 
 * @author borah
 *
 */
public class HeatPropagateEvent extends BlockEvent {

	private BlockPos to;

	private BlockState toState;

	private float heatAttained;

	private float heatLost;

	private float returnHeat;

	private Direction direction;

	private Temperatures temperatureHandler;

	private boolean isSunlight;

	public HeatPropagateEvent(Temperatures temperatureHandler, ServerWorld world, BlockPos from, BlockPos to,
			float heatAttained, float heatLost, float returnHeat, boolean isSunlight) {

		super(world, from, world.getBlockState(from));

		this.to = to;
		this.toState = world.getBlockState(to);
		this.heatAttained = heatAttained;
		this.heatLost = heatLost;
		this.returnHeat = returnHeat;
		Vector3i betweenVec = to.subtract(from);
		this.direction = Direction.getFacingFromVector(betweenVec.getX(), betweenVec.getY(), betweenVec.getZ());
		this.temperatureHandler = temperatureHandler;
		this.isSunlight = isSunlight;
	}

	/**
	 * If the temperature propagation was from sunlight; this means getFrom will be
	 * the highest y value but the same x,z, as getTo
	 * 
	 * @return
	 */
	public boolean isSunlight() {
		return isSunlight;
	}

	@Override
	public ServerWorld getWorld() {
		// TODO Auto-generated method stub
		return (ServerWorld) super.getWorld();
	}

	/**
	 * Block pos heat is traveling to
	 * 
	 * @return
	 */
	public BlockPos getTo() {
		return to;
	}

	/**
	 * Block pos heat is traveling from
	 * 
	 * @return
	 */
	public BlockState getToState() {
		return toState;
	}

	public Direction getDirection() {
		return direction;
	}

	public float getHeatAttained() {
		return heatAttained;
	}

	/**
	 * Blockstate heat travels from
	 */
	@Override
	public BlockState getState() {
		return super.getState();
	}

	/**
	 * Position heat travels from
	 */
	@Override
	public BlockPos getPos() {
		return super.getPos();
	}

	/**
	 * Changes amount of heat that travels; is irrespective of heat rate so the
	 * amount can be changed to anything
	 * 
	 * @param heat
	 */
	public void setHeatAttained(float heat) {
		this.heatAttained = heat;
	}

	public float getReturnHeat() {
		return returnHeat;
	}

	public void setReturnHeat(float returnHeat) {
		this.returnHeat = returnHeat;
	}

	/**
	 * Heat amount taken from the initial block
	 * 
	 * @return
	 */
	public float getHeatLost() {
		return heatLost;
	}

	public void setHeatLost(float heatLost) {
		this.heatLost = heatLost;
	}

	public float predictTemperatureAtTo() {
		return temperatureHandler.getTemperatureAt(to) + heatAttained;
	}

	public float predictTemperatureAtFrom() {
		return temperatureHandler.getTemperatureAt(getPos()) - heatLost + returnHeat;
	}

	public Temperatures getTemperatureHandler() {
		return temperatureHandler;
	}

	public void setTemperatureHandler(Temperatures temperatureHandler) {
		this.temperatureHandler = temperatureHandler;
	}
}
