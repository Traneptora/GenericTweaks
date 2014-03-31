package thebombzen.mods.generictweaks.fastcrafting;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Recipe {
	
	private ReducedItemStack[] items = new ReducedItemStack[9];
	private ReducedItemStack result;

	public Recipe(DataInputStream in) throws IOException {
		for (int i = 0; i < 9; i++){
			int damage = in.readInt();
			String name = in.readUTF();
			items[i] = new ReducedItemStack(name, damage);
		}
		int damage = in.readInt();
		String name = in.readUTF();
		result = new ReducedItemStack(name, damage);
	}
	
	public Recipe(boolean specific, ReducedItemStack result, Collection<? extends ReducedItemStack> items){
		if (items.size() != 9){
			throw new IllegalArgumentException();
		}
		int i = 0;
		for (Iterator<? extends ReducedItemStack> iter = items.iterator(); iter.hasNext(); i++){
			ReducedItemStack stack = iter.next();
			if (specific){
				this.items[i] = stack;
			} else {
				this.items[i] = new ReducedItemStack(stack.getName(), -1);
			}
		}
		if (specific){
			this.result = result;
		} else {
			this.result = new ReducedItemStack(result.getName(), -1);
		}
	}
	
	public ReducedItemStack getResult() {
		return result;
	}
	
	public ReducedItemStack[] getNonemptyStacks(){
		List<ReducedItemStack> list = new ArrayList<ReducedItemStack>(Arrays.asList(items));
		list.removeAll(Collections.singletonList(ReducedItemStack.NONE));
		return list.toArray(new ReducedItemStack[0]);
	}
	
	public String toString(){
		return result.getName() + " = " + Arrays.toString(items);
	}
	
	public boolean contains(Recipe recipe){
		if (items.length != recipe.items.length){
			return false;
		}
		for (int i = 0; i < items.length; i++){
			if (!items[i].contains(recipe.items[i])){
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Recipe other = (Recipe) obj;
		if (other.items.length != items.length){
			return false;
		}
		for (int i = 0; i < items.length; i++){
			if (!items[i].equals(other.items[i])){
				return false;
			}
		}
		return true;
	}
	
	public boolean isSpecific(){
		return result.getDamageValue() != -1;
	}

	public ReducedItemStack[] getItemStacks(){
		return items;
	}
	
	public void writeToOutputStream(DataOutputStream out) throws IOException {
		for (int i = 0; i < 9; i++){
			out.writeInt(items[i].getDamageValue());
			out.writeUTF(items[i].getName());
		}
		out.writeInt(result.getDamageValue());
		out.writeUTF(result.getName());
	}

}
