package net.FENGberd.Nukkit.FNPC.tasks;

import cn.nukkit.scheduler.PluginTask;
import net.FENGberd.Nukkit.FNPC.Main;
import net.FENGberd.Nukkit.FNPC.npc.NPC;

public class QuickSystemTask extends PluginTask<Main>
{
	public QuickSystemTask(Main owner)
	{
		super(owner);
	}

	@Override
	public void onRun(int currentTick)
	{
		NPC.tick();
	}
}
