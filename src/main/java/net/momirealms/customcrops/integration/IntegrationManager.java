package net.momirealms.customcrops.integration;

import net.momirealms.customcrops.CustomCrops;
import net.momirealms.customcrops.api.object.Function;
import net.momirealms.customcrops.api.object.basic.ConfigManager;
import net.momirealms.customcrops.api.util.AdventureUtils;
import net.momirealms.customcrops.api.util.ConfigUtils;
import net.momirealms.customcrops.integration.item.DefaultImpl;
import net.momirealms.customcrops.integration.item.MMOItemsItemImpl;
import net.momirealms.customcrops.integration.job.EcoJobsImpl;
import net.momirealms.customcrops.integration.job.JobsRebornImpl;
import net.momirealms.customcrops.integration.papi.PlaceholderManager;
import net.momirealms.customcrops.integration.season.CustomCropsSeasonImpl;
import net.momirealms.customcrops.integration.season.RealisticSeasonsImpl;
import net.momirealms.customcrops.integration.skill.AureliumsImpl;
import net.momirealms.customcrops.integration.skill.EcoSkillsImpl;
import net.momirealms.customcrops.integration.skill.MMOCoreImpl;
import net.momirealms.customcrops.integration.skill.mcMMOImpl;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class IntegrationManager extends Function {

    private final CustomCrops plugin;
    private SkillInterface skillInterface;
    private JobInterface jobInterface;
    private ItemInterface[] itemInterfaces;
    private SeasonInterface seasonInterface;
    private final PluginManager pluginManager;
    private final PlaceholderManager placeholderManager;

    public IntegrationManager(CustomCrops plugin) {
        this.plugin = plugin;
        this.pluginManager = Bukkit.getPluginManager();
        this.placeholderManager = new PlaceholderManager(plugin);
    }

    @Override
    public void load() {
        this.hookJobs();
        this.hookSkills();
        this.hookSeasons();
        this.hookItems();
        this.placeholderManager.load();
    }

    @Override
    public void unload() {
        this.seasonInterface = null;
        this.skillInterface = null;
        this.itemInterfaces = null;
        this.jobInterface = null;
        this.placeholderManager.unload();
    }

    private void hookItems() {
        ArrayList<ItemInterface> itemInterfaceList = new ArrayList<>();
        if (pluginManager.isPluginEnabled("MythicMobs")) {
            itemInterfaceList.add(new MMOItemsItemImpl());
            hookMessage("MythicMobs");
        }
        if (pluginManager.isPluginEnabled("MMOItems")) {
            itemInterfaceList.add(new MMOItemsItemImpl());
            hookMessage("MMOItems");
        }
        itemInterfaceList.add(new DefaultImpl());
        this.itemInterfaces = itemInterfaceList.toArray(new ItemInterface[0]);
    }

    private void hookSeasons() {
        if (pluginManager.isPluginEnabled("RealisticSeasons")) {
            this.seasonInterface = new RealisticSeasonsImpl();
            ConfigManager.rsHook = true;
            hookMessage("RealisticSeasons");
        } else {
            this.seasonInterface = new CustomCropsSeasonImpl();
        }
    }

    private void hookJobs() {
        if (pluginManager.isPluginEnabled("Jobs")) {
            this.jobInterface = new JobsRebornImpl();
            hookMessage("JobsReborn");
        } else if (pluginManager.isPluginEnabled("EcoJobs")) {
            this.jobInterface = new EcoJobsImpl();
            hookMessage("EcoJobs");
        }
    }

    private void hookSkills() {
        if (pluginManager.isPluginEnabled("mcMMO")) {
            this.skillInterface = new mcMMOImpl();
            hookMessage("mcMMO");
        } else if (pluginManager.isPluginEnabled("MMOCore")) {
            this.skillInterface = new MMOCoreImpl(ConfigUtils.getConfig("config.yml").getString("other-settings.MMOCore-profession-name", "farmer"));
            hookMessage("MMOCore");
        } else if (pluginManager.isPluginEnabled("AureliumSkills")) {
            this.skillInterface = new AureliumsImpl();
            hookMessage("AureliumSkills");
        } else if (pluginManager.isPluginEnabled("EcoSkills")) {
            this.skillInterface = new EcoSkillsImpl();
            hookMessage("EcoSkills");
        }
    }

    private void hookMessage(String plugin){
        AdventureUtils.consoleMessage("[CustomCrops] " + plugin + " hooked!");
    }

    @NotNull
    public ItemStack build(String key) {
        for (ItemInterface itemInterface : itemInterfaces) {
            ItemStack itemStack = itemInterface.build(key);
            if (itemStack != null) {
                return itemStack;
            }
        }
        return new ItemStack(Material.AIR);
    }

    @Nullable
    public SkillInterface getSkillInterface() {
        return skillInterface;
    }

    @NotNull
    public SeasonInterface getSeasonInterface() {
        return seasonInterface;
    }

    @Nullable
    public JobInterface getJobInterface() {
        return jobInterface;
    }

    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }
}
