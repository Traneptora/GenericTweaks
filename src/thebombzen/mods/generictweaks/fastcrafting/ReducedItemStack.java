package thebombzen.mods.generictweaks.fastcrafting;

import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

public class ReducedItemStack {
	
	public static final ReducedItemStack NONE = new ReducedItemStack(null, false);
	
	private String name;
	private int damageValue;
	
	public ReducedItemStack(String name, int damageValue){
		this.name = name;
		this.damageValue = damageValue;
	}
	
	public ReducedItemStack(ItemStack stack, boolean specific){
		if (stack == null){
			this.name = "";
			this.damageValue = 0;
		} else {
			UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(stack.getItem());
			this.name = id.modId + ":" + id.name;
			this.damageValue = specific ? stack.getItemDamage() : -1;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + damageValue;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReducedItemStack other = (ReducedItemStack) obj;
		if (damageValue != other.damageValue)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public boolean contains(ReducedItemStack stack){
		if (!name.equals(stack.name)){
			return false;
		}
		return (damageValue == -1 || damageValue == stack.damageValue);
	}
	
	public boolean isEmpty(){
		return name.equals("");
	}
	
	public String getName() {
		return name;
	}
	
	public int getDamageValue() {
		return damageValue;
	}
	
	@Override
	public String toString() {
		if (isEmpty()){
			return "";
		}
		return damageValue != -1 ? name + "+" + damageValue : name;
	}
	
}
