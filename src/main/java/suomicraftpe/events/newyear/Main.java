package suomicraftpe.events.newyear;

import cn.nukkit.Server;
import cn.nukkit.item.ItemFirework;
import cn.nukkit.item.ItemFirework.FireworkExplosion;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.DyeColor;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/**
 * SuomiCraft PE Events / New Year
 * Created by PetteriM1 for SuomiCraft PE Network
 */
public class Main extends PluginBase {

    static String world;
    static int fireworkFlightDuration;
    static boolean muteSpawnSound;
    static boolean muteExplodeSound;
    static boolean splitGroups;
    static final List<Vector3> positions = new ArrayList<>();
    static final SplittableRandom random = new SplittableRandom();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Config config = getConfig();

        if (config.getInt("configVersion") != 3) {
            getServer().getLogger().warning("Plugin not enabled due to invalid or outdated config");
            return;
        }

        if (getServer().getLevelByName(config.getString("worldName")) != null) {
            world = config.getString("worldName");
            getServer().getScheduler().scheduleDelayedRepeatingTask(this, new Task(), config.getInt("spawnTick"), config.getInt("spawnTick"));
        } else {
            getServer().getLogger().warning("Plugin not enabled due to invalid world name in config");
            return;
        }

        fireworkFlightDuration = config.getInt("fireworkFlightDuration");
        muteSpawnSound = config.getBoolean("muteSpawnSound");
        muteExplodeSound = config.getBoolean("muteExplodeSound");
        splitGroups = config.getBoolean("splitGroups");

        for (int i = 0; i != config.getInt("positionCount"); i++) {
            positions.add(new Vector3(config.getInt("pos" + i + ".x"), config.getInt("pos" + i + ".y"), config.getInt("pos" + i + ".z")));
        }
    }

    static void spawnFirework(Vector3 pos) {
        Level level = Server.getInstance().getLevelByName(world);
        FullChunk chunk = level.getChunkIfLoaded(pos.getChunkX(), pos.getChunkZ());

        if (chunk != null) {
            ItemFirework item = new ItemFirework();
            CompoundTag tag = new CompoundTag();
            CompoundTag ex = new CompoundTag()
                    .putByteArray("FireworkColor", new byte[]{(byte) DyeColor.values()[random.nextInt(FireworkExplosion.ExplosionType.values().length)].getDyeData()})
                    .putByteArray("FireworkFade", new byte[]{})
                    .putBoolean("FireworkFlicker", random.nextBoolean())
                    .putBoolean("FireworkTrail", random.nextBoolean())
                    .putByte("FireworkType", FireworkExplosion.ExplosionType.values()[random.nextInt(FireworkExplosion.ExplosionType.values().length)].ordinal());
            tag.putCompound("Fireworks", new CompoundTag("Fireworks")
                    .putList(new ListTag<CompoundTag>("Explosions").add(ex))
                    .putByte("Flight", fireworkFlightDuration));
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

            new EntityFireworkCustom(chunk, nbt).spawnToAll();
        }
    }
}

class Task implements Runnable {

    private boolean tick;

    @Override
    public void run() {
        if (!Server.getInstance().getLevelByName(Main.world).getPlayers().isEmpty()) {
            if (Main.splitGroups) {
                if (tick) {
                      for (int i = 0; i != (Main.positions.size() >> 1); i++) {
                        Main.spawnFirework(Main.positions.get(i));
                    }
                } else {
                    for (int i = (Main.positions.size() >> 1); i != Main.positions.size(); i++) {
                        Main.spawnFirework(Main.positions.get(i));
                    }
                }

                tick = !tick;
            } else {
                for (Vector3 pos : Main.positions) {
                    Main.spawnFirework(pos);
                }
            }
        }
    }
}
