package com.loadouts.loadout;
import java.util.UUID;
public class LoadoutNotFoundException extends RuntimeException { public LoadoutNotFoundException(UUID id){super("Loadout not found: "+id);} }
