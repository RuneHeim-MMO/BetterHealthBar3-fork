package kr.toxicity.healthbar.manager

import kr.toxicity.healthbar.api.compatibility.MythicActiveMob
import kr.toxicity.healthbar.api.event.HealthBarCreateEvent
import kr.toxicity.healthbar.api.manager.PlaceholderManager
import kr.toxicity.healthbar.api.placeholder.PlaceholderContainer
import kr.toxicity.healthbar.pack.PackResource
import kr.toxicity.healthbar.util.ATTRIBUTE_MAX_HEALTH
import kr.toxicity.healthbar.util.armor
import kr.toxicity.healthbar.util.placeholder
import kr.toxicity.healthbar.util.warn
import kr.toxicity.healthbar.version.MinecraftVersion
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType
import org.zuma.examplePlugin.RuneQuestPlugin
import org.zuma.examplePlugin.domain.mob.base.CombatantMob
import org.zuma.examplePlugin.domain.mount.ActiveMountData
import java.util.UUID
import java.util.function.Function

object PlaceholderManagerImpl : PlaceholderManager, BetterHealthBerManager {

    override fun start() {
        PlaceholderContainer.NUMBER.run {
            addPlaceholder("health") { e: HealthBarCreateEvent ->
                e.entity.entity().health
            }
            addPlaceholder("max_health") { e: HealthBarCreateEvent ->
                e.entity.entity().getAttribute(ATTRIBUTE_MAX_HEALTH)!!.value
            }
            addPlaceholder("health_percentage") { e: HealthBarCreateEvent ->
                e.entity.entity().health / e.entity.entity().getAttribute(ATTRIBUTE_MAX_HEALTH)!!.value
            }
            addPlaceholder("absorption") { e: HealthBarCreateEvent ->
                e.entity.entity().absorptionAmount
            }
            addPlaceholder("armor") { e: HealthBarCreateEvent ->
                e.entity.entity().armor
            }
        }
        PlaceholderContainer.STRING.run {
            addPlaceholder("entity_type") { e: HealthBarCreateEvent ->
                e.entity.entity().type.toString().lowercase()
            }
            addPlaceholder("entity_name") { e: HealthBarCreateEvent ->
                e.entity.entity().name
            }
            addPlaceholder("runeheim_entity_name") { e: HealthBarCreateEvent ->
                val mobName = RuneQuestPlugin.getMobRegistry().getMob(e.entity.entity())?.displayName ?: ""
                val activeMount: ActiveMountData? = RuneQuestPlugin.getMountHandler().getMountData(e.entity.entity().uniqueId)

                when {
                    activeMount != null -> activeMount.state()?.name ?: mobName
                    mobName != "" -> mobName
                    else -> ""
                }
            }
            addPlaceholder("runeheim_entity_lvl") { e: HealthBarCreateEvent ->
                val entity = e.entity.entity()
                val mob = RuneQuestPlugin.getMobRegistry().getMob(entity)
                val player = RuneQuestPlugin.getPlayerManager().getRunePlayer(entity.uniqueId)
                val activeMount = RuneQuestPlugin.getMountHandler().getMountData(entity.uniqueId);

                when {
                    activeMount != null -> activeMount.level().toString()
                    player != null -> player.combatLvl.toString()
                    mob is CombatantMob -> mob.combatLvl.toString()
                    mob == null -> "Unknown"
                    else -> "-"
                }
            }
        }
        PlaceholderContainer.BOOL.run {
            addPlaceholder("has_potion_effect", placeholder(1) {
                if (MinecraftVersion.current >= MinecraftVersion.version1_20_3) {
                    Registry.EFFECT.get(NamespacedKey.minecraft(it[0]))
                } else {
                    @Suppress("DEPRECATION")
                    PotionEffectType.getByName(it[0])
                }?.let { type ->
                    Function { pair: HealthBarCreateEvent ->
                        pair.entity.entity().hasPotionEffect(type)
                    }
                } ?: run {
                    warn("Unable to find this potion effect: ${it[0]}")
                    Function { _: HealthBarCreateEvent ->
                        false
                    }
                }
            })
            addPlaceholder("is_player") { e: HealthBarCreateEvent ->
                e.entity.entity() is Player
            }
            addPlaceholder("is_mythic_mob") { e: HealthBarCreateEvent ->
                e.entity.mob() is MythicActiveMob
            }
            addPlaceholder("is_vanilla_mob") { e: HealthBarCreateEvent ->
                e.entity.mob() == null
            }
        }
    }

    override fun reload(resource: PackResource) {

    }
}