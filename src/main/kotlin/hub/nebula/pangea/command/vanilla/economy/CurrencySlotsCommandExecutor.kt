package hub.nebula.pangea.command.vanilla.economy

import hub.nebula.pangea.command.PangeaInteractionContext
import hub.nebula.pangea.command.structure.PangeaSlashCommandExecutor
import hub.nebula.pangea.command.vanilla.economy.declaration.CurrencyCommand.Companion.LOCALE_PREFIX
import hub.nebula.pangea.utils.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class CurrencySlotsCommandExecutor : PangeaSlashCommandExecutor() {
    override suspend fun execute(context: PangeaInteractionContext) {
        val amount = context.getOption("amount")!!.asLong

        context.defer()

        val author = newSuspendedTransaction {
            context.pangeaUser
        }

        if (author.currency < amount) {
            context.reply {
                pretty(
                    context.locale["$LOCALE_PREFIX.slots.notEnoughCurrency", "Stardusts"]
                )
            }
            return
        }

        val slots = arrayOf(
            arrayOf("\uD83C\uDF4E", "\uD83C\uDF4A", "\uD83C\uDF4C"),
            arrayOf("\uD83C\uDF45", "\uD83C\uDF4B", "\uD83C\uDF51"),
            arrayOf("\uD83C\uDF52", "\uD83C\uDF4F", "\uD83C\uDF50")
        )

        val slot1 = slots[context.random.nextInt(0, 3)]
        val slot2 = slots[context.random.nextInt(0, 3)]
        val slot3 = slots[context.random.nextInt(0, 3)]

        val text = StringBuilder().apply {
            appendLine(
                slot1[0] + slot2[0] + slot3[0]
            )
            appendLine(
                slot1[1] + slot2[1] + slot3[1]
            )
            appendLine(
                slot1[2] + slot2[2] + slot3[2]
            )
        }

        val winnedAmount: Long
        val winMessage: String

        val multiplier: Int = when {
            // All rows are equal
            slot1[0] == slot2[0] && slot2[0] == slot3[0] && slot1[1] == slot2[1] && slot2[1] == slot3[1] && slot1[2] == slot2[2] && slot2[2] == slot3[2] -> 4
            // Two rows are equal
            (slot1[0] == slot2[0] && slot2[0] == slot3[0] && slot1[1] == slot2[1] && slot2[1] == slot3[1]) || (slot1[0] == slot2[0] && slot2[0] == slot3[0] && slot1[2] == slot2[2] && slot2[2] == slot3[2]) || (slot1[1] == slot2[1] && slot2[1] == slot3[1] && slot1[2] == slot2[2] && slot2[2] == slot3[2]) -> 3
            // One row is equal
            slot1[0] == slot2[0] && slot2[0] == slot3[0] || slot1[1] == slot2[1] && slot2[1] == slot3[1] || slot1[2] == slot2[2] && slot2[2] == slot3[2] -> 2
            // No rows are equal
            else -> 0
        }

        if (multiplier > 0) {
            winnedAmount = amount * multiplier
            winMessage = context.locale["$LOCALE_PREFIX.slots.youWon", winnedAmount.toString(), if (winnedAmount == 1L) "Stardust" else "Stardusts"]
        } else {
            winnedAmount = -amount
            winMessage = context.locale["$LOCALE_PREFIX.slots.youLose", amount.toString(), if (amount == 1L) "Stardust" else "Stardusts"]
        }

        newSuspendedTransaction {
            author.currency += winnedAmount
        }

        context.reply {
            embed {
                title = "Slots"
                description = text.toString()
                color = Constants.DEFAULT_COLOR

                field {
                    name = context.locale["$LOCALE_PREFIX.slots.result"]
                    value = winMessage
                }
            }
        }
    }
}