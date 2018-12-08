package suomicraftpe.events.newyear;

import cn.nukkit.Server;
import cn.nukkit.entity.item.EntityFirework;
import cn.nukkit.item.ItemFirework;
import cn.nukkit.item.ItemFirework.FireworkExplosion;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.DyeColor;

import java.util.Random;

/**
 * SuomiCraft PE Events / New Year
 * Created by PetteriM1 for SuomiCraft PE Network
 */
public class Main extends PluginBase {

    public static Config config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        if (getServer().getLevelByName(config.getString("level")) != null) {
            getServer().getScheduler().scheduleDelayedRepeatingTask(this, new Task(), config.getInt("tick"), config.getInt("tick"), true);
        } else {
            getServer().getLogger().notice("Plugin not enabled due to invalid world name in config");
        }
    }

    public static void spawnFirework(Vector3 pos) {
        Level level = Server.getInstance().getLevelByName(config.getString("level"));
        ItemFirework item = new ItemFirework();
        CompoundTag tag = new CompoundTag();
        Random random = new Random();
        CompoundTag ex = new CompoundTag()
                .putByteArray("FireworkColor", new byte[]{(byte) DyeColor.values()[random.nextInt(FireworkExplosion.ExplosionType.values().length)].getDyeData()})
                .putByteArray("FireworkFade", new byte[]{})
                .putBoolean("FireworkFlicker", random.nextBoolean())
                .putBoolean("FireworkTrail", random.nextBoolean())
                .putByte("FireworkType", FireworkExplosion.ExplosionType.values()[random.nextInt(FireworkExplosion.ExplosionType.values().length)].ordinal());
        tag.putCompound("Fireworks", new CompoundTag("Fireworks")
                .putList(new ListTag<CompoundTag>("Explosions").add(ex))
                .putByte("Flight", 1));
        item.setNamedTag(tag);
        CompoundTag nbt = new CompoundTag()
                .putList(new ListTag<DoubleTag>("Pos")
                        .add(new DoubleTag("", pos.x + 0.5))
                        .add(new DoubleTag("", pos.y + 0.5))
                        .add(new DoubleTag("", pos.z + 0.5)))
                .putList(new ListTag<DoubleTag>("Motion")
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0)))
                .putList(new ListTag<FloatTag>("Rotation")
                        .add(new FloatTag("", 0))
                        .add(new FloatTag("", 0)))
                .putCompound("FireworkItem", NBTIO.putItemHelper(item));
        EntityFirework entity = new EntityFirework(level.getChunk((int) pos.x >> 4, (int) pos.z >> 4), nbt);
        entity.spawnToAll();
    }
}

class Task extends Thread {

    @Override
    public void run() {
        if (Server.getInstance().getOnlinePlayers().size() > 0) {
            Main.spawnFirework(new Vector3(Main.config.getInt("pos1.x"), Main.config.getInt("pos1.y"), Main.config.getInt("pos1.z")));
            Main.spawnFirework(new Vector3(Main.config.getInt("pos2.x"), Main.config.getInt("pos2.y"), Main.config.getInt("pos2.z")));
            Main.spawnFirework(new Vector3(Main.config.getInt("pos3.x"), Main.config.getInt("pos3.y"), Main.config.getInt("pos3.z")));
            Main.spawnFirework(new Vector3(Main.config.getInt("pos4.x"), Main.config.getInt("pos4.y"), Main.config.getInt("pos4.z")));
        }
    }
}