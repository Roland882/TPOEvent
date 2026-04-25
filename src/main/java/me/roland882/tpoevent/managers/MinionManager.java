package me.roland882.tpoevent.managers;

import me.roland882.tpoevent.TPOEvent;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MinionManager implements Listener {
    public TPOEvent plugin;
    public static List<NPC> minions = new ArrayList<>();
    public MiniMessage mm = MiniMessage.miniMessage();
    public static List<org.bukkit.entity.Player> bminions = new ArrayList<>();
    public NamespacedKey key;
    public boolean attack = false;
    public UUID gUUID;
    public NPCRegistry npcReg = null;
    public boolean citA = false;
    public Map<Player, UUID> o = new HashMap<>();
    public Set<Player> eatP = new HashSet<>();

    public MinionManager(TPOEvent plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "ahorn");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        checkCit();
    }

    public void checkCit() {
        if (Bukkit.getPluginManager().isPluginEnabled("Citizens")) {
            try {
                this.npcReg = CitizensAPI.getNPCRegistry();
                this.citA = true;
                plugin.getLogger().info("Successfully hooked into Citizens API!");
            } catch (IllegalStateException e) {
                plugin.getLogger().warning("Citizens API is not ready yet: " + e.getMessage());
                Bukkit.getScheduler().runTaskLater(plugin, this::checkCit, 40L);
            }
        } else {
            plugin.getLogger().warning("Citizens plugin not found or not enabled!");
            Bukkit.getScheduler().runTaskLater(plugin, this::checkCit, 100L);
        }
    }

    public NPCRegistry getNPCReg() {
        if (npcReg == null && citA) {
            try {
                this.npcReg = CitizensAPI.getNPCRegistry();
            } catch (IllegalStateException e) {
                plugin.getLogger().warning("Failed to get Citizens registry: " + e.getMessage());
            }
        }
        return npcReg;
    }

    public void spawnMin(Location loc, String dname, Player owner) {
        if (!citA || getNPCReg() == null) {
            owner.sendMessage("§cCitizens NPC system is not ready yet. Please wait a moment.");
            plugin.getLogger().warning("Attempted to spawn minion but Citizens is not available!");
            return;
        }
        this.gUUID = owner.getUniqueId();

        NPC npc = getNPCReg().createNPC(EntityType.PLAYER, dname);

        npc.data().setPersistent("player-skin-name", owner.getName());
        npc.data().setPersistent("player-skin-uuid", owner.getUniqueId().toString());

        CitizensAPI.getTraitFactory();

        npc.spawn(loc);
        npc.setProtected(true);

        npc.getNavigator().getLocalParameters()
                .baseSpeed(0.3f)
                .attackRange(3.0)
                .attackDelayTicks(20)
                .straightLineTargetingDistance(10);

        if (npc.getEntity() instanceof org.bukkit.entity.Player npcP) {
            o.put(npcP, owner.getUniqueId());

            npcP.customName(Component.text(dname));
            npcP.setCustomNameVisible(true);
            npcP.setAI(false);
            npcP.setInvulnerable(true);
            npcP.setGameMode(GameMode.SURVIVAL);

            npcP.setRotation(loc.getYaw(), loc.getPitch());

            Objects.requireNonNull(npcP.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0.3);
            Objects.requireNonNull(npcP.getAttribute(Attribute.ATTACK_DAMAGE)).setBaseValue(10.0);
            Objects.requireNonNull(npcP.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(50.0);
            Objects.requireNonNull(npcP.getAttribute(Attribute.ATTACK_SPEED)).setBaseValue(3.5);
            npcP.setHealth(50.0);

            bminions.add(npcP);
        }
        minions.add(npc);
    }

    public void desAllMin() {
        for (NPC npc : new ArrayList<>(minions)) {
            if (npc.isSpawned()) {
                npc.despawn();
            }
            npc.destroy();
        }
        minions.clear();
        bminions.clear();
        o.clear();
    }
    public void setAttack(boolean enabled) {
        this.attack = enabled;

        if (enabled) {
            for (NPC npc : minions) {
                if (npc.isSpawned() && npc.getEntity() instanceof org.bukkit.entity.Player npcP) {
                    npcP.setAI(true);
                    npcP.setInvulnerable(false);
                    npcP.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 9999990, 2));
                    npcP.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 9999990, 2));
                    npcP.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 9999990, 1));
                    npc.setProtected(false);
                    npc.getNavigator().setPaused(false);
                    findAndAttackNear(npc);
                }
            }
        } else {
            for (NPC npc : minions) {
                if (npc.isSpawned() && npc.getEntity() instanceof org.bukkit.entity.Player npcP) {
                    npcP.setAI(false);
                    npcP.setInvulnerable(true);
                    npcP.getActivePotionEffects().clear();
                    npc.setProtected(true);
                    npc.getNavigator().setPaused(true);
                    npc.getNavigator().cancelNavigation();
                }
            }
        }
    }

    public void findAndAttackNear(NPC npc) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!attack || !npc.isSpawned() || !(npc.getEntity() instanceof org.bukkit.entity.Player npcP)) {
                    this.cancel();
                    return;
                }

                Player near = null;
                double nearD = Double.MAX_VALUE;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getUniqueId().equals(gUUID) || player.equals(npcP)) continue;

                    double distance = player.getLocation().distance(npcP.getLocation());
                    if (distance < nearD && distance < 30) {
                        nearD = distance;
                        near = player;
                    }
                }

                if (near != null) {
                    npc.getNavigator().setTarget(near, nearD < 15);
                }
            }
        }.runTaskTimer(plugin, 0L, 40L);
    }

    @EventHandler
    public void onDmg(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player npcP)) return;

        NPC npc = CitizensAPI.getNPCRegistry().getNPC(npcP);
        if (npc == null || !minions.contains(npc)) return;

        if (event.getDamager() instanceof Player attacker) {
            UUID ownerUUID = o.get(npcP);
            if (ownerUUID != null && attacker.getUniqueId().equals(ownerUUID)) {
                event.setCancelled(true);
                attacker.sendMessage(Component.text("§eA saját goonodat nem támadhatod!"));
                return;
            }
            if (attack && !eatP.contains(npcP)) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (attacker.isOnline() && npc.isSpawned()) {
                            attacker.damage(8.0, npcP);
                            npcP.swingMainHand();
                            npcP.getWorld().playSound(npcP.getLocation(),
                                    Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 1.0f);
                        }
                    }
                }.runTaskLater(plugin, 5L);
            }
        }

        if (!eatP.contains(npcP) && npcP.getHealth() - event.getFinalDamage() < 12) {
            checkAndEatGAP(npcP);
        }
    }
    public void checkAndEatGAP(Player npcP) {
        if (!hasGAP(npcP)) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!npcP.isValid() || npcP.isDead()) return;

                npcP.swingMainHand();
                npcP.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
                npcP.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 1));

                for (ItemStack i : npcP.getInventory().getContents()) {
                    if (i != null && (i.getType() == Material.GOLDEN_APPLE ||
                            i.getType() == Material.ENCHANTED_GOLDEN_APPLE)) {
                        if (i.getAmount() <= 1) {
                            npcP.getInventory().remove(i);
                        } else {
                            i.setAmount(i.getAmount() - 1);
                        }
                        break;
                    }
                }

                npcP.getWorld().spawnParticle(Particle.HEART,
                        npcP.getLocation().add(0, 2, 0), 5);
                npcP.getWorld().playSound(npcP.getLocation(),
                        Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
            }
        }.runTaskLater(plugin, 20L);
    }

    public boolean hasGAP(Player player) {
        for (ItemStack i : player.getInventory().getContents()) {
            if (i != null && (i.getType() == Material.GOLDEN_APPLE ||
                    i.getType() == Material.ENCHANTED_GOLDEN_APPLE)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.Player minion)) return;

        NPC npc = CitizensAPI.getNPCRegistry().getNPC(minion);
        if (npc == null || !minions.contains(npc)) return;


        if (npc.isSpawned()) {
            npc.despawn();
            npc.destroy();
        }
        npc.destroy();
        minions.remove(npc);
        bminions.remove(minion);
        o.remove(minion);
    }
    @EventHandler
    public void onPop(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.Player minion)) return;

        NPC npc = CitizensAPI.getNPCRegistry().getNPC(minion);
        if (npc == null || !minions.contains(npc)) return;

        PlayerInventory inv = minion.getInventory();
        ItemStack off = inv.getItemInOffHand();
        boolean foundTotem = false;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() == Material.TOTEM_OF_UNDYING) {
                if (off.getType() == Material.AIR) {
                    inv.setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING, 1));
                } else if (off.getType() == Material.TOTEM_OF_UNDYING) {
                    off.setAmount(off.getAmount() + 1);
                }
                item.setAmount(item.getAmount() - 1);
                if (item.getAmount() <= 0) {
                    inv.setItem(i, null);
                }
                foundTotem = true;
                break;
            }
        }

        if (!foundTotem) {
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && item.getType() == Material.SHIELD) {
                    inv.setItemInOffHand(item.clone());
                    inv.setItem(i, null);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (e.getPlayer().getName().startsWith("CIT-")) {
            e.quitMessage(null);
        }
    }


    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        Player victim = event.getEntity();
        Component ori = event.deathMessage();
        if (ori == null) return;

        Component newMsg = ori;
        if (killer != null) {
            var kL = killer.getInventory().getLeggings();
            if (kL != null && kL.hasItemMeta() && kL.getItemMeta().hasDisplayName()) {
                String kLN = LegacyComponentSerializer.legacySection()
                        .serialize(Objects.requireNonNull(kL.getItemMeta().displayName()));
                if (kLN.contains("Grinch's Leggings")) {
                    newMsg = newMsg.replaceText(builder ->
                            builder.matchLiteral(killer.getName()).replacement(Component.text("The Grinch"))
                    );
                }
                if (kLN.contains("Grinch Helper")) {
                    newMsg = newMsg.replaceText(builder ->
                            builder.matchLiteral(killer.getName()).replacement(Component.text("The Grinch's Helper"))
                    );
                }
            }
        }

        var vL = victim.getInventory().getLeggings();
        if (vL != null && vL.hasItemMeta() && vL.getItemMeta().hasDisplayName()) {
            String vLN = LegacyComponentSerializer.legacySection()
                    .serialize(Objects.requireNonNull(vL.getItemMeta().displayName()));
            if (vLN.contains("Grinch's Leggings")) {
                newMsg = newMsg.replaceText(builder ->
                        builder.matchLiteral(victim.getName()).replacement(Component.text("The Grinch"))
                );
            }
            if (vLN.contains("Grinch Helper")) {
                newMsg = newMsg.replaceText(builder ->
                        builder.matchLiteral(victim.getName()).replacement(Component.text("The Grinch's Helper"))
                );
            }
        }
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getEntity());
        if (npc != null && minions.contains(npc)) {
            npc.destroy();
            minions.remove(npc);

            String npcN = npc.getName();
            newMsg = newMsg.replaceText(builder ->
                    builder.matchLiteral(npcN)
                            .replacement(Component.text(npcN  + " ", net.kyori.adventure.text.format.NamedTextColor.WHITE))
            );
        }
        event.deathMessage(newMsg);
    }
    @EventHandler
    public void onHorn(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack i = player.getInventory().getItemInMainHand();

        if (i.getType() != Material.GOAT_HORN) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().getName().equalsIgnoreCase("Roland882")) {
            ItemMeta meta = i.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                player.getWorld().playSound(
                        player.getLocation(),
                        Sound.ITEM_GOAT_HORN_SOUND_0,
                        SoundCategory.PLAYERS,
                        1.0f,
                        1.0f
                );
                setAttack(true);
            }
        }
    }

    public ItemStack ahorn() {
        ItemStack horn = new ItemStack(Material.GOAT_HORN);
        ItemMeta meta = horn.getItemMeta();

        meta.displayName(mm.deserialize("<!italic><red>Attack Horn"));
        meta.lore(List.of(mm.deserialize("<!italic><green>Right Click to start the attack")));
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "ahorn");
        meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);

        horn.setItemMeta(meta);
        return horn;
    }
}