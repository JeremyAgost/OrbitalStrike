package com.Lord.mcplug.orbitalstrike;

public interface Beam {
	public void place();
	
	public boolean canDetonate();
	
	public void detonate();
	
	public boolean shouldRemove();
	
	public void remove();

}
