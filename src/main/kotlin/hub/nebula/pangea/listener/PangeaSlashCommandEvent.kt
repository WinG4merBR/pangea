package hub.nebula.pangea.listener

import dev.minn.jda.ktx.events.listener
import hub.nebula.pangea.PangeaInstance
import hub.nebula.pangea.command.PangeaCommandContext
import mu.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import kotlin.reflect.jvm.jvmName

class PangeaSlashCommandEvent(private val pangea: PangeaInstance) {
    val logger = KotlinLogging.logger(this::class.jvmName)

    fun handle() = pangea.jda.listener<SlashCommandInteractionEvent> {
        val event = it
        val commandName = event.fullCommandName.split(" ").first()

        val command = pangea.commandManager[commandName]?.create()

        if (command != null) {
            val context = PangeaCommandContext(event)

            val subCommandGroupName = event.subcommandGroup
            val subCommandName = event.subcommandName

            val subCommandGroup =
                if (subCommandGroupName != null) command.getSubCommandGroup(subCommandGroupName) else null
            val subCommand = if (subCommandName != null) {
                if (subCommandGroup != null) {
                    subCommandGroup.getSubCommand(subCommandName)
                } else {
                    command.getSubCommand(subCommandName)
                }
            } else null

            if (subCommand != null) {
                subCommand.executor?.execute(context)
            } else if (subCommandGroupName == null && subCommandName == null) {
                command.executor?.execute(context)
            }

            logger.info { "${context.user.name} (${context.user.id}) used /${event.fullCommandName} command in ${context.guild?.name} (${context.guild?.id})" }
        }
    }
}