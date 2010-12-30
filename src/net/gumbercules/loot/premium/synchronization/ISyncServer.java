package net.gumbercules.loot.premium.synchronization;

import java.io.IOException;

public interface ISyncServer
{
	public abstract void startServer() throws IOException;
	public abstract void stopServer() throws IOException;
	
	public abstract String getId();
}
