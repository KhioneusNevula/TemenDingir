package com.gm910.temendingir.world.gods.tasks;

import com.gm910.temendingir.api.networking.messages.ModTask;
import com.google.gson.JsonSyntaxException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ReadBookScreen;
import net.minecraft.client.gui.screen.ReadBookScreen.WrittenBookInfo;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;

public class TaskOpenHolyBookGui extends ModTask {

	private ITextComponent book;
	private ITextComponent title;
	private ITextComponent author;

	public TaskOpenHolyBookGui() {

	}

	public TaskOpenHolyBookGui(ITextComponent book, ITextComponent title, ITextComponent author) {
		this.book = book;
		this.title = title;
		this.author = author;
	}

	@Override
	public void run() {
		ItemStack booke = constructBook();
		Minecraft.getInstance().displayGuiScreen(new ReadBookScreen(new WrittenBookInfo(booke)));
	}

	public ItemStack constructBook() {

		ItemStack stack = new ItemStack(Items.WRITTEN_BOOK); //TODO figure out how this darn gui works ack

		return stack;
	}

	@Override
	public CompoundNBT write() {
		CompoundNBT nbt = super.write();
		nbt.putString("C", ITextComponent.Serializer.toJson(book));
		nbt.putString("T", ITextComponent.Serializer.toJson(title));
		nbt.putString("A", ITextComponent.Serializer.toJson(author));
		return nbt;
	}

	@Override
	protected void read(CompoundNBT nbt) {
		try {
			this.title = ITextComponent.Serializer.getComponentFromJson(nbt.getString("T"));
			this.author = ITextComponent.Serializer.getComponentFromJson(nbt.getString("A"));
			this.book = ITextComponent.Serializer.getComponentFromJson(nbt.getString("C"));

		} catch (JsonSyntaxException e) {
			throw new ReportedException(CrashReport.makeCrashReport(e, "Opening holy book gui task"));
		}
	}
}
