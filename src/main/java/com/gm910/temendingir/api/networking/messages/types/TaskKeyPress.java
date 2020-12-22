package com.gm910.temendingir.api.networking.messages.types;

import java.awt.event.KeyEvent;

import com.gm910.temendingir.api.events.ServerKeyEvent;
import com.gm910.temendingir.api.networking.messages.ModTask;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.MinecraftForge;

public class TaskKeyPress extends ModTask {

	private int key;

	public TaskKeyPress() {
		key = 0;
	}

	public TaskKeyPress(int key) {
		this.key = key;
	}

	@Override
	public void run() {
		MinecraftForge.EVENT_BUS.post(new ServerKeyEvent(key));
	}

	@Override
	public String toString() {
		return super.toString() + ": " + KeyEvent.getModifiersExText(key);
	}

	public int getKey() {
		return key;
	}

	@Override
	public CompoundNBT write() {
		CompoundNBT dat = super.write();
		dat.putInt("Key", key);
		return dat;
	}

	@Override
	protected void read(CompoundNBT nbt) {
		this.key = nbt.getInt("Key");
	}

	@Override
	public boolean isLClient() {
		return false;
	}

}
