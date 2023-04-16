package net.momirealms.customcrops.api.customplugin;

import net.momirealms.customcrops.CustomCrops;
import net.momirealms.customcrops.api.CustomCropsAPI;
import net.momirealms.customcrops.api.customplugin.itemsadder.ItemsAdderHandler;
import net.momirealms.customcrops.api.customplugin.oraxen.OraxenHandler;
import net.momirealms.customcrops.api.object.BoneMeal;
import net.momirealms.customcrops.api.object.Function;
import net.momirealms.customcrops.api.object.InteractWithItem;
import net.momirealms.customcrops.api.object.ItemType;
import net.momirealms.customcrops.api.object.fill.PassiveFillMethod;
import net.momirealms.customcrops.api.object.action.Action;
import net.momirealms.customcrops.api.object.basic.ConfigManager;
import net.momirealms.customcrops.api.object.basic.MessageManager;
import net.momirealms.customcrops.api.object.crop.CropConfig;
import net.momirealms.customcrops.api.object.crop.GrowingCrop;
import net.momirealms.customcrops.api.object.crop.StageConfig;
import net.momirealms.customcrops.api.object.fertilizer.Fertilizer;
import net.momirealms.customcrops.api.object.fertilizer.FertilizerConfig;
import net.momirealms.customcrops.api.object.fill.PositiveFillMethod;
import net.momirealms.customcrops.api.object.pot.Pot;
import net.momirealms.customcrops.api.object.pot.PotConfig;
import net.momirealms.customcrops.api.object.requirement.CurrentState;
import net.momirealms.customcrops.api.object.requirement.Requirement;
import net.momirealms.customcrops.api.object.sprinkler.SprinklerConfig;
import net.momirealms.customcrops.api.object.wateringcan.WateringCanConfig;
import net.momirealms.customcrops.api.object.world.SimpleLocation;
import net.momirealms.customcrops.api.util.AdventureUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class PlatformManager extends Function {

    private final CustomCrops plugin;
    private final Handler handler;

    public PlatformManager(CustomCrops plugin) {
        this.plugin = plugin;
        this.handler = switch (plugin.getPlatform()) {
            case ItemsAdder -> new ItemsAdderHandler(this);
            case Oraxen -> new OraxenHandler(this);
        };
    }

    @Override
    public void load() {
        this.handler.load();
    }

    @Override
    public void unload() {
        this.handler.unload();
    }

    public void onBreakTripWire(Player player, Block block, String id, Cancellable event) {
        if (event.isCancelled()) return;
        onBreakSomething(player, block.getLocation(), id, event);
    }

    public void onBreakNoteBlock(Player player, Block block, String id, Cancellable event) {
        if (event.isCancelled()) return;
        onBreakSomething(player, block.getLocation(), id, event);
    }

    public void onBreakItemDisplay(Player player, Entity entity, String id, Cancellable event) {
        if (event.isCancelled()) return;
        onBreakSomething(player, entity.getLocation().getBlock().getLocation(), id, event);
    }

    public void onBreakItemFrame(Player player, Entity entity, String id, Cancellable event) {
        if (event.isCancelled()) return;
        onBreakSomething(player, entity.getLocation().getBlock().getLocation(), id, event);
    }

    public void onPlaceFurniture(Location location, String id) {
        onPlaceSomething(location, id);
    }

    public void onPlaceBlock(Location location, String id, Cancellable event) {
        if (event.isCancelled()) return;
        onPlaceSomething(location, id);
    }

    public void onInteractBlock(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
            onInteractAir(event.getPlayer());
        } else if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            String id = plugin.getPlatformInterface().getBlockID(block);
            assert block != null;
            onInteractSomething(event.getPlayer(), block.getLocation(), id, event);
        }
    }

    public void onInteractFurniture(Player player, Entity entity, String id, Cancellable event) {
        if (event.isCancelled()) return;
        onInteractSomething(player, entity.getLocation().getBlock().getLocation(), id, event);
    }

    @NotNull
    public ItemType onInteractAir(Player player) {
        ItemStack item_in_hand = player.getInventory().getItemInMainHand();
        String id = plugin.getPlatformInterface().getItemID(item_in_hand);

        if (onInteractWithWateringCan(player, id, item_in_hand)) {
            return ItemType.WATERINGCAN;
        }

        return ItemType.UNKNOWN;
    }

    @NotNull
    public ItemType onBreakSomething(Player player, Location location, String id, Cancellable event) {

        if (onBreakGlass(id, location)) {
            return ItemType.GLASS;
        }

        if (onBreakPot(player, id, location, event)) {
            return ItemType.POT;
        }

        if (onBreakCrop(player, id, location, event)) {
            return ItemType.CROP;
        }

        if (onBreakSprinkler(id, location)) {
            return ItemType.SPRINKLER;
        }

        if (onBreakScarecrow(id, location)) {
            return ItemType.SCARECROW;
        }

        return ItemType.UNKNOWN;
    }

    @NotNull
    public ItemType onPlaceSomething(Location location, String id) {

        if (onPlaceGlass(id, location)) {
            return ItemType.GLASS;
        }

        if (onPlacePot(id, location)) {
            return ItemType.POT;
        }

        if (onPlaceScarecrow(id, location)) {
            return ItemType.SCARECROW;
        }

        return ItemType.UNKNOWN;
    }

    @NotNull ItemType onInteractSomething(Player player, Location location, String id, Cancellable event) {

        ItemStack item_in_hand = player.getInventory().getItemInMainHand();
        String item_in_hand_id = plugin.getPlatformInterface().getItemID(item_in_hand);

        if (onInteractWithSprinkler(player, location, item_in_hand, item_in_hand_id)) {
            return ItemType.SPRINKLER;
        }

        if (onInteractSprinkler(player, id, location, item_in_hand, item_in_hand_id)) {
            return ItemType.SPRINKLER;
        }

        if (onInteractPot(player, id, location, item_in_hand, item_in_hand_id)) {
            return ItemType.POT;
        }

        if (onInteractCrop(player, id, location, item_in_hand, item_in_hand_id)) {
            return ItemType.CROP;
        }

        if (onInteractWithWateringCan(player, item_in_hand_id, item_in_hand)) {
            return ItemType.WATERINGCAN;
        }

        return ItemType.UNKNOWN;
    }

    public boolean onBreakGlass(String id, Location location) {
        if (!id.equals(ConfigManager.greenhouseBlock)) {
            return false;
        }

        plugin.getWorldDataManager().removeGreenhouse(SimpleLocation.getByBukkitLocation(location));
        return true;
    }

    public boolean onPlaceGlass(String id, Location location) {
        if (!id.equals(ConfigManager.greenhouseBlock)) {
            return false;
        }

        plugin.getWorldDataManager().addGreenhouse(SimpleLocation.getByBukkitLocation(location));
        return true;
    }

    public boolean onBreakScarecrow(String id, Location location) {
        if (!id.equals(ConfigManager.scarecrow)) {
            return false;
        }

        plugin.getWorldDataManager().removeScarecrow(SimpleLocation.getByBukkitLocation(location));
        return true;
    }

    public boolean onPlaceScarecrow(String id, Location location) {
        if (!id.equals(ConfigManager.scarecrow)) {
            return false;
        }

        plugin.getWorldDataManager().addScarecrow(SimpleLocation.getByBukkitLocation(location));
        return true;
    }

    private boolean onPlacePot(String id, Location location) {
        String pot_id = plugin.getPotManager().getPotKeyByBlockID(id);
        if (pot_id == null) return false;

        plugin.getWorldDataManager().addPotData(SimpleLocation.getByBukkitLocation(location), new Pot(pot_id, null, 0));
        return true;
    }

    public boolean onInteractSprinkler(Player player, String id, Location location, ItemStack item_in_hand, String item_in_hand_id) {
        SprinklerConfig sprinklerConfig = plugin.getSprinklerManager().getConfigByItemID(id);
        if (sprinklerConfig == null) {
            return false;
        }

        // water
        PassiveFillMethod[] passiveFillMethods = sprinklerConfig.getPassiveFillMethods();
        for (PassiveFillMethod passiveFillMethod : passiveFillMethods) {
            if (passiveFillMethod.isRightItem(item_in_hand_id)) {
                doPassiveFillAction(player, item_in_hand, passiveFillMethod, location.clone().add(0,0.2,0));
                plugin.getWorldDataManager().addWaterToSprinkler(SimpleLocation.getByBukkitLocation(location), passiveFillMethod.getAmount(), sprinklerConfig.getRange(), sprinklerConfig.getStorage());
                return true;
            }
        }

        WateringCanConfig wateringCanConfig = plugin.getWateringCanManager().getConfigByItemID(item_in_hand_id);
        if (wateringCanConfig != null) {
            String[] sprinkler_whitelist = wateringCanConfig.getSprinklerWhitelist();
            if (sprinkler_whitelist != null) {
                outer: {
                    for (String sprinkler_allowed : sprinkler_whitelist) {
                        if (sprinkler_allowed.equals(plugin.getSprinklerManager().getConfigKeyByItemID(id))) {
                            break outer;
                        }
                    }
                    return true;
                }
            }
            int current_water = plugin.getWateringCanManager().getCurrentWater(item_in_hand);
            if (current_water <= 0) return true;

            //TODO API Events

            current_water--;
            if (wateringCanConfig.hasActionBar()) {
                AdventureUtils.playerActionbar(player, wateringCanConfig.getActionBarMsg(current_water));
            }
            if (wateringCanConfig.getSound() != null) {
                AdventureUtils.playerSound(player, wateringCanConfig.getSound());
            }
            if (wateringCanConfig.getParticle() != null) {
                location.getWorld().spawnParticle(wateringCanConfig.getParticle(), location.clone().add(0.5,0.4, 0.5),5,0.3,0.1,0.3);
            }

            plugin.getWateringCanManager().setWater(item_in_hand, current_water, wateringCanConfig);
            plugin.getWorldDataManager().addWaterToSprinkler(SimpleLocation.getByBukkitLocation(location), 1, sprinklerConfig.getRange(), sprinklerConfig.getStorage());
            return true;
        }

        return true;
    }

    private void doPassiveFillAction(Player player, ItemStack item_in_hand, PassiveFillMethod passiveFillMethod, Location location) {
        if (player.getGameMode() != GameMode.CREATIVE) {
            item_in_hand.setAmount(item_in_hand.getAmount() - 1);
            ItemStack returned = passiveFillMethod.getReturnedItemStack();
            if (returned != null) {
                player.getInventory().addItem(returned);
            }
        }
        if (passiveFillMethod.getSound() != null) {
            AdventureUtils.playerSound(player, passiveFillMethod.getSound());
        }
        if (passiveFillMethod.getParticle() != null) {
            location.getWorld().spawnParticle(passiveFillMethod.getParticle(), location.clone().add(0.5,0.4, 0.5),5,0.3,0.1,0.3);
        }
    }

    public boolean onInteractWithSprinkler(Player player, Location location, ItemStack item_in_hand, String item_in_hand_id) {
        SprinklerConfig sprinklerConfig = plugin.getSprinklerManager().getConfigByItemID(item_in_hand_id);
        if (sprinklerConfig == null) {
            return false;
        }

        Location sprinkler_loc = location.clone().add(0,1,0);
        if (plugin.getPlatformInterface().detectAnyThing(sprinkler_loc)) return true;

        if (player.getGameMode() != GameMode.CREATIVE) item_in_hand.setAmount(item_in_hand.getAmount() - 1);
        CustomCropsAPI.getInstance().placeCustomItem(sprinkler_loc, sprinklerConfig.getThreeD(), sprinklerConfig.getItemMode());
        if (sprinklerConfig.getSound() != null) {
            AdventureUtils.playerSound(player, sprinklerConfig.getSound());
        }
        return true;
    }

    public boolean onInteractCrop(Player player, String id, Location location, ItemStack item_in_hand, String item_in_hand_id) {
        CropConfig cropConfig = plugin.getCropManager().getCropConfigByStage(id);
        if (cropConfig == null) {
            return false;
        }

        StageConfig stageConfig = plugin.getCropManager().getStageConfig(id);
        if (stageConfig == null) {
            return true;
        }

        if (item_in_hand_id.equals("AIR")) {
            Action[] actions = stageConfig.getInteractByHandActions();
            if (actions != null) {
                for (Action action : actions) {
                    action.doOn(player, SimpleLocation.getByBukkitLocation(location), cropConfig.getCropMode());
                }
            }
            return true;
        }

        WateringCanConfig wateringCanConfig = plugin.getWateringCanManager().getConfigByItemID(item_in_hand_id);
        Pot potData = plugin.getWorldDataManager().getPotData(SimpleLocation.getByBukkitLocation(location).add(0,-1,0));
        if (wateringCanConfig != null && potData != null) {
            String[] pot_whitelist = wateringCanConfig.getPotWhitelist();
            if (pot_whitelist != null) {
                outer: {
                    for (String pot : pot_whitelist) {
                        if (pot.equals(potData.getPotKey())) {
                            break outer;
                        }
                    }
                    return true;
                }
            }
            int current_water = plugin.getWateringCanManager().getCurrentWater(item_in_hand);
            if (current_water <= 0) return true;

            //TODO API Events

            current_water--;
            this.waterPot(wateringCanConfig.getWidth(), wateringCanConfig.getLength(), location.clone().subtract(0,1,0), player.getLocation().getYaw(), potData.getPotKey(), wateringCanConfig.getParticle());
            if (wateringCanConfig.hasActionBar()) {
                AdventureUtils.playerActionbar(player, wateringCanConfig.getActionBarMsg(current_water));
            }
            if (wateringCanConfig.getSound() != null) {
                AdventureUtils.playerSound(player, wateringCanConfig.getSound());
            }
            plugin.getWateringCanManager().setWater(item_in_hand, current_water, wateringCanConfig);
            return true;
        }

        BoneMeal[] boneMeals = cropConfig.getBoneMeals();
        if (boneMeals != null) {
            for (BoneMeal boneMeal : boneMeals) {
                if (boneMeal.isRightItem(item_in_hand_id)) {
                    if (plugin.getWorldDataManager().addCropPointAt(SimpleLocation.getByBukkitLocation(location), boneMeal.getPoint())) {
                        if (player.getGameMode() != GameMode.CREATIVE) {
                            item_in_hand.setAmount(item_in_hand.getAmount() - 1);
                            if (boneMeal.getReturned() != null) {
                                player.getInventory().addItem(boneMeal.getReturned());
                            }
                        }
                        if (boneMeal.getParticle() != null) {
                            location.getWorld().spawnParticle(boneMeal.getParticle(), location.clone().add(0.5,0.5, 0.5),3,0.4,0.4,0.4);
                        }
                        if (boneMeal.getSound() != null) {
                            AdventureUtils.playerSound(player, boneMeal.getSound());
                        }
                    }
                    return true;
                }
            }
        }

        InteractWithItem[] interactActions = stageConfig.getInteractActions();
        if (interactActions != null) {
            for (InteractWithItem interactWithItem : interactActions) {
                if (interactWithItem.isRightItem(item_in_hand_id)) {
                    if (player.getGameMode() != GameMode.CREATIVE) {
                        if (interactWithItem.isConsumed()) {
                            item_in_hand.setAmount(item_in_hand.getAmount() - 1);
                        }
                        if (interactWithItem.getReturned() != null) {
                            player.getInventory().addItem(interactWithItem.getReturned());
                        }
                    }
                    Action[] inAc = interactWithItem.getActions();
                    if (inAc != null) {
                        for (Action action : inAc) {
                            action.doOn(player, SimpleLocation.getByBukkitLocation(location), cropConfig.getCropMode());
                        }
                    }
                    return true;
                }
            }
        }
        return true;
    }

    public boolean onInteractPot(Player player, String id, Location location, ItemStack item_in_hand, String item_in_hand_id) {
        String pot_id = plugin.getPotManager().getPotKeyByBlockID(id);
        if (pot_id == null) {
            return false;
        }

        PotConfig potConfig = plugin.getPotManager().getPotConfig(pot_id);
        if (potConfig == null) {
            return false;
        }

        // water
        PassiveFillMethod[] passiveFillMethods = potConfig.getPassiveFillMethods();
        for (PassiveFillMethod passiveFillMethod : passiveFillMethods) {
            if (passiveFillMethod.isRightItem(item_in_hand_id)) {
                doPassiveFillAction(player, item_in_hand, passiveFillMethod, location);
                plugin.getWorldDataManager().addWaterToPot(SimpleLocation.getByBukkitLocation(location), passiveFillMethod.getAmount(), pot_id);
                return true;
            }
        }

        CropConfig cropConfig = plugin.getCropManager().getCropConfigBySeed(item_in_hand_id);
        if (cropConfig != null) {
            String[] pot_whitelist = cropConfig.getBottom_blocks();
            outer: {
                for (String bottom_block : pot_whitelist) {
                    if (bottom_block.equals(pot_id)) {
                        break outer;
                    }
                }
                AdventureUtils.playerMessage(player, MessageManager.prefix + MessageManager.unsuitablePot);
                return true;
            }

            Location crop_loc = location.clone().add(0,1,0);
            Requirement[] requirements = cropConfig.getPlantRequirements();
            if (requirements != null) {
                CurrentState currentState = new CurrentState(crop_loc, player);
                for (Requirement requirement : requirements) {
                    if (!requirement.isConditionMet(currentState)) {
                        return true;
                    }
                }
            }

            if (plugin.getPlatformInterface().detectAnyThing(crop_loc)) return true;
            if (ConfigManager.enableLimitation && plugin.getWorldDataManager().getChunkCropAmount(SimpleLocation.getByBukkitLocation(crop_loc)) >= ConfigManager.maxCropPerChunk) {
                AdventureUtils.playerMessage(player, MessageManager.prefix + MessageManager.reachChunkLimit);
                return true;
            }

            if (player.getGameMode() != GameMode.CREATIVE) item_in_hand.setAmount(item_in_hand.getAmount() - 1);
            player.swingMainHand();
            CustomCropsAPI.getInstance().placeCustomItem(crop_loc, Objects.requireNonNull(cropConfig.getStageConfig(0)).getModel(), cropConfig.getCropMode());
            plugin.getWorldDataManager().addCropData(SimpleLocation.getByBukkitLocation(crop_loc), new GrowingCrop(cropConfig.getKey(), 0));
            return true;
        }

        // use fertilizer
        FertilizerConfig fertilizerConfig = plugin.getFertilizerManager().getConfigByItemID(item_in_hand_id);
        if (fertilizerConfig != null) {
            if (fertilizerConfig.isBeforePlant() && plugin.getCropManager().containsStage(plugin.getPlatformInterface().getCustomItemAt(location.clone().add(0,1,0)))) {
                AdventureUtils.playerMessage(player, MessageManager.prefix + MessageManager.beforePlant);
                return true;
            }
            if (player.getGameMode() != GameMode.CREATIVE) item_in_hand.setAmount(item_in_hand.getAmount() - 1);
            player.swingMainHand();
            if (fertilizerConfig.getSound() != null) {
                AdventureUtils.playerSound(player, fertilizerConfig.getSound());
            }
            if (fertilizerConfig.getParticle() != null) {
                location.getWorld().spawnParticle(fertilizerConfig.getParticle(), location.clone().add(0.5,1.1,0.5), 5,0.25,0.1,0.25, 0);
            }
            plugin.getWorldDataManager().addFertilizerToPot(SimpleLocation.getByBukkitLocation(location), new Fertilizer(fertilizerConfig), pot_id);
            return true;
        }

        // use watering can
        WateringCanConfig wateringCanConfig = plugin.getWateringCanManager().getConfigByItemID(item_in_hand_id);
        if (wateringCanConfig != null) {
            String[] pot_whitelist = wateringCanConfig.getPotWhitelist();
            if (pot_whitelist != null) {
                outer: {
                    for (String pot : pot_whitelist) {
                        if (pot.equals(pot_id)) {
                            break outer;
                        }
                    }
                    return true;
                }
            }

            int current_water = plugin.getWateringCanManager().getCurrentWater(item_in_hand);
            if (current_water <= 0) return true;

            //TODO API Events

            current_water--;
            this.waterPot(wateringCanConfig.getWidth(), wateringCanConfig.getLength(), location, player.getLocation().getYaw(), pot_id, wateringCanConfig.getParticle());

            if (wateringCanConfig.hasActionBar()) {
                AdventureUtils.playerActionbar(player, wateringCanConfig.getActionBarMsg(current_water));
            }
            if (wateringCanConfig.getSound() != null) {
                AdventureUtils.playerSound(player, wateringCanConfig.getSound());
            }

            plugin.getWateringCanManager().setWater(item_in_hand, current_water, wateringCanConfig);
            return true;
        }
        return true;
    }

    public boolean onBreakPot(Player player, String id, Location location, Cancellable event) {
        if (!plugin.getPotManager().containsPotBlock(id)) {
            return false;
        }

        Location above_loc = location.clone().add(0,1,0);
        String above_id = plugin.getPlatformInterface().getCustomItemAt(above_loc);
        // has item above
        if (above_id != null) {
            // is a crop
            if (onBreakCrop(player, above_id, above_loc, event)) {
                // The event might be cancelled if the player doesn't meet the break requirements
                if (event.isCancelled()) {
                    return true;
                }
                plugin.getPlatformInterface().removeCustomItemAt(above_loc);
            }
        }

        plugin.getWorldDataManager().removePotData(SimpleLocation.getByBukkitLocation(location));
        return true;
    }

    private boolean onBreakSprinkler(String id, Location location) {
        if (!plugin.getSprinklerManager().containsSprinkler(id)) {
            return false;
        }

        plugin.getWorldDataManager().removeSprinklerData(SimpleLocation.getByBukkitLocation(location));
        return true;
    }

    private boolean onBreakCrop(Player player, String id, Location location, Cancellable event) {
        if (plugin.getCropManager().isDeadCrop(id)) {
            return true;
        }

        CropConfig cropConfig = plugin.getCropManager().getCropConfigByStage(id);
        if (cropConfig == null) return false;

        if (!canBreak(player, cropConfig, location)) {
            event.setCancelled(true);
            return true;
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            StageConfig stageConfig = plugin.getCropManager().getStageConfig(id);
            if (stageConfig != null) {
                Action[] breakActions = stageConfig.getBreakActions();
                if (breakActions != null) {
                    for (Action action : breakActions) {
                        action.doOn(player, SimpleLocation.getByBukkitLocation(location), cropConfig.getCropMode());
                    }
                }
            }
        }

        plugin.getWorldDataManager().removeCropData(SimpleLocation.getByBukkitLocation(location));
        return true;
    }

    public boolean canBreak(Player player, CropConfig cropConfig, Location crop_loc) {
        Requirement[] requirements = cropConfig.getBreakRequirements();
        if (requirements == null) return true;
        CurrentState currentState = new CurrentState(crop_loc, player);
        for (Requirement requirement : requirements) {
            if (!requirement.isConditionMet(currentState)) {
                return false;
            }
        }
        return true;
    }

    private void waterPot(int width, int length, Location location, float yaw, String id, @Nullable Particle particle){
        int extend = width / 2;
        if (yaw < 45 && yaw > -135) {
            if (yaw > -45) {
                for (int i = -extend; i <= extend; i++) {
                    Location tempLoc = location.clone().add(i, 0, -1);
                    for (int j = 0; j < length; j++){
                        tempLoc.add(0,0,1);
                        tryToWaterPot(tempLoc, id, particle);
                    }
                }
            }
            else {
                for (int i = -extend; i <= extend; i++) {
                    Location tempLoc = location.clone().add(-1, 0, i);
                    for (int j = 0; j < length; j++){
                        tempLoc.add(1,0,0);
                        tryToWaterPot(tempLoc, id, particle);
                    }
                }
            }
        }
        else {
            if (yaw > 45 && yaw < 135) {
                for (int i = -extend; i <= extend; i++) {
                    Location tempLoc = location.clone().add(1, 0, i);
                    for (int j = 0; j < length; j++){
                        tempLoc.subtract(1,0,0);
                        tryToWaterPot(tempLoc, id, particle);
                    }
                }
            }
            else {
                for (int i = -extend; i <= extend; i++) {
                    Location tempLoc = location.clone().add(i, 0, 1);
                    for (int j = 0; j < length; j++){
                        tempLoc.subtract(0,0,1);
                        tryToWaterPot(tempLoc, id, particle);
                    }
                }
            }
        }
    }

    private void tryToWaterPot(Location location, String pot_id, @Nullable Particle particle) {
        String blockID = plugin.getPlatformInterface().getBlockID(location.getBlock());
        String current_id = plugin.getPotManager().getPotKeyByBlockID(blockID);
        if (current_id != null && current_id.equals(pot_id)) {
            plugin.getWorldDataManager().addWaterToPot(SimpleLocation.getByBukkitLocation(location), 1, pot_id);
            if (particle != null)
                location.getWorld().spawnParticle(particle, location.clone().add(0.5,1, 0.5),3,0.1,0.1,0.1);
        }
    }

    public boolean onInteractWithWateringCan(Player player, String item_in_hand_id, ItemStack item_in_hand) {
        WateringCanConfig wateringCanConfig = plugin.getWateringCanManager().getConfigByItemID(item_in_hand_id);
        if (wateringCanConfig == null) {
            return false;
        }

        int current = plugin.getWateringCanManager().getCurrentWater(item_in_hand);
        if (current >= wateringCanConfig.getStorage()) return true;
        List<Block> lineOfSight = player.getLineOfSight(null, 5);
        List<String> blockIds = lineOfSight.stream().map(block -> plugin.getPlatformInterface().getBlockID(block)).toList();

        int add = 0;
        for (PositiveFillMethod positiveFillMethod : wateringCanConfig.getPositiveFillMethods()) {
            if (positiveFillMethod.getType() == PositiveFillMethod.InteractType.BLOCK) {
                int index = 0;
                for (String blockId : blockIds) {
                    if (positiveFillMethod.getId().equals(blockId)) {
                        add = positiveFillMethod.getAmount();
                        if (positiveFillMethod.getSound() != null) {
                            AdventureUtils.playerSound(player, positiveFillMethod.getSound());
                        }
                        if (positiveFillMethod.getParticle() != null) {
                            Block block = lineOfSight.get(index);
                            block.getWorld().spawnParticle(positiveFillMethod.getParticle(), block.getLocation().add(0.5,1, 0.5),5,0.1,0.1,0.1);
                        }
                        break;
                    }
                    index++;
                }
            }
        }

        if (add == 0) return true;
        int finalWater = Math.min(wateringCanConfig.getStorage(), add + current);
        plugin.getWateringCanManager().setWater(item_in_hand, finalWater, wateringCanConfig);
        if (wateringCanConfig.hasActionBar()) {
            AdventureUtils.playerActionbar(player, wateringCanConfig.getActionBarMsg(finalWater));
        }
        return true;
    }
}
